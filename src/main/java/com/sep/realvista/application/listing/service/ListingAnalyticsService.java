package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.listing.dto.AgentListingAnalyticsRowDTO;
import com.sep.realvista.application.listing.dto.AgentPerformanceAnalyticsDTO;
import com.sep.realvista.application.listing.dto.AgentPerformanceChannelDTO;
import com.sep.realvista.application.listing.dto.AgentPerformanceTrendPointDTO;
import com.sep.realvista.application.listing.dto.ListingAnalyticsDTO;
import com.sep.realvista.application.listing.dto.ListingDailyViewsDayDTO;
import com.sep.realvista.application.listing.dto.ListingWeeklyViewsDTO;
import com.sep.realvista.domain.agent.lead.LeadSource;
import com.sep.realvista.domain.agent.lead.LeadStatus;
import com.sep.realvista.domain.agent.lead.ListingLeadRepository;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.analytics.ListingDailyViewBucket;
import com.sep.realvista.domain.listing.analytics.ListingDailyViewBucketId;
import com.sep.realvista.domain.listing.analytics.ListingDailyViewBucketRepository;
import com.sep.realvista.domain.listing.analytics.ListingView;
import com.sep.realvista.domain.listing.analytics.ListingViewRepository;
import com.sep.realvista.domain.listing.appointment.Appointment;
import com.sep.realvista.domain.listing.appointment.AppointmentStatus;
import com.sep.realvista.domain.listing.repository.AppointmentRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.domain.property.location.LocationType;
import com.sep.realvista.shared.util.AddressFormatter;
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
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application Service for listing analytics operations.
 * <p>
 * Handles view tracking and analytics aggregation for listings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ListingAnalyticsService {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final ListingViewRepository listingViewRepository;
    private final AppointmentRepository appointmentRepository;
    private final ListingRepository listingRepository;
    private final ListingLeadRepository listingLeadRepository;
    private final ListingDailyViewBucketRepository listingDailyViewBucketRepository;

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
            incrementDailyBucket(listingId);
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

    /**
     * View counts per calendar day for the Monday–Sunday week containing {@code weekStart}.
     * Missing days are filled with zero.
     */
    @Transactional(readOnly = true)
    public ListingWeeklyViewsDTO getListingViewsByWeek(UUID listingId, LocalDate weekStartParam) {
        LocalDate monday = weekStartParam.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);
        List<ListingDailyViewBucket> rows =
                listingDailyViewBucketRepository.findByListingIdAndBucketDateBetweenOrderByBucketDateAsc(
                        listingId, monday, sunday);
        Map<LocalDate, Long> byDay = rows.stream()
                .collect(Collectors.toMap(ListingDailyViewBucket::getBucketDate, ListingDailyViewBucket::getViewCount));

        List<ListingDailyViewsDayDTO> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            long raw = byDay.getOrDefault(d, 0L);
            int v = raw > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) raw;
            days.add(ListingDailyViewsDayDTO.builder().date(d).views(v).build());
        }
        return ListingWeeklyViewsDTO.builder()
                .weekStart(monday)
                .days(days)
                .build();
    }

    private void incrementDailyBucket(UUID listingId) {
        try {
            LocalDate day = LocalDate.now(VIETNAM_ZONE);
            ListingDailyViewBucketId id = new ListingDailyViewBucketId(listingId, day);
            Optional<ListingDailyViewBucket> found = listingDailyViewBucketRepository.findById(id);
            if (found.isPresent()) {
                ListingDailyViewBucket bucket = found.get();
                bucket.incrementBy(1);
                listingDailyViewBucketRepository.save(bucket);
            } else {
                listingDailyViewBucketRepository.save(ListingDailyViewBucket.builder()
                        .listingId(listingId)
                        .bucketDate(day)
                        .viewCount(1)
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to increment daily view bucket for listing {}", listingId, e);
        }
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

    /**
     * Top managed listings for an agent with per-listing analytics, sorted descending.
     *
     * @param agentId agent user id
     * @param sortBy  primary sort: views, inquiries, tours
     * @param limit   max rows (clamped 1–10)
     */
    @Transactional(readOnly = true)
    public List<AgentListingAnalyticsRowDTO> getAgentListingsWithAnalytics(UUID agentId, String sortBy, int limit) {
        String normalizedSort = normalizeListingSort(sortBy);
        int effectiveLimit = clampListingLimit(limit);

        List<Listing> managed = listingRepository.findByUserIdOrPropertyOwnerId(agentId);
        if (managed.isEmpty()) {
            return List.of();
        }

        List<UUID> listingIds = managed.stream().map(Listing::getListingId).toList();

        Map<UUID, Long> viewsByListing =
                toUuidLongMap(listingViewRepository.sumViewCountsGroupedByListingIds(listingIds));
        Map<UUID, Long> uniqueByListing =
                toUuidLongMap(listingViewRepository.countDistinctUsersGroupedByListingIds(listingIds));
        Map<UUID, Long> inquiriesByListing =
                toUuidLongMap(listingLeadRepository.countByAgentIdGroupedByListingId(agentId));

        List<Appointment> appointments = appointmentRepository.findByListingIdInAndStatusIn(
                listingIds, Arrays.asList(AppointmentStatus.values()));
        Map<UUID, Long> toursByListing = appointments.stream()
                .filter(Appointment::isTour)
                .filter(a -> a.getListingId() != null)
                .collect(Collectors.groupingBy(Appointment::getListingId, Collectors.counting()));

        Comparator<Listing> comparator = (a, b) -> compareListingAnalyticsRows(
                a, b, viewsByListing, inquiriesByListing, toursByListing, normalizedSort);

        List<Listing> sorted = managed.stream().sorted(comparator).toList();

        int take = Math.min(effectiveLimit, sorted.size());
        List<Listing> top = sorted.subList(0, take);

        List<AgentListingAnalyticsRowDTO> rows = new ArrayList<>(take);
        for (Listing listing : top) {
            UUID id = listing.getListingId();
            int totalViews = safeLongToInt(viewsByListing.getOrDefault(id, 0L));
            int uniqueViewers = safeLongToInt(uniqueByListing.getOrDefault(id, 0L));
            int tourBookings = safeLongToInt(toursByListing.getOrDefault(id, 0L));
            int inquiries = safeLongToInt(inquiriesByListing.getOrDefault(id, 0L));

            BigDecimal conversionRate = BigDecimal.ZERO;
            if (totalViews > 0) {
                conversionRate = BigDecimal.valueOf(tourBookings)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalViews), 2, RoundingMode.HALF_UP);
            }

            String thumbnail = listingRepository.findThumbnailByListingId(id).orElse(null);

            rows.add(AgentListingAnalyticsRowDTO.builder()
                    .listingId(id)
                    .propertyId(listing.getPropertyId())
                    .name(listing.getName())
                    .slug(listing.getSlug())
                    .thumbnail(thumbnail)
                    .listingType(listing.getListingType())
                    .status(listing.getStatus())
                    .price(listing.getPrice())
                    .fullAddress(formatListingFullAddress(listing))
                    .publishedAt(listing.getPublishedAt())
                    .totalViews(totalViews)
                    .uniqueViewers(uniqueViewers)
                    .tourBookings(tourBookings)
                    .inquiries(inquiries)
                    .conversionRate(conversionRate)
                    .build());
        }

        return rows;
    }

    private static int clampListingLimit(int limit) {
        if (limit < 1) {
            return 1;
        }
        if (limit > 10) {
            return 10;
        }
        return limit;
    }

    private static String normalizeListingSort(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "views";
        }
        String v = sortBy.trim().toLowerCase(Locale.ROOT);
        if ("inquiries".equals(v) || "tours".equals(v) || "views".equals(v)) {
            return v;
        }
        return "views";
    }

    private static Map<UUID, Long> toUuidLongMap(List<Object[]> rows) {
        Map<UUID, Long> map = new HashMap<>();
        if (rows == null) {
            return map;
        }
        for (Object[] row : rows) {
            if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                continue;
            }
            UUID id = (UUID) row[0];
            long count = ((Number) row[1]).longValue();
            map.put(id, count);
        }
        return map;
    }

    private static int safeLongToInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }

    private static int compareListingAnalyticsRows(
            Listing a,
            Listing b,
            Map<UUID, Long> viewsByListing,
            Map<UUID, Long> inquiriesByListing,
            Map<UUID, Long> toursByListing,
            String normalizedSort) {

        UUID aid = a.getListingId();
        UUID bid = b.getListingId();
        long av = viewsByListing.getOrDefault(aid, 0L);
        long bv = viewsByListing.getOrDefault(bid, 0L);
        long ai = inquiriesByListing.getOrDefault(aid, 0L);
        long bi = inquiriesByListing.getOrDefault(bid, 0L);
        long at = toursByListing.getOrDefault(aid, 0L);
        long bt = toursByListing.getOrDefault(bid, 0L);

        int c;
        if ("inquiries".equals(normalizedSort)) {
            c = Long.compare(bi, ai);
            if (c != 0) {
                return c;
            }
            c = Long.compare(bv, av);
            if (c != 0) {
                return c;
            }
            c = Long.compare(bt, at);
        } else if ("tours".equals(normalizedSort)) {
            c = Long.compare(bt, at);
            if (c != 0) {
                return c;
            }
            c = Long.compare(bv, av);
            if (c != 0) {
                return c;
            }
            c = Long.compare(bi, ai);
        } else {
            c = Long.compare(bv, av);
            if (c != 0) {
                return c;
            }
            c = Long.compare(bi, ai);
            if (c != 0) {
                return c;
            }
            c = Long.compare(bt, at);
        }
        if (c != 0) {
            return c;
        }
        String an = a.getName() != null ? a.getName() : "";
        String bn = b.getName() != null ? b.getName() : "";
        return an.compareToIgnoreCase(bn);
    }

    /**
     * Same address formatting as {@link com.sep.realvista.application.listing.mapper.ListingMapper#toListingResponse}.
     */
    private static String formatListingFullAddress(Listing listing) {
        if (listing.getProperty() == null) {
            return "";
        }
        String street = listing.getProperty().getStreetAddress();
        if (listing.getProperty().getLocation() == null) {
            return AddressFormatter.formatFullAddress(street, null, null, null);
        }
        Map<LocationType, String> locationNames = new HashMap<>();
        Location current = listing.getProperty().getLocation();
        while (current != null) {
            locationNames.put(current.getType(), current.getName());
            current = current.getParent();
        }
        return AddressFormatter.formatFullAddress(
                street,
                locationNames.get(LocationType.WARD),
                locationNames.get(LocationType.DISTRICT),
                locationNames.get(LocationType.CITY));
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
