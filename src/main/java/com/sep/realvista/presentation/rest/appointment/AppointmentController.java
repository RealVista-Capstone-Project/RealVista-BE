package com.sep.realvista.presentation.rest.appointment;

import com.sep.realvista.application.appointment.dto.BookTourRequest;
import com.sep.realvista.application.appointment.service.AppointmentApplicationService;
import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.infrastructure.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment Management", description = "Endpoints for scheduling and managing property tours")
@Slf4j
public class AppointmentController {

    private final AppointmentApplicationService appointmentApplicationService;

    @GetMapping("/slots")
    @Operation(summary = "Get available time slots", 
            description = "Retrieves available appointment slots for a given listing on a specific date")
    public ResponseEntity<ApiResponse<List<LocalTime>>> getAvailableSlots(
            @RequestParam("listing_id") UUID listingId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        log.info("Request to get available slots - traceId: {}, listingId: {}, date: {}", 
                traceId, listingId, date);

        List<LocalTime> slots = appointmentApplicationService.getAvailableSlots(listingId, date);
        return ResponseEntity.ok(ApiResponse.success("Available slots retrieved successfully", slots));
    }

    @PostMapping
    @Operation(summary = "Book a tour", description = "Schedule a property tour for a listing")
    public ResponseEntity<ApiResponse<Void>> bookTour(
            @RequestBody BookTourRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        log.info("Request to book tour - traceId: {}, userId: {}, listingId: {}", 
                traceId, currentUser.getUserId(), request.getListingId());

        appointmentApplicationService.bookTour(currentUser.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Tour booked successfully", null));
    }
}
