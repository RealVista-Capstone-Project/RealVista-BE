package com.sep.realvista.domain.listing.analytics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for {@link ListingView} entity.
 * <p>
 * Provides data access methods for tracking and aggregating listing view metrics.
 * This interface follows the Repository pattern from Domain-Driven Design,
 * defining the contract for persistence operations without implementation details.
 */
public interface ListingViewRepository {

    /**
     * Save or update a listing view record.
     *
     * @param listingView the listing view entity to save
     * @return the saved listing view entity
     */
    ListingView save(ListingView listingView);

    /**
     * Find a listing view record by composite key (listing_id, user_id).
     *
     * @param listingId the listing ID
     * @param userId    the user ID
     * @return Optional containing the listing view if found, empty otherwise
     */
    Optional<ListingView> findByListingIdAndUserId(UUID listingId, UUID userId);

    /**
     * Count the number of distinct users who have viewed a listing.
     * This represents the "unique viewers" metric.
     *
     * @param listingId the listing ID
     * @return the count of unique viewers
     */
    Integer countDistinctUsersByListingId(UUID listingId);

    /**
     * Get the total view count for a listing.
     * This is the sum of all view_count values across all viewers.
     *
     * @param listingId the listing ID
     * @return the total view count
     */
    Integer getTotalViewCountByListingId(UUID listingId);

    /**
     * Get total view count for a set of listings in a time range.
     *
     * @param listingIds   listing IDs
     * @param from         inclusive range start
     * @param toExclusive  exclusive range end
     * @return total views in range
     */
    Long getTotalViewCountByListingIdsAndViewedAtBetween(
            List<UUID> listingIds, LocalDateTime from, LocalDateTime toExclusive);
}
