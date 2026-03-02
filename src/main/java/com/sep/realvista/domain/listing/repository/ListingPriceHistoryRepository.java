package com.sep.realvista.domain.listing.repository;

import com.sep.realvista.domain.listing.analytics.ListingPriceHistory;

import java.util.List;
import java.util.UUID;

/**
 * Domain repository interface for ListingPriceHistory entity.
 */
public interface ListingPriceHistoryRepository {

    /**
     * Find all price history entries for a listing, ordered by created_at descending.
     *
     * @param listingId the listing ID
     * @return list of price history entries
     */
    List<ListingPriceHistory> findByListingIdOrderByCreatedAtDesc(UUID listingId);

    /**
     * Save a price history entry.
     *
     * @param priceHistory the price history to save
     * @return saved price history
     */
    ListingPriceHistory save(ListingPriceHistory priceHistory);
}
