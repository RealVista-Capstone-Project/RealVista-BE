package com.sep.realvista.presentation.rest.report;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.report.ReportApplicationService;
import com.sep.realvista.application.report.dto.ReportDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Report", description = "Admin endpoints for managing user and listing reports")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminReportController {

    private final ReportApplicationService reportService;

    @GetMapping
    @Operation(summary = "Get paged reports with optional status filter")
    public ResponseEntity<ApiResponse<PageResponse<ReportDto>>> getReports(
            @RequestParam(required = false) com.sep.realvista.domain.report.ReportStatus status,
            Pageable pageable) {
        log.info("Fetching reports with status: {} and pageable: {}", status, pageable);
        PageResponse<ReportDto> response =
                reportService.getPagedReports(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    @GetMapping("/{id}")
    @Operation(summary = "Get report by ID")
    public ResponseEntity<ApiResponse<ReportDto>> getReport(@PathVariable("id") UUID id) {
        log.info("Fetching report by ID: {}", id);
        ReportDto response = reportService.getReportById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "Start reviewing a report")
    public ResponseEntity<ApiResponse<Void>> startReview(@PathVariable("id") UUID id) {
        log.info("[AdminReport] Start review request received for report ID: {}", id);
        reportService.startReview(id);
        log.info("[AdminReport] Successfully started review for report ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve a report with an admin note")
    public ResponseEntity<ApiResponse<Void>> resolveReport(
            @PathVariable("id") UUID id,
            @RequestBody Map<String, String> body) {
        String adminNote = body.get("admin_note");
        log.info("[AdminReport] Resolve request received for report ID: {} with note: {}", id, adminNote);
        reportService.resolveReport(id, adminNote);
        log.info("[AdminReport] Successfully resolved report ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/dismiss")
    @Operation(summary = "Dismiss a report with an admin note")
    public ResponseEntity<ApiResponse<Void>> dismissReport(
            @PathVariable("id") UUID id,
            @RequestBody Map<String, String> body) {
        String adminNote = body.get("admin_note");
        log.info("[AdminReport] Dismiss request received for report ID: {} with note: {}", id, adminNote);
        reportService.dismissReport(id, adminNote);
        log.info("[AdminReport] Successfully dismissed report ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
