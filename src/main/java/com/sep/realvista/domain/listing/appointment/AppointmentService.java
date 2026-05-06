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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final AppointmentAuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableSlots(UUID listingId, LocalDate date, UUID excludeId) {
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

        List<Appointment> existingAppointments = appointmentRepository.findOverlappingAppointments(
                owner.getUserId(),
                dayStart,
                dayEnd,
                List.of(AppointmentStatus.PENDING, AppointmentStatus.ACCEPTED, AppointmentStatus.RESCHEDULE_PENDING),
                excludeId);

        log.debug("Found {} potential overlapping appointments for Agent {}. Excluding ID: {}", 
                existingAppointments.size(), owner.getUserId(), excludeId);

        List<LocalTime> availableSlots = new ArrayList<>();
        for (LocalTime slotTime : allSlots) {
            LocalDateTime slotStart = date.atTime(slotTime);
            LocalDateTime slotEnd = slotStart.plusMinutes(30);
            
            boolean isBooked = existingAppointments.stream()
                    .filter(appt -> excludeId == null || !appt.getAppointmentId().equals(excludeId))
                    .anyMatch(appt -> (appt.getStartTime().isBefore(slotEnd) 
                                && appt.getEndTime().isAfter(slotStart))
                            || (appt.getProposedStartTime() != null 
                                && appt.getProposedStartTime().isBefore(slotEnd) 
                                && appt.getProposedEndTime().isAfter(slotStart)));

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

        List<LocalDateTime> sortedSlots = selectedSlots.stream()
                .sorted()
                .collect(Collectors.toList());

        List<LocalDateTime> mergedStartTimes = new ArrayList<>();
        List<LocalDateTime> mergedEndTimes = new ArrayList<>();

        if (!sortedSlots.isEmpty()) {
            LocalDateTime rangeStart = sortedSlots.get(0);
            LocalDateTime rangeEnd = sortedSlots.get(0).plusMinutes(30);

            for (int i = 1; i < sortedSlots.size(); i++) {
                LocalDateTime currentSlot = sortedSlots.get(i);
                if (!currentSlot.isAfter(rangeEnd)) {
                    rangeEnd = currentSlot.plusMinutes(30);
                } else {
                    mergedStartTimes.add(rangeStart);
                    mergedEndTimes.add(rangeEnd);
                    rangeStart = currentSlot;
                    rangeEnd = currentSlot.plusMinutes(30);
                }
            }
            mergedStartTimes.add(rangeStart);
            mergedEndTimes.add(rangeEnd);
        }

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

            List<Appointment> overlapping = appointmentRepository.findOverlappingAppointments(
                    owner.getUserId(),
                    slot,
                    slotEnd,
                    List.of(AppointmentStatus.PENDING, AppointmentStatus.ACCEPTED, 
                            AppointmentStatus.RESCHEDULE_PENDING),
                    null);

            boolean hasConflict = overlapping.stream()
                    .anyMatch(appt -> appt.getStartTime().isBefore(slotEnd) && appt.getEndTime().isAfter(slot));

            if (hasConflict) {
                throw new BusinessConflictException(
                    "Slot " + slot + " to " + slotEnd + " is already booked", "SLOT_ALREADY_BOOKED");
            }
        }

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
            Appointment saved = appointmentRepository.save(appointment);
            createdAppointments.add(saved);
            
            logAudit(LogAuditParams.builder()
                    .appt(saved)
                    .actorId(senderId)
                    .action("BOOK")
                    .newStatus(AppointmentStatus.PENDING.name())
                    .newStart(startTime)
                    .notes(notes)
                    .build());
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

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByUserId(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
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

        AppointmentStatus oldStatus = appointment.getStatus();
        switch (newStatus) {
            case ACCEPTED -> {
                if (!isReceiver) {
                    throw new BusinessConflictException("Only the receiver can accept an appointment");
                }
                appointment.accept();
                logAudit(LogAuditParams.builder().appt(appointment).actorId(currentUserId).action("ACCEPT")
                        .oldStatus(oldStatus.name()).newStatus(newStatus.name()).build());
            }
            case REJECTED -> {
                if (!isReceiver) {
                    throw new BusinessConflictException("Only the receiver can reject an appointment");
                }
                appointment.reject(reason);
                logAudit(LogAuditParams.builder().appt(appointment).actorId(currentUserId).action("REJECT")
                        .oldStatus(oldStatus.name()).newStatus(newStatus.name()).notes(reason).build());
            }
            case CANCELED -> {
                if (LocalDateTime.now().isAfter(appointment.getStartTime().minusHours(4))) {
                    throw new BusinessConflictException(
                            "Cannot cancel an appointment less than 4 hours before the start time");
                }
                appointment.cancel(currentUserId, reason);
                logAudit(LogAuditParams.builder().appt(appointment).actorId(currentUserId).action("CANCEL")
                        .oldStatus(oldStatus.name()).newStatus(newStatus.name()).notes(reason).build());
            }
            case COMPLETED -> {
                if (!isReceiver) {
                    throw new BusinessConflictException("Only the receiver can mark an appointment as completed");
                }
                appointment.complete();
                logAudit(LogAuditParams.builder().appt(appointment).actorId(currentUserId).action("COMPLETE")
                        .oldStatus(oldStatus.name()).newStatus(newStatus.name()).build());
            }
            default -> throw new BusinessConflictException("Invalid status update: " + newStatus);
        }

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment proposeReschedule(UUID appointmentId, UUID actorId, LocalDateTime newStartTime, 
                                        LocalDateTime newEndTime, String reason) {
        Appointment appointment = getAppointmentById(appointmentId);
        
        if (!appointment.getSenderId().equals(actorId) && !appointment.getReceiverId().equals(actorId)) {
            throw new BusinessConflictException("You are not authorized to reschedule this appointment");
        }

        checkTimeConflict(appointment.getReceiverId(), appointment.getListingId(), 
                         newStartTime, newEndTime, appointmentId);

        AppointmentStatus oldStatus = appointment.getStatus();
        LocalDateTime oldStart = appointment.getStartTime();
        
        appointment.proposeReschedule(actorId, newStartTime, newEndTime, reason);
        Appointment saved = appointmentRepository.save(appointment);
        
        logAudit(LogAuditParams.builder()
                .appt(saved)
                .actorId(actorId)
                .action("PROPOSE_RESCHEDULE")
                .oldStatus(oldStatus.name())
                .newStatus(AppointmentStatus.RESCHEDULE_PENDING.name())
                .oldStart(oldStart)
                .newStart(newStartTime)
                .notes(reason)
                .build());
        
        return saved;
    }

    @Transactional
    public Appointment confirmReschedule(UUID appointmentId, UUID actorId) {
        Appointment appointment = getAppointmentById(appointmentId);
        
        if (!appointment.getSenderId().equals(actorId) && !appointment.getReceiverId().equals(actorId)) {
            throw new BusinessConflictException("You are not authorized to confirm this reschedule");
        }

        AppointmentStatus oldStatus = appointment.getStatus();
        LocalDateTime oldStart = appointment.getStartTime();
        appointment.confirmReschedule(actorId);
        Appointment saved = appointmentRepository.save(appointment);
        
        logAudit(LogAuditParams.builder()
                .appt(saved)
                .actorId(actorId)
                .action("CONFIRM_RESCHEDULE")
                .oldStatus(oldStatus.name())
                .newStatus(AppointmentStatus.ACCEPTED.name())
                .oldStart(oldStart)
                .newStart(saved.getStartTime())
                .build());
        
        return saved;
    }

    @Transactional
    public Appointment rejectReschedule(UUID appointmentId, UUID actorId, String reason) {
        Appointment appointment = getAppointmentById(appointmentId);
        
        if (!appointment.getSenderId().equals(actorId) && !appointment.getReceiverId().equals(actorId)) {
            throw new BusinessConflictException("You are not authorized to reject this reschedule");
        }

        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.rejectReschedule(actorId, reason);
        Appointment saved = appointmentRepository.save(appointment);
        
        logAudit(LogAuditParams.builder()
                .appt(saved)
                .actorId(actorId)
                .action("REJECT_RESCHEDULE")
                .oldStatus(oldStatus.name())
                .newStatus(AppointmentStatus.REJECTED.name())
                .notes(reason)
                .build());
        
        return saved;
    }

    @Transactional
    public Appointment cancelRescheduleProposal(UUID appointmentId, UUID actorId) {
        Appointment appointment = getAppointmentById(appointmentId);
        
        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.cancelRescheduleProposal(actorId);
        Appointment saved = appointmentRepository.save(appointment);
        
        logAudit(LogAuditParams.builder()
                .appt(saved)
                .actorId(actorId)
                .action("CANCEL_RESCHEDULE")
                .oldStatus(oldStatus.name())
                .newStatus(AppointmentStatus.ACCEPTED.name())
                .build());
        
        return saved;
    }

    private void checkTimeConflict(UUID ownerId, UUID listingId, LocalDateTime start,
                                  LocalDateTime end, UUID excludeApptId) {
        List<Appointment> overlapping = appointmentRepository.findOverlappingAppointments(
                ownerId, start, end,
                List.of(AppointmentStatus.PENDING, AppointmentStatus.ACCEPTED, AppointmentStatus.RESCHEDULE_PENDING),
                excludeApptId);

        if (!overlapping.isEmpty()) {
            throw new BusinessConflictException("The proposed time slot is already booked", "SLOT_ALREADY_BOOKED");
        }
    }

    private void logAudit(LogAuditParams params) {
        AppointmentAuditLog auditLog = AppointmentAuditLog.builder()
                .appointmentId(params.getAppt().getAppointmentId())
                .actorId(params.getActorId())
                .action(params.getAction())
                .oldStatus(params.getOldStatus())
                .newStatus(params.getNewStatus())
                .oldStartTime(params.getOldStart())
                .newStartTime(params.getNewStart())
                .notes(params.getNotes())
                .build();
        auditLogRepository.save(auditLog);
    }

    @lombok.Builder
    @lombok.Getter
    private static class LogAuditParams {
        private final Appointment appt;
        private final UUID actorId;
        private final String action;
        private final String oldStatus;
        private final String newStatus;
        private final LocalDateTime oldStart;
        private final LocalDateTime newStart;
        private final String notes;
    }

    @Transactional
    public void syncBlocks(UUID userId, LocalDateTime startDate, LocalDateTime endDate,
                           List<BlockRequest> blockRequests) {
        List<Appointment> existingAppointments = appointmentRepository.findOverlappingAppointments(
                userId, startDate, endDate, 
                List.of(AppointmentStatus.ACCEPTED, AppointmentStatus.PENDING, AppointmentStatus.RESCHEDULE_PENDING),
                null);

        List<Appointment> blocksToRemove = existingAppointments.stream()
                .filter(a -> a.getAppointmentType() == AppointmentType.BLOCK)
                .collect(Collectors.toList());

        appointmentRepository.deleteAll(blocksToRemove);

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

    @Transactional
    public List<Appointment> cancelActiveAppointmentsByListingId(UUID listingId, UUID ownerId, String reason) {
        List<Appointment> activeAppointments = appointmentRepository.findByListingIdAndStatusIn(
                listingId, List.of(AppointmentStatus.PENDING, AppointmentStatus.ACCEPTED));
        
        for (Appointment appt : activeAppointments) {
            appt.cancel(ownerId, reason);
            appointmentRepository.save(appt);
        }
        
        log.info("Cancelled {} active appointments for listing ID: {}", activeAppointments.size(), listingId);
        return activeAppointments;
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByListingIdAndStatuses(UUID listingId, List<AppointmentStatus> statuses) {
        return appointmentRepository.findByListingIdAndStatusIn(listingId, statuses);
    }

    @Transactional(readOnly = true)
    public Appointment getAppointmentById(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
    }

    @Transactional
    public Appointment save(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }
}
