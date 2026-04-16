package com.sep.realvista.domain.listing.appointment;

import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.InvalidBookingRequestException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.repository.AppointmentRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.application.appointment.dto.BlockRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableSlots(UUID listingId, LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new InvalidBookingRequestException("Cannot query slots for a past date");
        }

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", listingId));

        User owner = userRepository.findById(listing.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner", listing.getUserId()));

        LocalTime start = owner.getWorkingStartTime() != null ? owner.getWorkingStartTime() : LocalTime.of(8, 0);
        LocalTime end = owner.getWorkingEndTime() != null ? owner.getWorkingEndTime() : LocalTime.of(17, 0);

        List<LocalTime> allSlots = new ArrayList<>();
        LocalTime current = start;
        while (current.isBefore(end)) {
            allSlots.add(current);
            current = current.plusMinutes(30);
        }

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

        List<Appointment> existingAppointments = appointmentRepository.findByReceiverIdAndStartTimeBetweenAndStatusIn(
                owner.getUserId(),
                dayStart,
                dayEnd,
                List.of(AppointmentStatus.PENDING, AppointmentStatus.ACCEPTED));

        List<LocalTime> availableSlots = new ArrayList<>();
        for (LocalTime slotTime : allSlots) {
            LocalDateTime slotStart = date.atTime(slotTime);
            LocalDateTime slotEnd = slotStart.plusMinutes(30);
            boolean isBooked = existingAppointments.stream()
                    .anyMatch(appt -> appt.getStartTime().isBefore(slotEnd) && appt.getEndTime().isAfter(slotStart));

            if (!isBooked) {
                availableSlots.add(slotTime);
            }
        }

        return availableSlots;
    }

    @Transactional
    public BookTourResult bookTour(UUID listingId, UUID senderId, List<LocalDateTime> selectedSlots, String notes) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", listingId));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", senderId));

        User owner = userRepository.findById(listing.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner", listing.getUserId()));

        if (listing.getUserId().equals(senderId)) {
            throw new InvalidBookingRequestException("Owner cannot book a tour of their own listing");
        }

        LocalTime workStart = owner.getWorkingStartTime() != null ? owner.getWorkingStartTime() : LocalTime.of(8, 0);
        LocalTime workEnd = owner.getWorkingEndTime() != null ? owner.getWorkingEndTime() : LocalTime.of(17, 0);

        // Sort slots by time and merge consecutive slots
        List<LocalDateTime> sortedSlots = selectedSlots.stream()
                .sorted()
                .collect(Collectors.toList());

        // Merge consecutive slots (within 30 minutes of each other) into time ranges
        List<LocalDateTime> mergedStartTimes = new ArrayList<>();
        List<LocalDateTime> mergedEndTimes = new ArrayList<>();

        if (!sortedSlots.isEmpty()) {
            LocalDateTime rangeStart = sortedSlots.get(0);
            LocalDateTime rangeEnd = sortedSlots.get(0).plusMinutes(30);

            for (int i = 1; i < sortedSlots.size(); i++) {
                LocalDateTime currentSlot = sortedSlots.get(i);
                // If current slot is within 30 min of previous end, extend the range
                if (!currentSlot.isAfter(rangeEnd)) {
                    rangeEnd = currentSlot.plusMinutes(30);
                } else {
                    // Save current range and start new one
                    mergedStartTimes.add(rangeStart);
                    mergedEndTimes.add(rangeEnd);
                    rangeStart = currentSlot;
                    rangeEnd = currentSlot.plusMinutes(30);
                }
            }
            // Add the last range
            mergedStartTimes.add(rangeStart);
            mergedEndTimes.add(rangeEnd);
        }

        // Validate and check conflicts for merged ranges
        for (int i = 0; i < mergedStartTimes.size(); i++) {
            LocalDateTime slot = mergedStartTimes.get(i);
            LocalDateTime slotEnd = mergedEndTimes.get(i);

            if (slot.isBefore(LocalDateTime.now())) {
                throw new InvalidBookingRequestException("Cannot book a tour in the past: " + slot);
            }

            LocalTime slotTime = slot.toLocalTime();
            if (slotTime.isBefore(workStart) || slotTime.plusMinutes(30).isAfter(workEnd)) {
                throw new InvalidBookingRequestException(
                        "Slot " + slot + " is outside working hours (" + workStart + " - " + workEnd + ")");
            }

            List<Appointment> overlapping = appointmentRepository.findByReceiverIdAndStartTimeBetweenAndStatusIn(
                    owner.getUserId(),
                    slot.toLocalDate().atStartOfDay(),
                    slot.toLocalDate().atTime(LocalTime.MAX),
                    List.of(AppointmentStatus.PENDING, AppointmentStatus.ACCEPTED));

            boolean hasConflict = overlapping.stream()
                    .anyMatch(appt -> appt.getStartTime().isBefore(slotEnd) && appt.getEndTime().isAfter(slot));

            if (hasConflict) {
                throw new BusinessConflictException("Slot " + slot + " to " + slotEnd + " is already booked");
            }
        }

        // Create single appointment for each merged range
        List<Appointment> createdAppointments = new ArrayList<>();
        for (int i = 0; i < mergedStartTimes.size(); i++) {
            LocalDateTime startTime = mergedStartTimes.get(i);
            LocalDateTime endTime = mergedEndTimes.get(i);

            Appointment appointment = Appointment.builder()
                    .listingId(listingId)
                    .listing(listing)
                    .senderId(senderId)
                    .sender(sender)
                    .receiverId(owner.getUserId())
                    .receiver(owner)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(AppointmentStatus.PENDING)
                    .appointmentType(AppointmentType.TOUR)
                    .senderNotes(notes)
                    .build();
            createdAppointments.add(appointmentRepository.save(appointment));
        }

        return new BookTourResult(createdAppointments, listing, sender, owner);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByUserId(UUID userId, LocalDateTime startDate,
            LocalDateTime endDate, List<AppointmentStatus> statuses) {
        if (statuses != null && !statuses.isEmpty()) {
            return appointmentRepository.findByUserIdAndDateRangeAndStatusIn(
                    userId, startDate, endDate, statuses);
        }
        return appointmentRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Transactional
    public Appointment updateAppointmentStatus(UUID appointmentId, UUID currentUserId,
            AppointmentStatus newStatus, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        boolean isSender = appointment.getSenderId().equals(currentUserId);
        boolean isReceiver = appointment.getReceiverId().equals(currentUserId);

        if (!isSender && !isReceiver) {
            throw new BusinessConflictException("You are not authorized to update this appointment");
        }

        switch (newStatus) {
            case ACCEPTED -> {
                if (!isReceiver) {
                    throw new BusinessConflictException("Only the receiver can accept an appointment");
                }
                appointment.accept();
            }
            case REJECTED -> {
                if (!isReceiver) {
                    throw new BusinessConflictException("Only the receiver can reject an appointment");
                }
                appointment.reject(reason);
            }
            case CANCELED -> {
                if (LocalDateTime.now().isAfter(appointment.getStartTime().minusHours(4))) {
                    throw new BusinessConflictException(
                            "Cannot cancel an appointment less than 4 hours before the start time");
                }
                appointment.cancel(currentUserId, reason);
            }
            default -> {
                throw new BusinessConflictException("Invalid status update: " + newStatus);
            }
        }

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void syncBlocks(UUID userId, LocalDateTime startDate, LocalDateTime endDate,
                           List<BlockRequest> blockRequests) {
        // Fetch existing blocks in the range
        // Note: For blocks, the user is both sender and receiver, 
        // but getAvailableSlots checks receiverId
        List<Appointment> existingAppointments = appointmentRepository.findByReceiverIdAndStartTimeBetweenAndStatusIn(
                userId, startDate, endDate, List.of(AppointmentStatus.ACCEPTED, AppointmentStatus.PENDING));

        List<Appointment> blocksToRemove = existingAppointments.stream()
                .filter(a -> a.getAppointmentType() == AppointmentType.BLOCK)
                .collect(Collectors.toList());

        appointmentRepository.deleteAll(blocksToRemove);

        // Create and save new blocks
        List<Appointment> newBlocks = blockRequests.stream()
                .map(req -> Appointment.builder()
                        .senderId(userId)
                        .receiverId(userId)
                        .startTime(req.getStartTime())
                        .endTime(req.getEndTime())
                        .status(AppointmentStatus.ACCEPTED)
                        .appointmentType(AppointmentType.BLOCK)
                        .build())
                .collect(Collectors.toList());

        appointmentRepository.saveAll(newBlocks);
    }

    @Transactional
    public void deleteAppointment(UUID userId, UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        if (!appointment.getSenderId().equals(userId) && !appointment.getReceiverId().equals(userId)) {
            throw new BusinessConflictException("You are not authorized to delete this appointment");
        }

        appointment.markAsDeleted();
        appointmentRepository.save(appointment);
    }
}
