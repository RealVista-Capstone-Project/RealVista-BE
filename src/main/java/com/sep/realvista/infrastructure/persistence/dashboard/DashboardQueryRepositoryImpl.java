package com.sep.realvista.infrastructure.persistence.dashboard;

import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.dashboard.dto.ActiveListingItemDTO;
import com.sep.realvista.application.dashboard.dto.DashboardAgentDTO;
import com.sep.realvista.application.dashboard.dto.DashboardPropertyItemDTO;
import com.sep.realvista.application.dashboard.dto.DashboardScheduleItemDTO;
import com.sep.realvista.application.dashboard.dto.DashboardScheduleResponse;
import com.sep.realvista.application.dashboard.dto.DashboardStatsResponse;
import com.sep.realvista.application.dashboard.dto.FeaturedPropertyDTO;
import com.sep.realvista.application.dashboard.dto.OwnerHeroInsightsResponse;
import com.sep.realvista.application.dashboard.dto.PerformancePointDTO;
import com.sep.realvista.application.dashboard.dto.PerformanceResponse;
import com.sep.realvista.application.dashboard.dto.PropertyOverviewResponse;
import com.sep.realvista.application.dashboard.dto.SalesAnalyticsResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Repository
@Slf4j
public class DashboardQueryRepositoryImpl implements DashboardQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public DashboardStatsResponse getStats(UUID ownerId) {
        LocalDate today = LocalDate.now(); LocalDate firstDayThisMonth = today.withDayOfMonth(1);
        LocalDate firstDayPrevMonth = firstDayThisMonth.minusMonths(1);

        long totalViews = sumViews(ownerId, null, null).longValue();

        BigDecimal currentViews = sumViews(
                ownerId,
                firstDayThisMonth.atStartOfDay(),
                today.plusDays(1).atStartOfDay());
        BigDecimal previousViews = sumViews(
                ownerId,
                firstDayPrevMonth.atStartOfDay(),
                firstDayThisMonth.atStartOfDay());

        // Active listings: total count (no date filter) + trend by published_at
        long activeListing = countListingsByStatus(ownerId, "PUBLISHED", null, null);
        long activeListingCurrent = countListingsByStatus(
                ownerId,
                "PUBLISHED",
                firstDayThisMonth.atStartOfDay(),
                today.plusDays(1).atStartOfDay());
        long activeListingPrevious = countListingsByStatus(
                ownerId,
                "PUBLISHED",
                firstDayPrevMonth.atStartOfDay(),
                firstDayThisMonth.atStartOfDay());

        // Closed deals (SOLD or RENTED): all-time total + monthly trend
        long totalClosed = countClosedListings(ownerId, null, null);
        long closedCurrent = countClosedListings(
                ownerId,
                firstDayThisMonth.atStartOfDay(),
                today.plusDays(1).atStartOfDay());
        long closedPrevious = countClosedListings(
                ownerId,
                firstDayPrevMonth.atStartOfDay(),
                firstDayThisMonth.atStartOfDay());

        // Active leads: total snapshot (no date filter) + trend by lead created_at
        long activeLeads = countActiveLeads(ownerId, null, null);
        long activeLeadsCurrent = countNewActiveLeads(
                ownerId,
                firstDayThisMonth.atStartOfDay(),
                today.plusDays(1).atStartOfDay());
        long activeLeadsPrevious = countNewActiveLeads(
                ownerId,
                firstDayPrevMonth.atStartOfDay(),
                firstDayThisMonth.atStartOfDay());

        // onProgress = listings awaiting approval (PENDING status)
        long onProgress = countListingsByStatus(ownerId, "PENDING", null, null);

