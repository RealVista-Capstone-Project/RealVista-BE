package com.sep.realvista.presentation.rest.dashboard;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.dashboard.dto.DashboardAgentDTO;
import com.sep.realvista.application.dashboard.dto.DashboardPropertyItemDTO;
import com.sep.realvista.application.dashboard.dto.DashboardScheduleResponse;
import com.sep.realvista.application.dashboard.dto.DashboardStatsResponse;
import com.sep.realvista.application.dashboard.dto.FeaturedPropertyDTO;
import com.sep.realvista.application.dashboard.dto.PerformanceResponse;
import com.sep.realvista.application.dashboard.dto.PropertyOverviewResponse;
import com.sep.realvista.application.dashboard.dto.SalesAnalyticsResponse;
import com.sep.realvista.application.dashboard.service.OwnerDashboardService;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Owner Dashboard", description = "Endpoints for owner dashboard widgets")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class OwnerDashboardController {

    private final OwnerDashboardService ownerDashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Get dashboard stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats(
            @AuthenticationPrincipal SecurityUserDetails userDetails
    ) {
        UUID ownerId = userDetails.getUserId();
        log.info("Dashboard stats request - ownerId: {}", ownerId);

        DashboardStatsResponse response = ownerDashboardService.getStats(ownerId);
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved successfully", response));
    }

    @GetMapping("/performance")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Get dashboard performance chart")
    public ResponseEntity<ApiResponse<PerformanceResponse>> getPerformance(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @RequestParam(defaultValue = "M") String period,
            @RequestParam(defaultValue = "revenue") String metric
    ) {
        UUID ownerId = userDetails.getUserId();
        log.info("Dashboard performance request - ownerId: {}, period: {}, metric: {}", ownerId, period, metric);

        PerformanceResponse response = ownerDashboardService.getPerformance(ownerId, period, metric);
        return ResponseEntity.ok(ApiResponse.success("Dashboard performance retrieved successfully", response));
    }

    @GetMapping("/featured-property")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Get featured property")
    public ResponseEntity<ApiResponse<FeaturedPropertyDTO>> getFeaturedProperty(
            @AuthenticationPrincipal SecurityUserDetails userDetails
    ) {
        UUID ownerId = userDetails.getUserId();
        log.info("Dashboard featured property request - ownerId: {}", ownerId);

        FeaturedPropertyDTO response = ownerDashboardService.getFeaturedProperty(ownerId);
        return ResponseEntity.ok(ApiResponse.success("Featured property retrieved successfully", response));
    }

    @GetMapping("/sales-analytics")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Get sales analytics")
    public ResponseEntity<ApiResponse<SalesAnalyticsResponse>> getSalesAnalytics(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @RequestParam(defaultValue = "month") String period
    ) {
        UUID ownerId = userDetails.getUserId();
        log.info("Dashboard sales analytics request - ownerId: {}, period: {}", ownerId, period);

        SalesAnalyticsResponse response = ownerDashboardService.getSalesAnalytics(ownerId, period);
        return ResponseEntity.ok(ApiResponse.success("Sales analytics retrieved successfully", response));
    }

    @GetMapping("/agents")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Get dashboard agents")
    public ResponseEntity<ApiResponse<List<DashboardAgentDTO>>> getAgents(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @RequestParam(defaultValue = "4") int limit
    ) {
        UUID ownerId = userDetails.getUserId();
        log.info("Dashboard agents request - ownerId: {}, limit: {}", ownerId, limit);

        List<DashboardAgentDTO> response = ownerDashboardService.getAgents(ownerId, limit);
        return ResponseEntity.ok(ApiResponse.success("Dashboard agents retrieved successfully", response));
    }

    @GetMapping("/schedules")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Get dashboard schedules")
    public ResponseEntity<ApiResponse<DashboardScheduleResponse>> getSchedules(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "all") String type
    ) {
        UUID ownerId = userDetails.getUserId();
        log.info("Dashboard schedules request - ownerId: {}, date: {}, type: {}", ownerId, date, type);

        DashboardScheduleResponse response = ownerDashboardService.getSchedules(ownerId, date, type);
        return ResponseEntity.ok(ApiResponse.success("Dashboard schedules retrieved successfully", response));
    }

    @GetMapping("/properties/overview")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Get property overview")
    public ResponseEntity<ApiResponse<PropertyOverviewResponse>> getPropertyOverview(
            @AuthenticationPrincipal SecurityUserDetails userDetails
    ) {
        UUID ownerId = userDetails.getUserId();
        log.info("Dashboard property overview request - ownerId: {}", ownerId);

        PropertyOverviewResponse response = ownerDashboardService.getPropertyOverview(ownerId);
        return ResponseEntity.ok(ApiResponse.success("Property overview retrieved successfully", response));
    }

    @GetMapping("/properties")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Get dashboard properties table")
    public ResponseEntity<ApiResponse<PageResponse<DashboardPropertyItemDTO>>> getProperties(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "All") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "cost") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        UUID ownerId = userDetails.getUserId();
        log.info("Dashboard properties request - ownerId: {}, search: {}, status: {}, page: {}, size: {}",
                ownerId, search, status, page, size);

        PageResponse<DashboardPropertyItemDTO> response = ownerDashboardService
                .getProperties(ownerId, search, status, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success("Dashboard properties retrieved successfully", response));
    }
}
