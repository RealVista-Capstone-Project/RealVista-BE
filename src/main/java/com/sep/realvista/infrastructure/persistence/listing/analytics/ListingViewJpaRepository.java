package com.sep.realvista.infrastructure.persistence.listing.analytics;

import com.sep.realvista.domain.listing.analytics.ListingView;
import com.sep.realvista.domain.listing.analytics.ListingViewId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ListingView} entity.
 * <p>
 * Provides standard CRUD operations and custom queries for listing view analytics.
 */
public interface ListingViewJpaRepository extends JpaRepository<ListingView, ListingViewId> {

    /**
     * Count the number of distinct users who have viewed a listing.
     * Excludes soft-deleted records.
     *
     * @param listingId the listing ID
     * @return the count of unique viewers
     */
    @Query("SELECT COUNT(DISTINCT lv.userId) FROM ListingView lv "
            + "WHERE lv.listingId = :listingId AND lv.deleted = false")
    Integer countDistinctUsersByListingId(@Param("listingId") UUID listingId);

    /**
     * Get the total view count for a listing.
     * This sums all view_count values across all viewers.
     * Returns 0 if no views exist.
     *
     * @param listingId the listing ID
     * @return the total view count
     */
    @Query("SELECT COALESCE(SUM(lv.viewCount), 0) FROM ListingView lv "
            + "WHERE lv.listingId = :listingId AND lv.deleted = false")
    Integer getTotalViewCountByListingId(@Param("listingId") UUID listingId);

    @Query("SELECT COALESCE(SUM(lv.viewCount), 0) FROM ListingView lv "
            + "WHERE lv.listingId IN :listingIds "
            + "AND lv.deleted = false "
            + "AND lv.viewedAt >= :from "
            + "AND lv.viewedAt < :toExclusive")
    Long getTotalViewCountByListingIdsAndViewedAtBetween(
            @Param("listingIds") List<UUID> listingIds,
            @Param("from") LocalDateTime from,
            @Param("toExclusive") LocalDateTime toExclusive);

    @Query("SELECT lv.listingId, COALESCE(SUM(lv.viewCount), 0) FROM ListingView lv "
            + "WHERE lv.listingId IN :listingIds AND lv.deleted = false "
            + "GROUP BY lv.listingId")
    List<Object[]> sumViewCountsGroupedByListingIds(@Param("listingIds") List<UUID> listingIds);

    @Query("SELECT lv.listingId, COUNT(DISTINCT lv.userId) FROM ListingView lv "
            + "WHERE lv.listingId IN :listingIds AND lv.deleted = false "
            + "GROUP BY lv.listingId")
    List<Object[]> countDistinctUsersGroupedByListingIds(@Param("listingIds") List<UUID> listingIds);
}
