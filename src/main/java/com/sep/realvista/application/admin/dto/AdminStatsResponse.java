package com.sep.realvista.application.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private List<ChartData> userGrowth;
    private List<ChartData> listingGrowth;
    private List<ChartData> listingStatus;
    private List<ChartData> revenueTrend;
    private List<ListingMetric> topListings;
    private List<ChartData> packageInsights;
    private List<AgentMetric> topAgents;
    private List<ActivityData> recentActivities;
    private List<TransactionDetail> detailedTransactions;
    private Map<String, Double> systemHealth;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        private String id;
        private String label;
        private double value;
        private Map<String, Double> extra;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDetail {
        private String id;
        private String userName;
        private String userEmail;
        private String userAvatar;
        private String type; // BOOST, LISTING, 3D_TOUR, AI
        private String planName;
        private double amount;
        private LocalDateTime timestamp;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListingMetric {
        private String id;
        private String title;
        private String thumbnailUrl;
        private long views;
        private long interactions;
        private double revenue;
        private Map<String, Double> breakdown;
        private boolean has3dTour;
        private String trend; // "up", "down", "stable"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentMetric {
        private String id;
        private String name;
        private String email;
        private String avatarUrl;
        private long listingCount;
        private double revenueGenerated;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityData {
        private String id;
        private String type;
        private String description;
        private String status;
        private LocalDateTime timestamp;
        private String targetId;
    }
}
