package com.sep.realvista.infrastructure.scheduler;

import com.sep.realvista.application.appointment.service.AppointmentApplicationService;
import com.sep.realvista.domain.listing.appointment.Appointment;
import com.sep.realvista.domain.listing.appointment.AppointmentStatus;
import com.sep.realvista.domain.listing.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentStatusScheduler {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentApplicationService appointmentApplicationService;

    /**
     * Automatically processes appointment status transitions:
     * 1. Completes ACCEPTED appointments that ended more than 30 minutes ago.
     * 2. Cancels PENDING appointments that ended more than 30 minutes ago (Expired).
     * Runs every 30 minutes.
     */
    @Scheduled(cron = "0 0/30 * * * *")
    public void processAutomaticStatusTransitions() {
        log.info("Starting scheduled processing of appointment status transitions...");

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);

        // 1. Auto-complete ACCEPTED appointments
        List<Appointment> pastAccepted = appointmentRepository.findByStatusAndEndTimeBefore(
                AppointmentStatus.ACCEPTED, threshold);
        
        log.info("Found {} past ACCEPTED appointments to complete.", pastAccepted.size());
        for (Appointment appt : pastAccepted) {
            try {
                appointmentApplicationService.autoCompleteAppointment(appt.getAppointmentId());
            } catch (Exception e) {
                log.error("Failed to auto-complete appointment {}: {}", appt.getAppointmentId(), e.getMessage());
            }
        }

        // 2. Auto-cancel PENDING appointments (Expired)
        List<Appointment> pastPending = appointmentRepository.findByStatusAndEndTimeBefore(
                AppointmentStatus.PENDING, threshold);
        
        log.info("Found {} past PENDING appointments to cancel.", pastPending.size());
        for (Appointment appt : pastPending) {
            try {
                appointmentApplicationService.autoCancelAppointment(appt.getAppointmentId());
            } catch (Exception e) {
                log.error("Failed to auto-cancel appointment {}: {}", appt.getAppointmentId(), e.getMessage());
            }
        }
    }
}
