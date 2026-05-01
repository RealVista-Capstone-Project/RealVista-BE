package com.sep.realvista.application.admin;

import com.sep.realvista.application.admin.dto.AdminOverviewResponse;
import com.sep.realvista.application.admin.dto.AdminStatsResponse;
import com.sep.realvista.domain.billing.boost.repository.BoostPackageRepository;
import com.sep.realvista.domain.billing.boost.repository.ListingBoostRepository;
import com.sep.realvista.domain.billing.subscription.repository.FeaturePackageRepository;
import com.sep.realvista.domain.billing.subscription.repository.UserFeatureSubscriptionRepository;
import com.sep.realvista.domain.billing.transaction.TransactionRepository;
import com.sep.realvista.domain.listing.analytics.ListingViewRepository;
import com.sep.realvista.domain.listing.bookmark.BookmarkRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.report.ReportRepository;
import com.sep.realvista.domain.report.ReportStatus;
import com.sep.realvista.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardApplicationService {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final ReportRepository reportRepository;
    private final TransactionRepository transactionRepository;
    private final ListingBoostRepository listingBoostRepository;
    private final UserFeatureSubscriptionRepository userFeatureSubscriptionRepository;
    private final FeaturePackageRepository featurePackageRepository;
    private final BoostPackageRepository boostPackageRepository;
    private final ListingViewRepository listingViewRepository;
    private final BookmarkRepository bookmarkRepository;

    @Transactional(readOnly = true)
    public AdminOverviewResponse getOverview(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime now = endDate != null ? endDate : LocalDateTime.now();
        LocalDateTime start = startDate != null ? startDate : now.minusDays(7).with(LocalTime.MIN);

        return AdminOverviewResponse.builder()
                .totalUsers(userRepository.count())
                .totalRevenue(listingBoostRepository.sumTotalRevenue() 
                        + userFeatureSubscriptionRepository.sumTotalRevenue())
                .pendingListings(listingRepository.countByStatus(ListingStatus.PENDING))
                .unresolvedReports(reportRepository.countByStatus(ReportStatus.PENDING))
                .totalListings(listingRepository.count())
                .totalBoosts(listingBoostRepository.count())
                .listingsCreatedToday(listingRepository.countByCreatedAtBetween(
                        LocalDateTime.now().with(LocalTime.MIN), LocalDateTime.now().with(LocalTime.MAX)))
                .listingsInPeriod(listingRepository.countByCreatedAtBetween(start, now))
                .revenueInPeriod(listingBoostRepository.sumTotalRevenueBetween(start, now)
                        + userFeatureSubscriptionRepository.sumTotalRevenueBetween(start, now))
                .newUsersInPeriod(userRepository.countByCreatedAtBetween(start, now))
                .build();
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats(LocalDateTime customStart, LocalDateTime customEnd) {
        LocalDateTime now = customEnd != null ? customEnd : LocalDateTime.now();
        LocalDateTime start = customStart != null ? customStart : now.minusDays(7).with(LocalTime.MIN);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, now) + 1;
        if (daysBetween > 100) {
            daysBetween = 100;
        }

        // 1. Revenue, User Growth & Listing Growth Trend
        List<AdminStatsResponse.ChartData> revenueTrend = new ArrayList<>();
        List<AdminStatsResponse.ChartData> userGrowth = new ArrayList<>();
        List<AdminStatsResponse.ChartData> listingGrowth = new ArrayList<>();

        for (int i = 0; i < daysBetween; i++) {
            LocalDateTime date = start.plusDays(i);
            String label = date.format(formatter);
            
            LocalDateTime dayStart = date.with(LocalTime.MIN);
            LocalDateTime dayEnd = date.with(LocalTime.MAX);

            // User Growth
            long userCount = userRepository.countByCreatedAtBetween(dayStart, dayEnd);
            userGrowth.add(AdminStatsResponse.ChartData.builder().label(label).value(userCount).build());

            // Listing Growth (Created Listings)
            long listingCount = listingRepository.countByCreatedAtBetween(dayStart, dayEnd);
            listingGrowth.add(AdminStatsResponse.ChartData.builder().label(label).value(listingCount).build());

            // Revenue Trend from Boosts + Subscriptions
            double boostRev = listingBoostRepository.sumTotalRevenueBetween(dayStart, dayEnd);
            double subRev = userFeatureSubscriptionRepository.sumTotalRevenueBetween(dayStart, dayEnd);
            double totalDayRevenue = boostRev + subRev;

            Map<String, Double> breakdown = new HashMap<>();
            breakdown.put("BOOST", boostRev);
            breakdown.put("SUBSCRIPTION", subRev);

            revenueTrend.add(AdminStatsResponse.ChartData.builder()
                    .label(label)
                    .value(totalDayRevenue)
                    .extra(breakdown)
                    .build());
        }

        // 2. Package Insights (Active Subscriptions & Boosts)
        List<AdminStatsResponse.ChartData> limitedPackageInsights = getPackageInsights(start, now);

        // 3. Top Listings with Metrics
        List<AdminStatsResponse.ListingMetric> topListings = getTopListings(start, now);

        // 4. Top Active Agents
        List<Object[]> topAgentsRaw = listingRepository.findTopAgents(start, now, PageRequest.of(0, 5));
        List<AdminStatsResponse.AgentMetric> topAgents = topAgentsRaw.stream()
                .map(row -> AdminStatsResponse.AgentMetric.builder()
                        .id(row[0].toString())
                        .name((String) row[1])
                        .email((String) row[2])
                        .avatarUrl((String) row[3])
                        .listingCount(((Number) row[4]).longValue())
                        .revenueGenerated(((Number) row[5]).doubleValue()) 
                        // Real revenue from database (index 5 from query)
                        .build())
                .collect(Collectors.toList());

        // 5. Listing Status (Pie chart)
        List<AdminStatsResponse.ChartData> listingStatus = new ArrayList<>();
        listingStatus.add(AdminStatsResponse.ChartData.builder().label(ListingStatus.PUBLISHED.name())
                .value(listingRepository.countByStatus(ListingStatus.PUBLISHED)).build());
        listingStatus.add(AdminStatsResponse.ChartData.builder().label(ListingStatus.PENDING.name())
                .value(listingRepository.countByStatus(ListingStatus.PENDING)).build());
        listingStatus.add(AdminStatsResponse.ChartData.builder().label(ListingStatus.SOLD.name())
                .value(listingRepository.countByStatus(ListingStatus.SOLD)).build());

        // 6. System Health
        Map<String, Double> systemHealth = new HashMap<>();
        systemHealth.put("activeListings", (double) listingRepository.countByStatus(ListingStatus.PUBLISHED));
        systemHealth.put("unresolvedReports", (double) reportRepository.countByStatus(ReportStatus.PENDING));
        systemHealth.put("serverStatus", 99.9);

        // 7. Recent Activities (Activity Feed)
        List<AdminStatsResponse.ActivityData> recentActivities = new ArrayList<>();
        userRepository.findTop10ByOrderByCreatedAtDesc().forEach(u ->
            recentActivities.add(AdminStatsResponse.ActivityData.builder()
                .id(UUID.randomUUID().toString())
                .type("USER")
                .description("User mới: " + u.getEmail().getValue())
                .status(u.getStatus().toString())
                .timestamp(u.getCreatedAt())
                .targetId(u.getUserId().toString()).build()));

        transactionRepository.findTop10ByOrderByCreatedAtDesc().forEach(t ->
            recentActivities.add(AdminStatsResponse.ActivityData.builder()
                .id(UUID.randomUUID().toString())
                .type("TRANSACTION")
                .description("Thanh toán: " + t.getPlanCode() + " (" + t.getAmount() + " VND)")
                .status(t.getPaymentStatus().toString())
                .timestamp(t.getCreatedAt())
                .targetId(t.getTransactionId().toString()).build()));

        recentActivities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return AdminStatsResponse.builder()
                .userGrowth(userGrowth)
                .listingGrowth(listingGrowth)
                .listingStatus(listingStatus)
                .revenueTrend(revenueTrend)
                .topListings(topListings)
                .packageInsights(limitedPackageInsights)
                .topAgents(topAgents)
                .recentActivities(recentActivities.stream().limit(10).collect(Collectors.toList()))
                .topUrgentReports(new ArrayList<>()) // Simplified
                .systemHealth(systemHealth)
                .build();
    }

    private List<AdminStatsResponse.ChartData> getPackageInsights(LocalDateTime start, LocalDateTime end) {
        List<AdminStatsResponse.ChartData> insights = new ArrayList<>();
        featurePackageRepository.findAllActive().forEach(fp -> {
            long count = userFeatureSubscriptionRepository.countActiveByFeaturePackageId(fp.getFeaturePackageId());
            if (count > 0) {
                double revenue = userFeatureSubscriptionRepository.sumRevenueByPackageAndPeriod(
                        fp.getFeaturePackageId(), start, end);
                insights.add(AdminStatsResponse.ChartData.builder()
                        .id(fp.getFeaturePackageId().toString())
                        .label(fp.getCode())
                        .value((double) count)
                        .extra(Map.of("revenue", revenue, "price", fp.getPrice().doubleValue())).build());
            }
        });
        boostPackageRepository.findAllActive().forEach(bp -> {
            long count = listingBoostRepository.countActiveByBoostPackageId(bp.getBoostPackageId());
            if (count > 0) {
                double revenue = listingBoostRepository.sumRevenueByPackageAndPeriod(
                        bp.getBoostPackageId(), start, end);
                insights.add(AdminStatsResponse.ChartData.builder()
                        .id(bp.getBoostPackageId().toString())
                        .label(bp.getCode())
                        .value((double) count)
                        .extra(Map.of("revenue", revenue, "price", bp.getPrice().doubleValue())).build());
            }
        });
        insights.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return insights.stream().limit(5).collect(Collectors.toList());
    }

    private List<AdminStatsResponse.ListingMetric> getTopListings(LocalDateTime start, LocalDateTime end) {
        List<Object[]> raw = listingRepository.findTopListings(start, end, PageRequest.of(0, 5));
        return raw.stream().map(row -> {
            UUID id = UUID.fromString(row[0].toString());
            return AdminStatsResponse.ListingMetric.builder()
                    .id(id.toString())
                    .title((String) row[1])
                    .thumbnailUrl((String) row[2])
                    .views(listingViewRepository.getTotalViewCountByListingId(id))
                    .interactions(10 + (long) (Math.random() * 20))
                    .revenue(((Number) row[3]).doubleValue())
                    .trend(Math.random() > 0.5 ? "up" : "stable").build();
        }).collect(Collectors.toList());
    }
}
