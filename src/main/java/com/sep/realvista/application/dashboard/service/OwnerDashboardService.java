package com.sep.realvista.application.dashboard.service;

import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.dashboard.dto.DashboardAgentDTO;
import com.sep.realvista.application.dashboard.dto.DashboardPropertyItemDTO;
import com.sep.realvista.application.dashboard.dto.DashboardScheduleResponse;
import com.sep.realvista.application.dashboard.dto.DashboardStatsResponse;
import com.sep.realvista.application.dashboard.dto.FeaturedPropertyDTO;
import com.sep.realvista.application.dashboard.dto.PerformanceResponse;
import com.sep.realvista.application.dashboard.dto.PropertyOverviewResponse;
import com.sep.realvista.application.dashboard.dto.SalesAnalyticsResponse;
import com.sep.realvista.infrastructure.persistence.dashboard.DashboardQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OwnerDashboardService {

    private final DashboardQueryRepository dashboardQueryRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats(UUID ownerId) {
        return dashboardQueryRepository.getStats(ownerId);
    }

    @Transactional(readOnly = true)
    public PerformanceResponse getPerformance(UUID ownerId, String period, String metric) {
        return dashboardQueryRepository.getPerformance(ownerId, period, metric);
    }

    @Transactional(readOnly = true)
    public FeaturedPropertyDTO getFeaturedProperty(UUID ownerId) {
        return dashboardQueryRepository.getFeaturedProperty(ownerId);
    }

    @Transactional(readOnly = true)
    public SalesAnalyticsResponse getSalesAnalytics(UUID ownerId, String period) {
        return dashboardQueryRepository.getSalesAnalytics(ownerId, period);
    }

    @Transactional(readOnly = true)
    public List<DashboardAgentDTO> getAgents(UUID ownerId, int limit) {
        return dashboardQueryRepository.getAgents(ownerId, limit);
    }

    @Transactional(readOnly = true)
    public DashboardScheduleResponse getSchedules(UUID ownerId, LocalDate date, String type) {
        return dashboardQueryRepository.getSchedules(ownerId, date, type);
    }

    @Transactional(readOnly = true)
    public PropertyOverviewResponse getPropertyOverview(UUID ownerId) {
        return dashboardQueryRepository.getPropertyOverview(ownerId);
    }

    @Transactional(readOnly = true)
    public PageResponse<DashboardPropertyItemDTO> getProperties(UUID ownerId,
                                                                 String search,
                                                                 String status,
                                                                 int page,
                                                                 int size,
                                                                 String sortBy,
                                                                 String sortDir) {
        return dashboardQueryRepository.getProperties(ownerId, search, status, page, size, sortBy, sortDir);
    }
}
