package com.sep.realvista.application.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class AdminStatsResponse {
    private List<ChartData> userGrowth;
    private List<ChartData> listingGrowth;
    private List<ChartData> listingStatus;
    private List<ChartData> revenueTrend;
    private List<ListingMetric> topListings;
    private List<ChartData> packageInsights;
    private List<AgentMetric> topAgents;
    private List<ActivityData> recentActivities;
    private List<ActivityData> topUrgentReports;
    private Map<String, Double> systemHealth;

    @Getter
    @Builder
    public static class ChartData {
        private String id;
        private String label;
        private double value;
        private Map<String, Double> extra;
    }

    @Getter
    @Builder
    public static class ListingMetric {
        private String id;
        private String title;
        private String thumbnailUrl;
        private long views;
        private long interactions;
        private double revenue;
        private Map<String, Double> breakdown;
        private String trend; // "up", "down", "stable"
    }

    @Getter
    @Builder
    public static class AgentMetric {
        private String id;
        private String name;
        private String email;
        private String avatarUrl;
        private long listingCount;
        private double revenueGenerated;
    }

    @Getter
    @Builder
    public static class ActivityData {
        private String id;
        private String type;
        private String description;
        private String status;
        private LocalDateTime timestamp;
        private String targetId;
    }
}
