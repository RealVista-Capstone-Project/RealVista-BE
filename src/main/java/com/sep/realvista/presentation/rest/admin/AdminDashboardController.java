package com.sep.realvista.presentation.rest.admin;

import com.sep.realvista.application.admin.AdminDashboardApplicationService;
import com.sep.realvista.application.admin.dto.AdminOverviewResponse;
import com.sep.realvista.application.admin.dto.AdminStatsResponse;
import com.sep.realvista.application.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Dashboard", description = "Admin endpoints for platform overview and statistics")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminDashboardController {

    private final AdminDashboardApplicationService dashboardService;

    @GetMapping("/overview")
    @Operation(summary = "Get platform overview statistics")
    public ResponseEntity<ApiResponse<AdminOverviewResponse>> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching admin platform overview from {} to {}", startDate, endDate);
        AdminOverviewResponse response = dashboardService.getOverview(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform detailed statistics and charts")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching admin platform statistics from {} to {}", startDate, endDate);
        AdminStatsResponse response = dashboardService.getStats(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get paginated transaction list for revenue analysis")
    public ResponseEntity<ApiResponse<Page<AdminStatsResponse.TransactionDetail>>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching admin transactions page {} size {} type {} from {} to {}",
                page, size, type, startDate, endDate);
        Page<AdminStatsResponse.TransactionDetail> response = 
                dashboardService.getPaginatedTransactions(page, size, type, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
