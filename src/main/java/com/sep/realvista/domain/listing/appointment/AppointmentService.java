package com.sep.realvista.domain.listing.appointment;

import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.InvalidBookingRequestException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.repository.AppointmentRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        LocalTime start = owner.getWorkingStartTime();
        LocalTime end = owner.getWorkingEndTime();

        if (start == null) {
            start = LocalTime.of(8, 0);
        }
        if (end == null) {
            end = LocalTime.of(17, 0);
        }

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
                List.of(AppointmentStatus.PENDING, AppointmentStatus.ACCEPTED)
        );

        List<LocalTime> availableSlots = new ArrayList<>();
        for (LocalTime slotTime : allSlots) {
            LocalDateTime slotStart = date.atTime(slotTime);
            LocalDateTime slotEnd = slotStart.plusMinutes(30);
            boolean isBooked = existingAppointments.stream().anyMatch(appt ->
                    appt.getStartTime().isBefore(slotEnd) && appt.getEndTime().isAfter(slotStart)
            );

            if (!isBooked) {
                availableSlots.add(slotTime);
            }
        }

        return availableSlots;
    }

    @Transactional
    public void bookTour(UUID listingId, UUID senderId, List<LocalDateTime> selectedSlots, String notes) {
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

        for (LocalDateTime slot : selectedSlots) {
            if (slot.isBefore(LocalDateTime.now())) {
                throw new InvalidBookingRequestException("Cannot book a tour in the past: " + slot);
            }

            LocalTime slotTime = slot.toLocalTime();
            if (slotTime.isBefore(workStart) || slotTime.plusMinutes(30).isAfter(workEnd)) {
                throw new InvalidBookingRequestException(
                        "Slot " + slot + " is outside working hours (" + workStart + " - " + workEnd + ")"
                );
            }

            LocalDateTime slotEnd = slot.plusMinutes(30);
            List<Appointment> overlapping = appointmentRepository.findByReceiverIdAndStartTimeBetweenAndStatusIn(
                    owner.getUserId(),
                    slot.toLocalDate().atStartOfDay(),
                    slot.toLocalDate().atTime(LocalTime.MAX),
                    List.of(AppointmentStatus.PENDING, AppointmentStatus.ACCEPTED)
            );

            boolean hasConflict = overlapping.stream().anyMatch(appt ->
                    appt.getStartTime().isBefore(slotEnd) && appt.getEndTime().isAfter(slot)
            );

            if (hasConflict) {
                throw new BusinessConflictException("Slot " + slot + " is already booked");
            }

            Appointment appointment = Appointment.builder()
                    .listingId(listingId)
                    .listing(listing)
                    .senderId(senderId)
                    .sender(sender)
                    .receiverId(owner.getUserId())
                    .receiver(owner)
                    .startTime(slot)
                    .endTime(slotEnd)
                    .status(AppointmentStatus.PENDING)
                    .appointmentType(AppointmentType.TOUR)
                    .senderNotes(notes)
                    .build();
            appointmentRepository.save(appointment);
        }
    }
}
