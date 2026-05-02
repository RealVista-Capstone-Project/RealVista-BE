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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardApplicationService {
    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(AdminDashboardApplicationService.class);

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
                .detailedTransactions(getDetailedTransactions(txs.stream().limit(10).collect(Collectors.toList())))
                .build();
    }

@Transactional(readOnly = true)
public Page<AdminStatsResponse.TransactionDetail> getPaginatedTransactions(
        int page, int size, String type, LocalDateTime start, LocalDateTime end) {
    
    LocalDateTime now = end != null ? end : LocalDateTime.now();
    LocalDateTime startTime = start != null ? start : now.minusDays(7).with(LocalTime.MIN);
    
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    
    // Sanitize input type
    final String sanitizedType = (type != null) ? type.trim().toUpperCase() : "ALL";
    List<String> targetPlanCodes = (!sanitizedType.equals("ALL")) ? getPlanCodesForType(sanitizedType) : null;
    LOGGER.info("Filtering transactions by type: {}, planCodes: {}", sanitizedType, targetPlanCodes);

    Page<com.sep.realvista.domain.billing.transaction.Transaction> txPage;
    if (!sanitizedType.equals("ALL")) {
        if (targetPlanCodes != null && !targetPlanCodes.isEmpty()) {
            txPage = transactionRepository.findAllByCreatedAtBetweenAndPlanCodeInPaged(
                    startTime, now, targetPlanCodes, pageable);
        } else {
            txPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
    } else {
        txPage = transactionRepository.findAllByCreatedAtBetweenPaged(startTime, now, pageable);
    }

    Map<String, String> planNames = new HashMap<>();
    Map<String, String> planTypes = new HashMap<>();
    featurePackageRepository.findAllIncludingInactive().forEach(fp -> {
        planNames.put(fp.getCode(), fp.getName());
        planTypes.put(fp.getCode(), fp.getFeatureType().name());
    });
    boostPackageRepository.findAllIncludingInactive().forEach(bp -> {
        planNames.put(bp.getCode(), bp.getName());
        planTypes.put(bp.getCode(), "BOOST");
    });

    List<AdminStatsResponse.TransactionDetail> details = txPage.getContent().stream()
            .map((com.sep.realvista.domain.billing.transaction.Transaction t) -> {
                var user = userRepository.findById(t.getUserId()).orElse(null);
                String rawType = planTypes.getOrDefault(t.getPlanCode(), t.getTransactionType().name());
                
                // Robust normalization for frontend consistency
                String derivedType = rawType.replace("_PACKAGE", "").replace("_REQUEST", "");
                String normalizedType = derivedType.startsWith("_") ? derivedType.substring(1) : derivedType;
                
                // Special case for AI_REQUEST -> AI
                if ("AI".equalsIgnoreCase(normalizedType)) {
                    normalizedType = "AI";
                }

                return AdminStatsResponse.TransactionDetail.builder()
                        .id(t.getTransactionId().toString())
                        .userName(user != null ? user.getBusinessName() : "Unknown")
                        .userEmail(user != null ? user.getEmail().getValue() : "N/A")
                        .userAvatar(user != null ? user.getAvatarUrl() : null)
                        .type(normalizedType)
                        .planName(planNames.getOrDefault(t.getPlanCode(), t.getPlanCode()))
                        .amount(t.getAmount().doubleValue())
                        .timestamp(t.getCreatedAt())
                        .status(t.getPaymentStatus().name())
                        .build();
            })
            .collect(Collectors.toList());

    return new PageImpl<>(details, pageable, txPage.getTotalElements());
}

private List<String> getPlanCodesForType(String type) {
    List<String> codes = new ArrayList<>();
    LOGGER.info("Collecting plan codes for category: {}", type);
    
    if ("BOOST".equalsIgnoreCase(type)) {
        boostPackageRepository.findAllIncludingInactive().forEach(p -> codes.add(p.getCode()));
    } else {
        featurePackageRepository.findAllIncludingInactive().forEach(p -> {
            String rawEnumName = p.getFeatureType().name();
            // Handle both _3D_TOUR and 3D_TOUR (and AI_REQUEST -> AI)
            String derivedType = rawEnumName.replace("_PACKAGE", "").replace("_REQUEST", "");
            String normalizedDerived = derivedType.startsWith("_") ? derivedType.substring(1) : derivedType;
            String normalizedInput = type.startsWith("_") ? type.substring(1) : type;

            if (normalizedDerived.equalsIgnoreCase(normalizedInput)) {
                codes.add(p.getCode());
            }
        });
    }
    LOGGER.info("Found {} codes for category {}: {}", codes.size(), type, codes);
    return codes;
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
                        .label(fp.getName())
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
                            .label(bp.getName())
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
        return raw.stream().map((Object[] row) -> {
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
                    .has3dTour(listingRepository.has3dTour(id))
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
                .map((java.util.Map.Entry<UUID, Double> entry) -> {
                    UUID uId = entry.getKey();
                    var user = userRepository.findById(uId).orElse(null);
                    if (user == null) {
                        return (AdminStatsResponse.AgentMetric) null;
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

    private List<AdminStatsResponse.TransactionDetail> getDetailedTransactions(
            List<com.sep.realvista.domain.billing.transaction.Transaction> txs) {
        Map<String, String> planNames = new HashMap<>();
        Map<String, String> planTypes = new HashMap<>();

        featurePackageRepository.findAllIncludingInactive().forEach(fp -> {
            planNames.put(fp.getCode(), fp.getName());
            planTypes.put(fp.getCode(), fp.getFeatureType().name());
        });
        boostPackageRepository.findAllIncludingInactive().forEach(bp -> {
            planNames.put(bp.getCode(), bp.getName());
            planTypes.put(bp.getCode(), "BOOST");
        });

        return txs.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(50)
                .map((com.sep.realvista.domain.billing.transaction.Transaction t) -> {
                    var user = userRepository.findById(t.getUserId()).orElse(null);
                    String rawType = planTypes.getOrDefault(t.getPlanCode(), t.getTransactionType().name());
                    String type = rawType.replace("_PACKAGE", "").replace("_REQUEST", "");

                    return AdminStatsResponse.TransactionDetail.builder()
                            .id(t.getTransactionId().toString())
                            .userName(user != null ? user.getBusinessName() : "Unknown")
                            .userEmail(user != null ? user.getEmail().getValue() : "N/A")
                            .userAvatar(user != null ? user.getAvatarUrl() : null)
                            .type(type)
                            .planName(planNames.getOrDefault(t.getPlanCode(), t.getPlanCode()))
                            .amount(t.getAmount().doubleValue())
                            .timestamp(t.getCreatedAt())
                            .status(t.getPaymentStatus().name())
                            .build();
                })
                .collect(Collectors.toList());
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
