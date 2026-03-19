package com.sep.realvista.application.appointment.service;

import com.sep.realvista.application.appointment.dto.BookTourRequest;
import com.sep.realvista.domain.listing.appointment.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AppointmentApplicationService {

    private final AppointmentService appointmentService;

    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableSlots(UUID listingId, LocalDate date) {
        log.info("Fetching available slots for listingId: {} on date: {}", listingId, date);
        return appointmentService.getAvailableSlots(listingId, date);
    }

    public void bookTour(UUID userId, BookTourRequest request) {
        log.info("Booking tour for listingId: {}, userId: {}", request.getListingId(), userId);
        appointmentService.bookTour(
                request.getListingId(),
                userId,
                request.getSelectedSlots(),
                request.getNotes()
        );
    }
}
