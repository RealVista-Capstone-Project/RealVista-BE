package com.sep.realvista.application.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private Long totalViews;
    private BigDecimal totalViewsTrend;
    private Long activeListing;
    private BigDecimal activeListingTrend;
    private Long totalClosed;
    private BigDecimal totalClosedTrend;
    private Long activeLeads;
    private BigDecimal activeLeadsTrend;
    private Long onProgress;
    private Long closedDeals;
}
