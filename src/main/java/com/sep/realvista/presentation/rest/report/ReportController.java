package com.sep.realvista.presentation.rest.report;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.report.ReportApplicationService;
import com.sep.realvista.application.report.dto.CreateReportRequest;
import com.sep.realvista.application.report.dto.ReportDto;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Endpoints for users to submit reports")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    private final ReportApplicationService reportService;

    @PostMapping
    @Operation(summary = "Submit a new report", description = "Users can report a listing or another user")
    public ResponseEntity<ApiResponse<ReportDto>> createReport(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @Valid @RequestBody CreateReportRequest request) {
        log.info("User {} submitting report for target {} of type {}",
                userDetails.getUserId(), request.getTargetId(), request.getTargetType());
        ReportDto report = reportService.createReport(userDetails.getUserId(), request);
        log.info("Report {} created successfully by user {}", report.getReportId(), userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
