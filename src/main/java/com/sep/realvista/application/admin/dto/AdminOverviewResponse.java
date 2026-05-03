package com.sep.realvista.application.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminOverviewResponse {
    private long totalUsers;
    private double totalRevenue;
    private long pendingListings;
    private long unresolvedReports;
    private long totalListings;
    private long totalBoosts;
    private long listingsCreatedToday;
    private long listingsInPeriod;
    private double revenueInPeriod;
    private long newUsersInPeriod;
}
