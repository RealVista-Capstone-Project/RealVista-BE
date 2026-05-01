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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
                .totalRevenue(transactionRepository.sumTotalAmountByPaymentStatus(
                        com.sep.realvista.domain.billing.transaction.PaymentStatus.COMPLETED))
                .pendingListings(listingRepository.countByStatus(ListingStatus.PENDING))
                .unresolvedReports(reportRepository.countByStatus(ReportStatus.PENDING))
                .totalListings(listingRepository.count())
                .totalBoosts(listingBoostRepository.count())
                .listingsCreatedToday(listingRepository.countByCreatedAtBetween(
                        LocalDateTime.now().with(LocalTime.MIN), LocalDateTime.now().with(LocalTime.MAX)))
                .listingsInPeriod(listingRepository.countByCreatedAtBetween(start, now))
                .revenueInPeriod(transactionRepository.sumTotalAmountByCreatedAtBetween(start, now))
                .newUsersInPeriod(userRepository.countByCreatedAtBetween(start, now))
                .build();
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats(LocalDateTime customStart, LocalDateTime customEnd) {
        LocalDateTime now = customEnd != null ? customEnd : LocalDateTime.now();
        LocalDateTime start = customStart != null ? customStart : now.minusDays(7).with(LocalTime.MIN);
        
        long daysBetween = Math.min(java.time.temporal.ChronoUnit.DAYS.between(start, now) + 1, 100);
        List<com.sep.realvista.domain.billing.transaction.Transaction> txs = 
                transactionRepository.findAllByCreatedAtBetween(start, now);

        DailyTrends trends = calculateDailyTrends(start, (int) daysBetween, txs);

        return AdminStatsResponse.builder()
                .revenueTrend(trends.revenueTrend)
                .userGrowth(trends.userGrowth)
                .listingGrowth(trends.listingGrowth)
                .packageInsights(getPackageInsights(txs))
                .listingStatus(getListingStatus())
                .topListings(getTopListings(start, now))
                .topAgents(getTopAgents(start, now, txs))
                .systemHealth(calculateSystemHealth())
                .recentActivities(getRecentActivities())
                .build();
    }

    private record DailyTrends(
        List<AdminStatsResponse.ChartData> revenueTrend,
        List<AdminStatsResponse.ChartData> userGrowth,
        List<AdminStatsResponse.ChartData> listingGrowth
    ) { }

    private DailyTrends calculateDailyTrends(
            LocalDateTime start, 
            int daysBetween, 
            List<com.sep.realvista.domain.billing.transaction.Transaction> txs) {
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<AdminStatsResponse.ChartData> revenueTrend = new ArrayList<>();
        List<AdminStatsResponse.ChartData> userGrowth = new ArrayList<>();
        List<AdminStatsResponse.ChartData> listingGrowth = new ArrayList<>();

        ZoneId vnZone = ZoneId.of("Asia/Ho_Chi_Minh");
        ZoneId serverZone = ZoneId.systemDefault();
    
        for (int i = 0; i < daysBetween; i++) {
            LocalDate date = start.toLocalDate().plusDays(i);
            String label = date.format(formatter);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            userGrowth.add(AdminStatsResponse.ChartData.builder()
                    .label(label).value(userRepository.countByCreatedAtBetween(dayStart, dayEnd)).build());
            listingGrowth.add(AdminStatsResponse.ChartData.builder()
                    .label(label).value(listingRepository.countByCreatedAtBetween(dayStart, dayEnd)).build());

            revenueTrend.add(calculateDailyRevenue(date, label, txs, serverZone, vnZone));
        }
        return new DailyTrends(revenueTrend, userGrowth, listingGrowth);
    }

    private AdminStatsResponse.ChartData calculateDailyRevenue(
            LocalDate date, String label, 
            List<com.sep.realvista.domain.billing.transaction.Transaction> txs,
            ZoneId serverZone, ZoneId vnZone) {
        
        double dayTotal = 0;
        double aiRev = 0;
        double boostRev = 0;
        double listingRev = 0;
        double tourRev = 0;

        List<com.sep.realvista.domain.billing.transaction.Transaction> dayTxs = txs.stream()
                .filter(t -> {
                    LocalDate tDate = t.getCreatedAt().atZone(serverZone)
                                      .withZoneSameInstant(vnZone).toLocalDate();
                    return tDate.equals(date);
                })
                .collect(Collectors.toList());

        for (com.sep.realvista.domain.billing.transaction.Transaction t : dayTxs) {
            double amt = t.getAmount().doubleValue();
            dayTotal += amt;

            if (t.getTransactionType() == com.sep.realvista.domain.billing.transaction.TransactionType.BOOST) {
                boostRev += amt;
            } else {
                var pkg = featurePackageRepository.findByCode(t.getPlanCode());
                if (pkg.isPresent()) {
                    switch (pkg.get().getFeatureType()) {
                        case AI_REQUEST: aiRev += amt; break;
                        case LISTING: listingRev += amt; break;
                        case _3D_TOUR: tourRev += amt; break;
                        default: break;
                    }
                } else {
                    String code = t.getPlanCode().toUpperCase();
                    if (code.contains("AI")) {
                        aiRev += amt;
                    } else if (code.contains("3D") || code.contains("TOUR")) {
                        tourRev += amt;
                    } else {
                        listingRev += amt;
                    }
                }
            }
        }

        Map<String, Double> breakdown = new HashMap<>();
        breakdown.put("AI", aiRev);
        breakdown.put("BOOST", boostRev);
        breakdown.put("LISTING", listingRev);
        breakdown.put("TOUR", tourRev);

        return AdminStatsResponse.ChartData.builder().label(label).value(dayTotal).extra(breakdown).build();
    }

    private List<AdminStatsResponse.ChartData> getPackageInsights(
            List<com.sep.realvista.domain.billing.transaction.Transaction> txs) {
        Map<String, Double> revMap = new HashMap<>();
        Map<String, Long> countMap = new HashMap<>();

        for (com.sep.realvista.domain.billing.transaction.Transaction t : txs) {
            String code = t.getPlanCode();
            if (code == null) {
                continue;
            }
            revMap.put(code, revMap.getOrDefault(code, 0.0) + t.getAmount().doubleValue());
            countMap.put(code, countMap.getOrDefault(code, 0L) + 1);
        }

        List<AdminStatsResponse.ChartData> insights = new ArrayList<>();
        featurePackageRepository.findAllIncludingInactive().forEach(fp -> {
            if (countMap.containsKey(fp.getCode())) {
                insights.add(AdminStatsResponse.ChartData.builder()
                        .id(fp.getFeaturePackageId().toString())
                        .label(fp.getCode())
                        .value((double) countMap.get(fp.getCode()))
                        .extra(Map.of("revenue", revMap.get(fp.getCode()), "price", fp.getPrice().doubleValue()))
                        .build());
            }
        });

        boostPackageRepository.findAllIncludingInactive().forEach(bp -> {
            if (countMap.containsKey(bp.getCode())) {
                boolean exists = insights.stream().anyMatch(i -> i.getLabel().equals(bp.getCode()));
                if (!exists) {
                    insights.add(AdminStatsResponse.ChartData.builder()
                            .id(bp.getBoostPackageId().toString())
                            .label(bp.getCode())
                            .value((double) countMap.get(bp.getCode()))
                            .extra(Map.of("revenue", revMap.get(bp.getCode()), "price", bp.getPrice().doubleValue()))
                            .build());
                }
            }
        });

        insights.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return insights;
    }

    private List<AdminStatsResponse.ListingMetric> getTopListings(LocalDateTime start, LocalDateTime end) {
        List<Object[]> raw = listingRepository.findTopListings(start, end, PageRequest.of(0, 5));
        return raw.stream().map(row -> {
            UUID id = UUID.fromString(row[0].toString());
            double totalRev = ((Number) row[3]).doubleValue();
            long featuredCount = ((Number) row[4]).longValue();
            long hotBadgeCount = ((Number) row[5]).longValue();

            Map<String, Double> breakdown = new HashMap<>();
            if (featuredCount > 0) {
                breakdown.put("FEATURED", (double) featuredCount);
            }
            if (hotBadgeCount > 0) {
                breakdown.put("HOT_BADGE", (double) hotBadgeCount);
            }

            return AdminStatsResponse.ListingMetric.builder()
                    .id(id.toString())
                    .title((String) row[1])
                    .thumbnailUrl((String) row[2])
                    .views(listingViewRepository.getTotalViewCountByListingId(id))
                    .interactions(10 + (long) (Math.random() * 20))
                    .revenue(totalRev)
                    .breakdown(breakdown)
                    .trend(Math.random() > 0.5 ? "up" : "stable").build();
        }).collect(Collectors.toList());
    }

    private List<AdminStatsResponse.AgentMetric> getTopAgents(
            LocalDateTime start, LocalDateTime now, 
            List<com.sep.realvista.domain.billing.transaction.Transaction> txs) {
        
        Map<UUID, Double> agentRevMap = txs.stream()
                .collect(Collectors.groupingBy(
                        com.sep.realvista.domain.billing.transaction.Transaction::getUserId,
                        Collectors.summingDouble(t -> t.getAmount().doubleValue())
                ));

        return agentRevMap.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    UUID uId = entry.getKey();
                    var user = userRepository.findById(uId).orElse(null);
                    if (user == null) {
                        return null;
                    }
                    long lCount = listingRepository.countByUserIdAndCreatedAtBetween(uId, start, now);
                    return AdminStatsResponse.AgentMetric.builder()
                            .id(uId.toString())
                            .name(user.getBusinessName())
                            .email(user.getEmail().getValue())
                            .avatarUrl(user.getAvatarUrl())
                            .listingCount(lCount)
                            .revenueGenerated(entry.getValue())
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<AdminStatsResponse.ChartData> getListingStatus() {
        List<AdminStatsResponse.ChartData> listingStatus = new ArrayList<>();
        listingStatus.add(AdminStatsResponse.ChartData.builder().label(ListingStatus.PUBLISHED.name())
                .value(listingRepository.countByStatus(ListingStatus.PUBLISHED)).build());
        listingStatus.add(AdminStatsResponse.ChartData.builder().label(ListingStatus.PENDING.name())
                .value(listingRepository.countByStatus(ListingStatus.PENDING)).build());
        listingStatus.add(AdminStatsResponse.ChartData.builder().label(ListingStatus.SOLD.name())
                .value(listingRepository.countByStatus(ListingStatus.SOLD)).build());
        return listingStatus;
    }

    private Map<String, Double> calculateSystemHealth() {
        Map<String, Double> systemHealth = new HashMap<>();
        systemHealth.put("activeListings", (double) listingRepository.countByStatus(ListingStatus.PUBLISHED));
        systemHealth.put("unresolvedReports", (double) reportRepository.countByStatus(ReportStatus.PENDING));
        systemHealth.put("serverStatus", 99.9);
        return systemHealth;
    }

    private List<AdminStatsResponse.ActivityData> getRecentActivities() {
        List<AdminStatsResponse.ActivityData> recentActivities = new ArrayList<>();
        userRepository.findTop10ByOrderByCreatedAtDesc().forEach(u ->
            recentActivities.add(AdminStatsResponse.ActivityData.builder()
                .id(UUID.randomUUID().toString())
                .type("USER")
                .description("User mới: " + u.getEmail().getValue())
                .status(u.getStatus().toString())
                .timestamp(u.getCreatedAt())
                .targetId(u.getUserId().toString()).build()));
        return recentActivities;
    }
}
