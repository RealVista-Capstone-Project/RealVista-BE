package com.sep.realvista.presentation.rest.appointment;

import com.sep.realvista.application.appointment.dto.AppointmentResponse;
import com.sep.realvista.application.appointment.dto.AppointmentDashboardSnapshotResponse;
import com.sep.realvista.application.appointment.dto.AppointmentSummaryResponse;
import com.sep.realvista.application.appointment.dto.BookTourRequest;
import com.sep.realvista.application.appointment.dto.UpdateAppointmentStatusRequest;
import com.sep.realvista.application.appointment.dto.SyncBlocksRequest;
import com.sep.realvista.application.appointment.service.AppointmentApplicationService;
import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.domain.listing.appointment.AppointmentStatus;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment Management", description = "Endpoints for scheduling and managing property tours")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class AppointmentController {

        private final AppointmentApplicationService appointmentApplicationService;

        @GetMapping("/slots")
        @Operation(summary = "Get available time slots", 
                   description = "Retrieves available appointment slots for a given listing on a specific date")
        public ResponseEntity<ApiResponse<List<LocalTime>>> getAvailableSlots(
                        @RequestParam("listing_id") UUID listingId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
                String traceId = UUID.randomUUID().toString();
                MDC.put("traceId", traceId);
                try {
                        log.info("Request to get available slots - traceId: {}, listingId: {}, date: {}",
                                        traceId, listingId, date);

                        List<LocalTime> slots = appointmentApplicationService.getAvailableSlots(listingId, date);
                        return ResponseEntity.ok(ApiResponse.success("Available slots retrieved successfully", slots));
                } finally {
                        MDC.remove("traceId");
                }
        }

        @PostMapping
        @Operation(summary = "Book a tour", description = "Schedule a property tour for a listing")
        public ResponseEntity<ApiResponse<Void>> bookTour(
                        @Valid @RequestBody BookTourRequest request,
                        @AuthenticationPrincipal SecurityUserDetails currentUser) {
                String traceId = UUID.randomUUID().toString();
                MDC.put("traceId", traceId);
                try {
                        log.info("Request to book tour - traceId: {}, userId: {}, listingId: {}",
                                        traceId, currentUser.getUserId(), request.getListingId());

                        appointmentApplicationService.bookTour(currentUser.getUserId(), request);

                        log.info("Tour booked successfully - traceId: {}, userId: {}, listingId: {}",
                                        traceId, currentUser.getUserId(), request.getListingId());

                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.success("Tour booked successfully", null));
                } finally {
                        MDC.remove("traceId");
                }
        }

        @GetMapping
        @Operation(summary = "Get user appointments", 
                   description = "Retrieves appointments for the current user within a date range")
        public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointments(
                        @RequestParam(name = "start_date", required = false) 
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(name = "end_date", required = false) 
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(required = false) List<String> statuses,
                        @AuthenticationPrincipal SecurityUserDetails currentUser) {
                String traceId = UUID.randomUUID().toString();
                MDC.put("traceId", traceId);
                try {
                        log.info("Request to get appointments - traceId: {}, userId: {},"
                                        + " startDate: {}, endDate: {}, statuses: {}",
                                        traceId, currentUser.getUserId(), startDate, endDate, statuses);

                        LocalDateTime startDateTime = startDate != null
                                        ? startDate.atStartOfDay()
                                        : LocalDateTime.now().minusDays(30);
                        LocalDateTime endDateTime = endDate != null
                                        ? endDate.atTime(LocalTime.MAX)
                                        : LocalDateTime.now().plusDays(30);

                        List<AppointmentStatus> statusEnums = null;
                        if (statuses != null && !statuses.isEmpty()) {
                                statusEnums = statuses.stream()
                                                .map(s -> AppointmentStatus.valueOf(s.toUpperCase()))
                                                .collect(java.util.stream.Collectors.toList());
                        }

                        List<AppointmentResponse> appointments = appointmentApplicationService.getAppointments(
                                        currentUser.getUserId(), startDateTime, endDateTime, statusEnums);

                        log.info("Retrieved {} appointments for userId: {}", appointments.size(),
                                        currentUser.getUserId());
                        return ResponseEntity
                                        .ok(ApiResponse.success("Appointments retrieved successfully", appointments));
                } finally {
                        MDC.remove("traceId");
                }
        }

        @GetMapping("/summary")
        @Operation(summary = "Get appointment summary", description = "Retrieves aggregate appointment metrics"
                        + " for the current user within an optional date range")
        public ResponseEntity<ApiResponse<AppointmentSummaryResponse>> getAppointmentSummary(
                        @RequestParam(name = "start_date", required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(name = "end_date", required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @AuthenticationPrincipal SecurityUserDetails currentUser) {
                String traceId = UUID.randomUUID().toString();
                MDC.put("traceId", traceId);
                try {
                        log.info("Request to get appointment summary - traceId: {}, userId: {}, startDate: {},"
                                        + " endDate: {}", traceId, currentUser.getUserId(), startDate, endDate);

                        LocalDateTime startDateTime = startDate != null
                                        ? startDate.atStartOfDay()
                                        : LocalDateTime.now().minusDays(30);
                        LocalDateTime endDateTime = endDate != null
                                        ? endDate.atTime(LocalTime.MAX)
                                        : LocalDateTime.now().plusDays(30);

                        AppointmentSummaryResponse summary = appointmentApplicationService.getAppointmentSummary(
                                        currentUser.getUserId(), startDateTime, endDateTime);

                        return ResponseEntity.ok(
                                        ApiResponse.success("Appointment summary retrieved successfully", summary));
                } finally {
                        MDC.remove("traceId");
                }
        }

        @GetMapping("/dashboard-snapshot")
        @Operation(summary = "Get dashboard appointment snapshot",
                   description = "Returns calendar-focused appointment data for agent dashboard")
        public ResponseEntity<ApiResponse<AppointmentDashboardSnapshotResponse>> getDashboardSnapshot(
                        @RequestParam(name = "start_date", required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(name = "end_date", required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @AuthenticationPrincipal SecurityUserDetails currentUser) {
                String traceId = UUID.randomUUID().toString();
                MDC.put("traceId", traceId);
                try {
                        LocalDate resolvedStartDate = startDate != null ? startDate : LocalDate.now();
                        LocalDate resolvedEndDate = endDate != null ? endDate : resolvedStartDate.plusDays(29);

                        log.info("Request to get dashboard appointment snapshot - traceId: {}, userId: {},"
                                        + " startDate: {}, endDate: {}",
                                        traceId, currentUser.getUserId(), resolvedStartDate, resolvedEndDate);

                        AppointmentDashboardSnapshotResponse snapshot = appointmentApplicationService.getDashboardSnapshot(
                                        currentUser.getUserId(), resolvedStartDate, resolvedEndDate);

                        return ResponseEntity.ok(
                                        ApiResponse.success("Dashboard appointment snapshot retrieved successfully",
                                                        snapshot));
                } finally {
                        MDC.remove("traceId");
                }
        }

        @PatchMapping("/{appointmentId}")
        @Operation(summary = "Update appointment status", description = "Accept, reject, or cancel an appointment")
        public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointmentStatus(
                        @PathVariable UUID appointmentId,
                        @Valid @RequestBody UpdateAppointmentStatusRequest request,
                        @AuthenticationPrincipal SecurityUserDetails currentUser) {
                String traceId = UUID.randomUUID().toString();
                MDC.put("traceId", traceId);
                try {
                        log.info("Request to update appointment status - traceId: {},"
                                        + " appointmentId: {}, userId: {}, newStatus: {}",
                                        traceId, appointmentId, currentUser.getUserId(), request.getStatus());

                        AppointmentResponse updated = appointmentApplicationService.updateAppointmentStatus(
                                        currentUser.getUserId(), appointmentId, request);

                        log.info("Appointment status updated successfully - traceId: {}, appointmentId: {}",
                                        traceId, appointmentId);
                        return ResponseEntity
                                        .ok(ApiResponse.success("Appointment status updated successfully", updated));
                } finally {
                        MDC.remove("traceId");
                }
        }

        @PostMapping("/blocks/sync")
        @Operation(summary = "Sync busy blocks", description = "Batch create/update busy time blocks for the agent")
        public ResponseEntity<ApiResponse<Void>> syncBlocks(
                        @Valid @RequestBody SyncBlocksRequest request,
                        @AuthenticationPrincipal SecurityUserDetails currentUser) {
                String traceId = UUID.randomUUID().toString();
                MDC.put("traceId", traceId);
                try {
                        log.info("Request to sync blocks - traceId: {}, userId: {}, blockCount: {}",
                                        traceId, currentUser.getUserId(), request.getBlocks().size());

                        appointmentApplicationService.syncBlocks(currentUser.getUserId(), request);

                        log.info("Blocks synced successfully - traceId: {}, userId: {}",
                                        traceId, currentUser.getUserId());

                        return ResponseEntity.ok(ApiResponse.success("Blocks synced successfully", null));
                } finally {
            MDC.remove("traceId");
        }
    }

    @DeleteMapping("/{appointmentId}")
    @Operation(summary = "Delete an appointment", 
               description = "Hard delete an appointment (primarily for busy blocks)")
    public ResponseEntity<ApiResponse<Void>> deleteAppointment(
            @PathVariable UUID appointmentId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        try {
            log.info("Request to delete appointment - traceId: {}, appointmentId: {}, userId: {}",
                    traceId, appointmentId, currentUser.getUserId());

            appointmentApplicationService.deleteAppointment(currentUser.getUserId(), appointmentId);

            log.info("Appointment deleted successfully - traceId: {}, appointmentId: {}",
                    traceId, appointmentId);
            return ResponseEntity.ok(ApiResponse.success("Appointment deleted successfully", null));
        } finally {
            MDC.remove("traceId");
        }
    }
}
