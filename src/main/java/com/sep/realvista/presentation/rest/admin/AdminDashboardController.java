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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ApiResponse<AdminOverviewResponse>> getOverview() {
        log.info("Fetching admin platform overview");
        AdminOverviewResponse response = dashboardService.getOverview();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform detailed statistics and charts")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        log.info("Fetching admin platform statistics");
        AdminStatsResponse response = dashboardService.getStats();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
