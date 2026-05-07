package com.sep.realvista.infrastructure.persistence.dashboard;

import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.dashboard.dto.DashboardAgentDTO;
import com.sep.realvista.application.dashboard.dto.DashboardPropertyItemDTO;
import com.sep.realvista.application.dashboard.dto.DashboardScheduleResponse;
import com.sep.realvista.application.dashboard.dto.DashboardStatsResponse;
import com.sep.realvista.application.dashboard.dto.FeaturedPropertyDTO;
import com.sep.realvista.application.dashboard.dto.OwnerHeroInsightsResponse;
import com.sep.realvista.application.dashboard.dto.PerformanceResponse;
import com.sep.realvista.application.dashboard.dto.PropertyOverviewResponse;
import com.sep.realvista.application.dashboard.dto.SalesAnalyticsResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DashboardQueryRepository {
    DashboardStatsResponse getStats(UUID ownerId);

    OwnerHeroInsightsResponse getHeroInsights(UUID ownerId);

    PerformanceResponse getPerformance(UUID ownerId, String period, String metric);

    FeaturedPropertyDTO getFeaturedProperty(UUID ownerId);

    SalesAnalyticsResponse getSalesAnalytics(UUID ownerId, String period);

    List<DashboardAgentDTO> getAgents(UUID ownerId, int limit);

    DashboardScheduleResponse getSchedules(UUID ownerId, LocalDate date, String type);

    PropertyOverviewResponse getPropertyOverview(UUID ownerId);

    PageResponse<DashboardPropertyItemDTO> getProperties(UUID ownerId,
                                                          String search,
                                                          String status,
                                                          int page,
                                                          int size,
                                                          String sortBy,
                                                          String sortDir);
}
