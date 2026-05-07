package com.sep.realvista.domain.listing.analytics;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistent aggregates: views per listing per calendar day.
 */
public interface ListingDailyViewBucketRepository {

    Optional<ListingDailyViewBucket> findById(ListingDailyViewBucketId id);

    ListingDailyViewBucket save(ListingDailyViewBucket bucket);

    /**
     * Inclusive range on {@code bucketDate}.
     */
    List<ListingDailyViewBucket> findByListingIdAndBucketDateBetweenOrderByBucketDateAsc(
            UUID listingId, LocalDate fromInclusive, LocalDate toInclusive);
}