        return DashboardStatsResponse.builder()
                .totalViews(totalViews)
                .totalViewsTrend(trend(currentViews, previousViews))
                .activeListing(activeListing)
                .activeListingTrend(trend(
                        BigDecimal.valueOf(activeListingCurrent),
                        BigDecimal.valueOf(activeListingPrevious)))
                .totalClosed(totalClosed)
                .totalClosedTrend(trend(
                        BigDecimal.valueOf(closedCurrent),
                        BigDecimal.valueOf(closedPrevious)))
                .activeLeads(activeLeads)
                .activeLeadsTrend(trend(
                        BigDecimal.valueOf(activeLeadsCurrent),
                        BigDecimal.valueOf(activeLeadsPrevious)))
                .onProgress(onProgress)
                .closedDeals(closedCurrent)
                .build();
    }

    @Override
    public OwnerHeroInsightsResponse getHeroInsights(UUID ownerId) {
        long listingViews = sumViews(ownerId, null, null).longValue();
        long completed = countClosedListings(ownerId, null, null);
        long chatMsgs = countChatMessagesLinkedToOwnerListings(ownerId);
        long appts = countAppointmentsOnOwnerListings(ownerId);

        return OwnerHeroInsightsResponse.builder()
                .listingViewsTotal(listingViews)
                .chatMessagesOnListings(chatMsgs)
                .appointmentsOnOwnerListings(appts)
                .completedContracts(completed)
                .build();
    }

    @Override
    public PerformanceResponse getPerformance(UUID ownerId, String period, String metric) {
        String normalizedPeriod = period == null ? "M" : period.toUpperCase(Locale.ROOT);
        String normalizedMetric = metric == null ? "revenue" : metric.toLowerCase(Locale.ROOT);

        List<PerformancePointDTO> points = switch (normalizedPeriod) {
            case "W" -> buildWeeklyPoints(ownerId, normalizedMetric);
            case "Y" -> buildYearlyPoints(ownerId, normalizedMetric);
            case "M" -> buildMonthlyPoints(ownerId, normalizedMetric);
            default -> buildMonthlyPoints(ownerId, normalizedMetric);
        };

        return PerformanceResponse.builder()
                .period(normalizedPeriod)
                .metric(normalizedMetric)
                .data(points)
                .build();
    }

    @Override
    public FeaturedPropertyDTO getFeaturedProperty(UUID ownerId) {
        String sql = """
                SELECT l.listing_id,
                       COALESCE(pt.name, 'Property') AS type,
                       COALESCE(MAX(p.street_address), '') AS address,
                       COALESCE(SUM(lv.view_count), 0) AS views,
                       l.status,
                       COALESCE((SELECT COUNT(*) FROM listings ls
                                 WHERE ls.property_id = l.property_id
                                   AND ls.status = 'SOLD'
                                   AND ls.deleted = false), 0) AS sold_count,
                       COALESCE((SELECT COUNT(*) FROM listings lr
                                 WHERE lr.property_id = l.property_id
                                   AND lr.status = 'RENTED'
                                   AND lr.deleted = false), 0) AS rented_count,
                       COALESCE((SELECT COALESCE(pm.thumbnail_url, pm.media_url)
                                 FROM listing_medias lm
                                 JOIN property_medias pm ON pm.property_media_id = lm.property_media_id
                                 WHERE lm.listing_id = l.listing_id
                                   AND lm.deleted = false
                                   AND pm.deleted = false
                                 ORDER BY lm.is_primary DESC,
                                          pm.is_primary DESC,
                                          lm.display_order ASC,
                                          lm.created_at ASC
                                 LIMIT 1), '') AS image_url
                FROM listings l
                JOIN properties p ON p.property_id = l.property_id
                LEFT JOIN property_types pt ON pt.property_type_id = p.property_type_id
                LEFT JOIN listing_views lv ON lv.listing_id = l.listing_id AND lv.deleted = false
                WHERE p.owner_id = :ownerId
                  AND p.deleted = false
                  AND l.deleted = false
                GROUP BY l.listing_id, l.property_id, pt.name, l.status, l.created_at
                ORDER BY views DESC, l.created_at DESC
                LIMIT 1
                """;

        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("ownerId", ownerId)
                .getResultList();

        if (rows.isEmpty()) {
            return null;
        }

        Object[] row = rows.get(0);
        String type = str(row[1]);
        String address = str(row[2]);

        return FeaturedPropertyDTO.builder()
                .listingId(uuid(row[0]))
                .type(type)
                .name(type + " - " + address)
                .views(longVal(row[3]))
                .status(str(row[4]))
                .sold(longVal(row[5]))
                .rented(longVal(row[6]))
                .imageUrl(str(row[7]))
                .thumbnailUrl(str(row[7]))
                .build();
    }

    @Override
    public SalesAnalyticsResponse getSalesAnalytics(UUID ownerId, String period) {
        String normalized = period == null ? "month" : period.toLowerCase(Locale.ROOT);
        LocalDateTime from = switch (normalized) {
            case "year" -> LocalDate.now().withDayOfYear(1).atStartOfDay();
            case "quarter" -> {
                int month = LocalDate.now().getMonthValue();
                int quarterStart = ((month - 1) / 3) * 3 + 1;
                yield LocalDate.of(LocalDate.now().getYear(), quarterStart, 1).atStartOfDay();
            }
            default -> LocalDate.now().withDayOfMonth(1).atStartOfDay();
        };
        LocalDateTime to = LocalDateTime.now().plusSeconds(1);

        SalesAnalyticsResponse.Metric direct = metric(ownerId, from, to, true);
        SalesAnalyticsResponse.Metric agent = metric(ownerId, from, to, false);

        return SalesAnalyticsResponse.builder()
                .period(normalized)
                .direct(direct)
                .agent(agent)
                .total(SalesAnalyticsResponse.Metric.builder()
                        .count(direct.getCount() + agent.getCount())
                        .value(direct.getValue().add(agent.getValue()))
                        .build())
                .build();
    }

    @Override
    public List<DashboardAgentDTO> getAgents(UUID ownerId, int limit) {
        String sql = """
                SELECT u.user_id,
                       COALESCE(
                           NULLIF(TRIM(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))), ''),
                           u.business_name,
                           '') AS full_name,
                       u.avatar_url,
                       u.phone,
                       COALESCE(COUNT(DISTINCT ll.listing_lead_id)
                                FILTER (
                                    WHERE ll.status NOT IN ('CLOSED', 'NOT_POTENTIAL')
                                      AND ll.deleted = false
                                ), 0) AS active_leads
                FROM engagements e
                JOIN listings l ON l.listing_id = e.listing_id AND l.deleted = false
                JOIN properties p ON p.property_id = l.property_id AND p.deleted = false
                JOIN users u ON u.user_id = CASE
                        WHEN e.initiator_id = :ownerId THEN e.receiver_id
                        ELSE e.initiator_id
                    END
                LEFT JOIN listing_leads ll ON ll.agent_id = u.user_id AND ll.listing_id = l.listing_id
                WHERE p.owner_id = :ownerId
                  AND e.deleted = false
                  AND e.status IN ('ACCEPTED', 'FINISHED')
                  AND (e.initiator_id = :ownerId OR e.receiver_id = :ownerId)
                GROUP BY u.user_id, u.first_name, u.last_name, u.business_name, u.avatar_url, u.phone
                ORDER BY active_leads DESC, full_name ASC
                """;

        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("ownerId", ownerId)
                .setMaxResults(Math.max(limit, 1))
                .getResultList();

        List<DashboardAgentDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            long activeLeads = longVal(row[4]);
            result.add(DashboardAgentDTO.builder()
                    .userId(uuid(row[0]))
                    .fullName(str(row[1]))
                    .avatarUrl(str(row[2]))
                    .phone(str(row[3]))
                    .activeLeads(activeLeads)
                    .leadBadge(toLeadBadge(activeLeads))
                    .build());
        }
        return result;
    }

    @Override
    public DashboardScheduleResponse getSchedules(UUID ownerId, LocalDate date, String type) {
        String normalizedType = type == null ? "all" : type;
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        LocalDateTime from = targetDate.atStartOfDay();
        LocalDateTime to = targetDate.plusDays(1).atStartOfDay();

        String sql = """
                SELECT a.appointment_id,
                       COALESCE(NULLIF(TRIM(a.sender_notes), ''), 'Appointment') AS title,
                       COALESCE(p.street_address, '') AS address,
                       a.start_time,
                       a.status,
                       CASE
                          WHEN a.receiver_id = :ownerId THEN 'mySchedule'
                          ELSE 'assigned'
                       END AS schedule_type
                FROM appointments a
                LEFT JOIN listings l ON l.listing_id = a.listing_id
                LEFT JOIN properties p ON p.property_id = l.property_id
                WHERE a.deleted = false
                  AND a.start_time >= :fromTime
                  AND a.start_time < :toTime
                  AND (
                        (:mode = 'all' AND (a.sender_id = :ownerId OR a.receiver_id = :ownerId))
                     OR (:mode = 'mySchedule' AND a.receiver_id = :ownerId)
                     OR (:mode = 'assigned' AND a.sender_id = :ownerId)
                  )
                ORDER BY a.start_time ASC
                """;

        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("ownerId", ownerId)
                .setParameter("mode", normalizedType)
                .setParameter("fromTime", from)
                .setParameter("toTime", to)
                .getResultList();

        List<DashboardScheduleItemDTO> items = new ArrayList<>();
        for (Object[] row : rows) {
            LocalDateTime start = time(row[3]);
            items.add(DashboardScheduleItemDTO.builder()
                    .appointmentId(uuid(row[0]))
                    .title(str(row[1]))
                    .address(str(row[2]))
                    .date(start != null ? start.toLocalDate() : targetDate)
                    .time(start != null ? start.toLocalTime() : LocalTime.MIDNIGHT)
                    .status(str(row[4]))
                    .type(str(row[5]))
                    .build());
        }

        return DashboardScheduleResponse.builder()
                .date(targetDate)
                .type(normalizedType)
                .items(items)
                .build();
    }

    @Override
    public PropertyOverviewResponse getPropertyOverview(UUID ownerId) {
        long total = countSimple(
                "SELECT COUNT(*) FROM properties p "
                        + "WHERE p.owner_id = :ownerId AND p.deleted = false",
                ownerId);
        long listed = countSimple("""
                SELECT COUNT(*) FROM listings l
                JOIN properties p ON p.property_id = l.property_id
                WHERE p.owner_id = :ownerId
                  AND p.deleted = false
                  AND l.deleted = false
                  AND l.status = 'PUBLISHED'
                """, ownerId);
        long sold = countSimple("""
                SELECT COUNT(*) FROM listings l
                JOIN properties p ON p.property_id = l.property_id
                WHERE p.owner_id = :ownerId
                  AND p.deleted = false
                  AND l.deleted = false
                  AND l.status IN ('SOLD', 'RENTED')
                """, ownerId);

        String activeListingsSql = """
                SELECT l.listing_id,
                       COALESCE(pt.name, 'Property') AS property_type,
                       COALESCE(p.street_address, '') AS address,
                       COALESCE(COUNT(ll.listing_lead_id)
                                FILTER (
                                    WHERE ll.status NOT IN ('CLOSED', 'NOT_POTENTIAL')
                                      AND ll.deleted = false
                                ), 0) AS lead_count
                FROM listings l
                JOIN properties p ON p.property_id = l.property_id
                LEFT JOIN property_types pt ON pt.property_type_id = p.property_type_id
                LEFT JOIN listing_leads ll ON ll.listing_id = l.listing_id
                WHERE p.owner_id = :ownerId
                  AND p.deleted = false
                  AND l.deleted = false
                  AND l.status = 'PUBLISHED'
                GROUP BY l.listing_id, pt.name, p.street_address, l.created_at
                ORDER BY lead_count DESC, l.created_at DESC
                LIMIT 5
                """;

        List<Object[]> rows = entityManager.createNativeQuery(activeListingsSql)
                .setParameter("ownerId", ownerId)
                .getResultList();

        List<ActiveListingItemDTO> activeListings = new ArrayList<>();
        for (Object[] row : rows) {
            String type = str(row[1]);
            String address = str(row[2]);
            activeListings.add(ActiveListingItemDTO.builder()
                    .listingId(uuid(row[0]))
                    .name(type + " - " + address)
                    .address(address)
                    .leadCount(longVal(row[3]))
                    .build());
        }

        BigDecimal listedPercent = total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(listed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        BigDecimal soldPercent = total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(sold)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        return PropertyOverviewResponse.builder()
                .total(total)
                .listed(listed)
                .listedPercent(listedPercent)
                .sold(sold)
                .soldPercent(soldPercent)
                .activeListings(activeListings)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public PageResponse<DashboardPropertyItemDTO> getProperties(UUID ownerId,
                                                                 String search,
                                                                 String status,
                                                                 int page,
                                                                 int size,
                                                                 String sortBy,
                                                                 String sortDir) {
        StringBuilder where = new StringBuilder(
                " WHERE p.owner_id = :ownerId AND p.deleted = false AND l.deleted = false ");

        if (search != null && !search.isBlank()) {
            where.append(" AND LOWER(p.street_address) LIKE LOWER(:search) ");
        }

        String mappedStatus = mapListingStatus(status);
        if (mappedStatus != null) {
            where.append(" AND l.status = :status ");
        }

        String baseFrom = " FROM listings l "
                + " JOIN properties p ON p.property_id = l.property_id "
                + " LEFT JOIN property_types pt ON pt.property_type_id = p.property_type_id ";

        String countSql = "SELECT COUNT(*) " + baseFrom + where;
        Query countQuery = entityManager.createNativeQuery(countSql)
                .setParameter("ownerId", ownerId);
        if (search != null && !search.isBlank()) {
            countQuery.setParameter("search", "%" + search.trim() + "%");
        }
        if (mappedStatus != null) {
            countQuery.setParameter("status", mappedStatus);
        }

        long totalElements = longVal(countQuery.getSingleResult());
        int safeSize = Math.max(size, 1);
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);

        String orderBy = toOrderBy(sortBy, sortDir);
        String dataSql = """
                SELECT l.listing_id,
                       COALESCE(pt.name, 'Property') AS type,
                       COALESCE(p.street_address, '') AS address,
                       l.price,
                       COALESCE((
                           SELECT COUNT(*)
                           FROM listing_leads ll
                           WHERE ll.listing_id = l.listing_id
                             AND ll.deleted = false
                             AND ll.status NOT IN ('CLOSED', 'NOT_POTENTIAL')
                       ), 0) AS active_leads,
                       COALESCE((
                           SELECT SUM(lv.view_count)
                           FROM listing_views lv
                           WHERE lv.listing_id = l.listing_id
                             AND lv.deleted = false
                       ), 0) AS views,
                       l.status,
                       l.listing_type,
                       COALESCE((
                           SELECT COALESCE(pm.thumbnail_url, pm.media_url)
                           FROM listing_medias lm
                           JOIN property_medias pm ON pm.property_media_id = lm.property_media_id
                           WHERE lm.listing_id = l.listing_id
                             AND lm.deleted = false
                             AND pm.deleted = false
                           ORDER BY lm.is_primary DESC,
                                    pm.is_primary DESC,
                                    lm.display_order ASC,
                                    lm.created_at ASC
                           LIMIT 1
                       ), '') AS image_url
                """ + baseFrom + where + orderBy;

        Query dataQuery = entityManager.createNativeQuery(dataSql)
                .setParameter("ownerId", ownerId)
                .setFirstResult(Math.max(page, 0) * safeSize)
                .setMaxResults(safeSize);

        if (search != null && !search.isBlank()) {
            dataQuery.setParameter("search", "%" + search.trim() + "%");
        }
        if (mappedStatus != null) {
            dataQuery.setParameter("status", mappedStatus);
        }

        List<Object[]> rows = dataQuery.getResultList();
        List<DashboardPropertyItemDTO> content = new ArrayList<>();

        for (Object[] row : rows) {
            String typeName = str(row[1]);
            String address = str(row[2]);
            content.add(DashboardPropertyItemDTO.builder()
                    .listingId(uuid(row[0]))
                    .name(typeName + " - " + address)
                    .type(typeName)
                    .cost(decimal(row[3]))
                    .activeLeads(longVal(row[4]))
                    .views(longVal(row[5]))
                    .status(str(row[6]))
                    .listingType(str(row[7]))
                    .imageUrl(str(row[8]))
                    .build());
        }

        return PageResponse.<DashboardPropertyItemDTO>builder()
                .content(content)
                .page(Math.max(page, 0))
                .size(safeSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(Math.max(page, 0) == 0)
                .last(Math.max(page, 0) + 1 >= Math.max(totalPages, 1))
                .build();
    }

    private List<PerformancePointDTO> buildWeeklyPoints(UUID ownerId, String metric) {
        List<PerformancePointDTO> points = new ArrayList<>();
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        for (LocalDate cursor = start; !cursor.isAfter(end); cursor = cursor.plusDays(1)) {
            LocalDateTime from = cursor.atStartOfDay();
            LocalDateTime to = cursor.plusDays(1).atStartOfDay();
            BigDecimal value = "visit".equals(metric)
                    ? sumViews(ownerId, from, to)
                    : sumRevenue(ownerId, from, to);
            points.add(PerformancePointDTO.builder()
                    .label(cursor.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .value(value)
                    .build());
        }

        return points;
    }

    private List<PerformancePointDTO> buildMonthlyPoints(UUID ownerId, String metric) {
        List<PerformancePointDTO> points = new ArrayList<>();
        YearMonth month = YearMonth.now();
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        int weekIndex = 1;
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            LocalDate weekEnd = cursor.plusDays(6);
            if (weekEnd.isAfter(end)) {
                weekEnd = end;
            }
            BigDecimal value = "visit".equals(metric)
                    ? sumViews(ownerId, cursor.atStartOfDay(), weekEnd.plusDays(1).atStartOfDay())
                    : sumRevenue(ownerId, cursor.atStartOfDay(), weekEnd.plusDays(1).atStartOfDay());

            points.add(PerformancePointDTO.builder()
                    .label("W" + weekIndex)
                    .value(value)
                    .build());

            cursor = weekEnd.plusDays(1);
            weekIndex++;
        }

        return points;
    }

    private List<PerformancePointDTO> buildYearlyPoints(UUID ownerId, String metric) {
        List<PerformancePointDTO> points = new ArrayList<>();
        int year = LocalDate.now().getYear();

        for (int month = 1; month <= 12; month++) {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDateTime from = start.atStartOfDay();
            LocalDateTime to = start.plusMonths(1).atStartOfDay();

            BigDecimal value = "visit".equals(metric)
                    ? sumViews(ownerId, from, to)
                    : sumRevenue(ownerId, from, to);

            points.add(PerformancePointDTO.builder()
                    .label(start.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .value(value)
                    .build());
        }

        return points;
    }

    private SalesAnalyticsResponse.Metric metric(UUID ownerId,
                                                 LocalDateTime from,
                                                 LocalDateTime to,
                                                 boolean direct) {
        String sql = """
                SELECT COUNT(*), COALESCE(SUM(l.price), 0)
                FROM listings l
                JOIN properties p ON p.property_id = l.property_id
                WHERE p.owner_id = :ownerId
                  AND p.deleted = false
                  AND l.deleted = false
                  AND l.status IN ('SOLD', 'RENTED')
                  AND COALESCE(l.sold_at, l.rented_at, l.updated_at) >= :fromTime
                  AND COALESCE(l.sold_at, l.rented_at, l.updated_at) < :toTime
                  AND ((:direct = true AND COALESCE(l.sold_by_user_id, l.rented_by_user_id) = :ownerId)
                    OR (:direct = false AND COALESCE(l.sold_by_user_id, l.rented_by_user_id) <> :ownerId))
                """;

        Object[] row = (Object[]) entityManager.createNativeQuery(sql)
                .setParameter("ownerId", ownerId)
                .setParameter("fromTime", from)
                .setParameter("toTime", to)
                .setParameter("direct", direct)
                .getSingleResult();

        return SalesAnalyticsResponse.Metric.builder()
                .count(longVal(row[0]))
                .value(decimal(row[1]))
                .build();
    }

    private BigDecimal sumRevenue(UUID ownerId, LocalDateTime from, LocalDateTime to) {
        // Fallback chain: sold_at → rented_at → updated_at to handle rows where closing timestamp is null
        String sql = """
                SELECT COALESCE(SUM(l.price), 0)
                FROM listings l
                JOIN properties p ON p.property_id = l.property_id
                WHERE p.owner_id = :ownerId
                  AND p.deleted = false
                  AND l.deleted = false
                  AND l.status IN ('SOLD', 'RENTED')
                """ + optionalTimeFilter("COALESCE(l.sold_at, l.rented_at, l.updated_at)", from, to);

        Query query = entityManager.createNativeQuery(sql)
                .setParameter("ownerId", ownerId);

        setTimeParams(query, from, to);
        return decimal(query.getSingleResult());
    }

    private BigDecimal sumViews(UUID ownerId, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT COALESCE(SUM(lv.view_count), 0)
                FROM listing_views lv
                JOIN listings l ON l.listing_id = lv.listing_id
                JOIN properties p ON p.property_id = l.property_id
                WHERE p.owner_id = :ownerId
                  AND p.deleted = false
                  AND l.deleted = false
                  AND lv.deleted = false
                """ + optionalTimeFilter("lv.viewed_at", from, to);

        Query query = entityManager.createNativeQuery(sql)
                .setParameter("ownerId", ownerId);

        setTimeParams(query, from, to);
        return decimal(query.getSingleResult());
    }

    private long countListingsByStatus(UUID ownerId, String status, LocalDateTime from, LocalDateTime to) {
        // Use published_at for PUBLISHED trend (not created_at which is when the draft was saved)
        // For non-PUBLISHED statuses fall back to created_at
        String timeColumn = "PUBLISHED".equals(status) ? "l.published_at" : "l.created_at";
        String sql = """
                SELECT COUNT(*)
                FROM listings l
                JOIN properties p ON p.property_id = l.property_id
                WHERE p.owner_id = :ownerId
                  AND p.deleted = false
                  AND l.deleted = false
                  AND l.status = :status
                """ + optionalTimeFilter(timeColumn, from, to);

        Query query = entityManager.createNativeQuery(sql)
                .setParameter("ownerId", ownerId)
                .setParameter("status", status);

        setTimeParams(query, from, to);
        return longVal(query.getSingleResult());
    }

    private long countClosedListings(UUID ownerId, LocalDateTime from, LocalDateTime to) {
        // Fallback chain: sold_at → rented_at → updated_at to handle rows where closing timestamp was not set
        String sql = """
                SELECT COUNT(*)
                FROM listings l
                JOIN properties p ON p.property_id = l.property_id
                WHERE p.owner_id = :ownerId
                  AND p.deleted = false
                  AND l.deleted = false
                  AND l.status IN ('SOLD', 'RENTED')
                """ + optionalTimeFilter("COALESCE(l.sold_at, l.rented_at, l.updated_at)", from, to);

        Query query = entityManager.createNativeQuery(sql)
                .setParameter("ownerId", ownerId);

        setTimeParams(query, from, to);
        return longVal(query.getSingleResult());
    }

    /**
     * Count ALL currently active leads for an owner (total snapshot, no date filter).
     * Used for the absolute activeLeads value.
     */
    private long countActiveLeads(UUID ownerId, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT COUNT(*)
                FROM listing_leads ll
                JOIN listings l ON l.listing_id = ll.listing_id
                JOIN properties p ON p.property_id = l.property_id
                WHERE p.owner_id = :ownerId
                  AND p.deleted = false
                  AND l.deleted = false
                  AND ll.deleted = false
                  AND ll.status NOT IN ('CLOSED', 'NOT_POTENTIAL')
                """ + optionalTimeFilter("ll.created_at", from, to);

        Query query = entityManager.createNativeQuery(sql)
                .setParameter("ownerId", ownerId);

        setTimeParams(query, from, to);
        return longVal(query.getSingleResult());
    }

    /**
     * Count NEW active leads created within a time window.
     * Used for trend comparison: new active leads this month vs last month.
     * A lead is "new active" if it was created in the period AND is still not closed/not-potential.
     */
    private long countNewActiveLeads(UUID ownerId, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT COUNT(*)
                FROM listing_leads ll
                JOIN listings l ON l.listing_id = ll.listing_id
                JOIN properties p ON p.property_id = l.property_id
                WHERE p.owner_id = :ownerId
                  AND p.deleted = false
                  AND l.deleted = false
                  AND ll.deleted = false
                  AND ll.status NOT IN ('CLOSED', 'NOT_POTENTIAL')
                  AND ll.created_at >= :fromTime
                  AND ll.created_at < :toTime
                """;

        Query query = entityManager.createNativeQuery(sql)
                .setParameter("ownerId", ownerId)
                .setParameter("fromTime", from)
                .setParameter("toTime", to);

        return longVal(query.getSingleResult());
    }

    private long countSimple(String sql, UUID ownerId) {
        return longVal(entityManager.createNativeQuery(sql)
                .setParameter("ownerId", ownerId)
                .getSingleResult());
    }

    private String mapListingStatus(String status) {
        if (status == null || status.isBlank() || "All".equalsIgnoreCase(status)) {
            return null;
        }
        return switch (status.toLowerCase(Locale.ROOT)) {
            case "available" -> "PUBLISHED";
            case "occupied" -> "RENTED";
            case "sold out", "soldout" -> "SOLD";
            default -> status.toUpperCase(Locale.ROOT);
        };
    }

    private String toOrderBy(String sortBy, String sortDir) {
        String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        String key = sortBy == null ? "cost" : sortBy.toLowerCase(Locale.ROOT);

        return switch (key) {
            case "leads" -> " ORDER BY active_leads " + direction + ", l.created_at DESC ";
            case "views" -> " ORDER BY views " + direction + ", l.created_at DESC ";
            case "cost", "price" -> " ORDER BY l.price " + direction + ", l.created_at DESC ";
            default -> " ORDER BY l.created_at DESC ";
        };
    }

    private String toLeadBadge(long activeLeads) {
        if (activeLeads >= 10) {
            return "hot";
        }
        if (activeLeads >= 5) {
            return "warm";
        }
        return "new";
    }

    private BigDecimal trend(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }

    private String optionalTimeFilter(String column, LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder();
        if (from != null) {
            sql.append(" AND ").append(column).append(" >= :fromTime ");
        }
        if (to != null) {
            sql.append(" AND ").append(column).append(" < :toTime ");
        }
        return sql.toString();
    }

    private void setTimeParams(Query query, LocalDateTime from, LocalDateTime to) {
        if (from != null) {
            query.setParameter("fromTime", from);
        }
        if (to != null) {
            query.setParameter("toTime", to);
        }
    }

    private LocalDateTime time(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        return null;
    }

    private UUID uuid(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID id) {
            return id;
        }
        return UUID.fromString(value.toString());
    }

    private String str(Object value) {
        return value == null ? "" : value.toString();
    }

    private BigDecimal decimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception ex) {
            log.debug("Cannot parse decimal from value: {}", value);
            return BigDecimal.ZERO;
        }
    }

    private long longVal(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (Exception ex) {
            log.debug("Cannot parse long from value: {}", value);
            return 0L;
        }
    }

    private long countChatMessagesLinkedToOwnerListings(UUID ownerId) {
        String sql = """
                SELECT COUNT(*) FROM messages m
                WHERE m.deleted = false
                  AND m.message_type <> 'SYSTEM'
                  AND EXISTS (
                      SELECT 1
                      FROM listing_leads ll
                      INNER JOIN listings l ON l.listing_id = ll.listing_id AND l.deleted = false
                      INNER JOIN properties p ON p.property_id = l.property_id AND p.deleted = false
                      WHERE ll.deleted = false
                        AND ll.conversation_id IS NOT NULL
                        AND ll.conversation_id = m.conversation_id
                        AND p.owner_id = :ownerId
                  )
                """;
        Object result = entityManager.createNativeQuery(sql)
                .setParameter("ownerId", ownerId)
                .getSingleResult();
        return longVal(result);
    }

    private long countAppointmentsOnOwnerListings(UUID ownerId) {
        String sql = """
                SELECT COUNT(*) FROM appointments a
                INNER JOIN listings l ON l.listing_id = a.listing_id AND l.deleted = false
                INNER JOIN properties p ON p.property_id = l.property_id AND p.deleted = false
                WHERE a.deleted = false
                  AND a.listing_id IS NOT NULL
                  AND p.owner_id = :ownerId
                """;
        Object result = entityManager.createNativeQuery(sql)
                .setParameter("ownerId", ownerId)
                .getSingleResult();
        return longVal(result);
    }
}
