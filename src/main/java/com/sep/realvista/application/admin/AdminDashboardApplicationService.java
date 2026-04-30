package com.sep.realvista.application.admin;

import com.sep.realvista.application.admin.dto.AdminOverviewResponse;
import com.sep.realvista.application.admin.dto.AdminStatsResponse;
import com.sep.realvista.domain.billing.transaction.TransactionRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.report.ReportRepository;
import com.sep.realvista.domain.report.ReportStatus;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.UserStatus;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public AdminOverviewResponse getOverview() {
        return AdminOverviewResponse.builder()
                .totalUsers(userRepository.count())
                .totalRevenue(transactionRepository.sumTotalAmount())
                .pendingListings(listingRepository.countByStatus(ListingStatus.PENDING))
                .unresolvedReports(reportRepository.countByStatus(ReportStatus.PENDING))
                .totalListings(listingRepository.count())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ISO_DATE;

        List<Object[]> topAgentsRaw = listingRepository.findTopAgents(5);
        List<AdminStatsResponse.ChartData> topAgents = topAgentsRaw.stream()
                .map(row -> AdminStatsResponse.ChartData.builder()
                        .label((String) row[0])
                        .value(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        List<AdminStatsResponse.ChartData> userGrowth = new ArrayList<>();
        List<AdminStatsResponse.ChartData> revenueTrend = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            java.time.LocalDateTime startOfDay = now.minusDays(i).with(java.time.LocalTime.MIN);
            java.time.LocalDateTime endOfDay = now.minusDays(i).with(java.time.LocalTime.MAX);
            String label = startOfDay.format(formatter);

            // User Growth
            long userCount = userRepository.countByCreatedAtBetween(startOfDay, endOfDay);
            userGrowth.add(AdminStatsResponse.ChartData.builder().label(label).value(userCount).build());

            // Revenue Trend
            double revenue = transactionRepository.sumTotalAmountByCreatedAtBetween(startOfDay, endOfDay);
            revenueTrend.add(AdminStatsResponse.ChartData.builder().label(label).value(revenue).build());
        }

        List<AdminStatsResponse.ChartData> listingStatus = new ArrayList<>();
        listingStatus.add(AdminStatsResponse.ChartData.builder().label(ListingStatus.PUBLISHED.name())
                .value(listingRepository.countByStatus(ListingStatus.PUBLISHED)).build());
        listingStatus.add(AdminStatsResponse.ChartData.builder().label(ListingStatus.PENDING.name())
                .value(listingRepository.countByStatus(ListingStatus.PENDING)).build());
        listingStatus.add(AdminStatsResponse.ChartData.builder().label(ListingStatus.DRAFT.name())
                .value(listingRepository.countByStatus(ListingStatus.DRAFT)).build());
        listingStatus.add(AdminStatsResponse.ChartData.builder().label(ListingStatus.SOLD.name())
                .value(listingRepository.countByStatus(ListingStatus.SOLD)).build());
        listingStatus.add(AdminStatsResponse.ChartData.builder().label(ListingStatus.EXPIRED.name())
                .value(listingRepository.countByStatus(ListingStatus.EXPIRED)).build());

        Map<String, Double> moderationPerformance = new HashMap<>();

        // Calculate Approval Rate: (Published + Sold + Rented) / (Total - Draft)
        long published = listingRepository.countByStatus(ListingStatus.PUBLISHED);
        long sold = listingRepository.countByStatus(ListingStatus.SOLD);
        long rented = listingRepository.countByStatus(ListingStatus.RENTED);
        long pending = listingRepository.countByStatus(ListingStatus.PENDING);
        long expired = listingRepository.countByStatus(ListingStatus.EXPIRED);

        long totalNonDraft = published + sold + rented + pending + expired;
        double approvalRate = totalNonDraft > 0
            ? ((double) (published + sold + rented) / totalNonDraft) * 100.0
            : 0.0;

        // Calculate Avg Resolution Time (hours) for reports
        Double avgResTime = reportRepository.findAverageResolutionTimeInHours();

        moderationPerformance.put("approvalRate", Math.round(approvalRate * 10.0) / 10.0);
        moderationPerformance.put("avgResolutionTime", avgResTime != null ? Math.round(avgResTime * 10.0) / 10.0 : 0.0);
        moderationPerformance.put("moderatorEfficiency", 88.5); // Placeholder for now
        moderationPerformance.put("activeModerators", (double) userRepository.countByStatusAndCreatedAtAfter(
                UserStatus.ACTIVE,
                java.time.LocalDateTime.now().minusDays(1)));

        List<AdminStatsResponse.ActivityData> recentActivities = new ArrayList<>();

        // Add latest users
        userRepository.findTop10ByOrderByCreatedAtDesc().forEach(u ->
            recentActivities.add(AdminStatsResponse.ActivityData.builder()
                .id(UUID.randomUUID().toString())
                .type("USER")
                .description("New user registered: " + u.getEmail().getValue())
                .status(u.getStatus().toString())
                .timestamp(u.getCreatedAt())
                .targetId(u.getUserId().toString())
                .build())
        );

        // Add latest reports
        List<AdminStatsResponse.ActivityData> urgentReports = new ArrayList<>();
        reportRepository.findTop10ByOrderByCreatedAtDesc().forEach(r -> {
            AdminStatsResponse.ActivityData activity = AdminStatsResponse.ActivityData.builder()
                .id(UUID.randomUUID().toString())
                .type("REPORT")
                .description("New report: " + r.getReportReason())
                .status(r.getStatus().toString())
                .timestamp(r.getCreatedAt())
                .targetId(r.getReportId().toString())
                .build();

            recentActivities.add(activity);
            if (r.getStatus() == ReportStatus.PENDING) {
                urgentReports.add(activity);
            }
        });

        // Add latest transactions
        transactionRepository.findTop10ByOrderByCreatedAtDesc().forEach(t ->
            recentActivities.add(AdminStatsResponse.ActivityData.builder()
                .id(UUID.randomUUID().toString())
                .type("TRANSACTION")
                .description("New transaction: " + t.getAmount() + " VND")
                .status(t.getPaymentStatus().toString())
                .timestamp(t.getCreatedAt())
                .targetId(t.getTransactionId().toString())
                .build())
        );

        // Sort by timestamp desc and take top 10
        recentActivities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        List<AdminStatsResponse.ActivityData> topActivities = recentActivities.stream()
                .limit(10)
                .collect(java.util.stream.Collectors.toList());

        return AdminStatsResponse.builder()
                .userGrowth(userGrowth)
                .listingStatus(listingStatus)
                .revenueTrend(revenueTrend)
                .topAgents(topAgents)
                .recentActivities(topActivities)
                .topUrgentReports(urgentReports.stream().limit(4).collect(Collectors.toList()))
                .systemHealth(moderationPerformance)
                .build();
    }

}
