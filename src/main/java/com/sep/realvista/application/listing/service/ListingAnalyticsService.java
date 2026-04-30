package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.listing.dto.AgentPerformanceAnalyticsDTO;
import com.sep.realvista.application.listing.dto.AgentPerformanceChannelDTO;
import com.sep.realvista.application.listing.dto.AgentPerformanceTrendPointDTO;
import com.sep.realvista.application.listing.dto.ListingAnalyticsDTO;
import com.sep.realvista.domain.agent.lead.LeadSource;
import com.sep.realvista.domain.agent.lead.LeadStatus;
import com.sep.realvista.domain.agent.lead.ListingLeadRepository;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.analytics.ListingView;
import com.sep.realvista.domain.listing.analytics.ListingViewRepository;
import com.sep.realvista.domain.listing.appointment.Appointment;
import com.sep.realvista.domain.listing.repository.AppointmentRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service for listing analytics operations.
 * <p>
 * Handles view tracking and analytics aggregation for listings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ListingAnalyticsService {

    private final ListingViewRepository listingViewRepository;
    private final AppointmentRepository appointmentRepository;
    private final ListingRepository listingRepository;
    private final ListingLeadRepository listingLeadRepository;

    /**
     * Record a view for a listing by a user.
     * If the user has already viewed the listing, increment the view count.
     * Otherwise, create a new view record.
     * <p>
     * This method is asynchronous to avoid slowing down the listing detail response.
     *
     * @param listingId the listing ID
     * @param userId    the user ID
     */
    private static final UUID ANONYMOUS_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Async
    @Transactional
    public void recordView(UUID listingId, UUID userId) {
        try {
            UUID effectiveUserId = userId != null ? userId : ANONYMOUS_USER_ID;
            log.debug("Recording view for listing: {} by user: {}", listingId, effectiveUserId);

            Optional<ListingView> existingView = listingViewRepository
                    .findByListingIdAndUserId(listingId, effectiveUserId);

            if (existingView.isPresent()) {
                ListingView view = existingView.get();
                view.incrementViewCount();
                listingViewRepository.save(view);
                log.debug("Incremented view count to {} for listing: {} by user: {}",
                        view.getViewCount(), listingId, userId);
            } else {
                ListingView newView = ListingView.builder()
                        .listingId(listingId)
                        .userId(effectiveUserId)
                        .viewCount(1)
                        .build();
                listingViewRepository.save(newView);
                log.debug("Created new view record for listing: {} by user: {}", listingId, effectiveUserId);
            }
        } catch (Exception e) {
            log.error("Failed to record view for listing: {} by user: {}", 
                    listingId, userId != null ? userId : "anonymous", e);
            // Don't rethrow - view tracking failure should not affect user experience
        }
    }

    /**
     * Get aggregated analytics for a listing.
     * <p>
     * Calculates:
     * - Total views (sum of all view_count)
     * - Unique viewers (distinct users)
     * - Tour bookings (appointments with type=TOUR)
     * - Conversion rate (tour bookings / total views * 100)
     *
     * @param listingId the listing ID
     * @return the analytics DTO
     */
    @Transactional(readOnly = true)
    public ListingAnalyticsDTO getListingAnalytics(UUID listingId) {
        log.debug("Fetching analytics for listing: {}", listingId);

        // Get view metrics
        Integer totalViews = listingViewRepository.getTotalViewCountByListingId(listingId);
        Integer uniqueViewers = listingViewRepository.countDistinctUsersByListingId(listingId);

        // Get tour booking count
        long tourBookingsLong = appointmentRepository.findAll().stream()
                .filter(appointment -> listingId.equals(appointment.getListingId()))
                .filter(Appointment::isTour)
                .count();
        Integer tourBookings = (int) tourBookingsLong;

        // Calculate conversion rate
        BigDecimal conversionRate = BigDecimal.ZERO;
        if (totalViews > 0) {
            conversionRate = BigDecimal.valueOf(tourBookings)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalViews), 2, RoundingMode.HALF_UP);
        }

        log.debug("Analytics for listing {}: totalViews={}, uniqueViewers={}, tourBookings={}, conversionRate={}",
                listingId, totalViews, uniqueViewers, tourBookings, conversionRate);

        return ListingAnalyticsDTO.builder()
                .totalViews(totalViews)
                .uniqueViewers(uniqueViewers)
                .tourBookings(tourBookings)
                .conversionRate(conversionRate)
                .build();
    }

    @Transactional(readOnly = true)
    public AgentPerformanceAnalyticsDTO getAgentPerformanceAnalytics(UUID agentId, String period) {
        String normalizedPeriod = normalizePeriod(period);
        List<UUID> listingIds = listingRepository.findByUserIdOrPropertyOwnerId(agentId).stream()
                .map(Listing::getListingId)
                .toList();
        List<TimeBucket> buckets = buildBuckets(normalizedPeriod);

        List<AgentPerformanceTrendPointDTO> trend = buckets.stream()
                .map(bucket -> AgentPerformanceTrendPointDTO.builder()
                        .month(bucket.label())
                        .views(listingViewRepository.getTotalViewCountByListingIdsAndViewedAtBetween(
                                listingIds, bucket.from(), bucket.toExclusive()))
                        .inquiries(listingLeadRepository.countByAgentIdWithFilters(
                                agentId, null, bucket.from(), bucket.toExclusive(), null, null))
                        .closedDeals(listingLeadRepository.countByAgentIdWithFilters(
                                agentId, LeadStatus.CLOSED, bucket.from(), bucket.toExclusive(), null, null))
                        .build())
                .toList();

        LocalDateTime currentRangeFrom = buckets.get(0).from();
        LocalDateTime currentRangeTo = buckets.get(buckets.size() - 1).toExclusive();
        long totalInquiries = listingLeadRepository.countByAgentIdWithFilters(
                agentId, null, currentRangeFrom, currentRangeTo, null, null);
        List<Object[]> sourceCounts = listingLeadRepository.countBySourceWithFilters(
                agentId, currentRangeFrom, currentRangeTo, null, null);

        List<AgentPerformanceChannelDTO> channels = Arrays.stream(LeadSource.values())
                .map(source -> {
                    long leads = countForSource(sourceCounts, source);
                    int conversionRate = totalInquiries > 0
                            ? Math.toIntExact(Math.round((double) leads * 100 / totalInquiries))
                            : 0;
                    return AgentPerformanceChannelDTO.builder()
                            .channel(source.name().toLowerCase())
                            .leads(leads)
                            .conversionRate(conversionRate)
                            .build();
                })
                .toList();

        return AgentPerformanceAnalyticsDTO.builder()
                .period(normalizedPeriod)
                .trend(trend)
                .channels(channels)
                .build();
    }

    private String normalizePeriod(String period) {
        if (period == null || period.isBlank()) {
            return "M";
        }
        String value = period.trim().toUpperCase(Locale.ROOT);
        if ("W".equals(value) || "M".equals(value) || "Y".equals(value)) {
            return value;
        }
        return "M";
    }

    private List<TimeBucket> buildBuckets(String period) {
        if ("W".equals(period)) {
            return buildWeekBuckets(LocalDate.now());
        }
        if ("Y".equals(period)) {
            return buildYearBuckets(LocalDate.now());
        }
        return buildMonthBuckets(LocalDate.now());
    }

    private List<TimeBucket> buildWeekBuckets(LocalDate now) {
        LocalDate weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<TimeBucket> buckets = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            buckets.add(new TimeBucket(
                    day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    day.atStartOfDay(),
                    day.plusDays(1).atStartOfDay()));
        }
        return buckets;
    }

    private List<TimeBucket> buildMonthBuckets(LocalDate now) {
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate nextMonthStart = monthStart.plusMonths(1);
        List<TimeBucket> buckets = new ArrayList<>();
        LocalDate cursor = monthStart;
        int weekIndex = 1;

        while (cursor.isBefore(nextMonthStart)) {
            LocalDate bucketEnd = cursor.plusDays(7);
            if (bucketEnd.isAfter(nextMonthStart)) {
                bucketEnd = nextMonthStart;
            }
            buckets.add(new TimeBucket(
                    "W" + weekIndex,
                    cursor.atStartOfDay(),
                    bucketEnd.atStartOfDay()));
            cursor = bucketEnd;
            weekIndex++;
        }

        return buckets;
    }

    private List<TimeBucket> buildYearBuckets(LocalDate now) {
        int year = now.getYear();
        List<TimeBucket> buckets = new ArrayList<>();
        for (Month month : Month.values()) {
            LocalDate from = LocalDate.of(year, month, 1);
            LocalDate to = from.plusMonths(1);
            buckets.add(new TimeBucket(
                    month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    from.atStartOfDay(),
                    to.atStartOfDay()));
        }
        return buckets;
    }

    private long countForSource(List<Object[]> rows, LeadSource source) {
        return rows.stream()
                .filter(row -> row[0] == source)
                .map(row -> (Long) row[1])
                .findFirst()
                .orElse(0L);
    }

    private record TimeBucket(String label, LocalDateTime from, LocalDateTime toExclusive) {
    }
}
