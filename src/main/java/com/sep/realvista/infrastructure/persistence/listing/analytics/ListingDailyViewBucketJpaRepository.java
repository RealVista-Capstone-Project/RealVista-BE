package com.sep.realvista.infrastructure.persistence.listing.analytics;

import com.sep.realvista.domain.listing.analytics.ListingDailyViewBucket;
import com.sep.realvista.domain.listing.analytics.ListingDailyViewBucketId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ListingDailyViewBucketJpaRepository
        extends JpaRepository<ListingDailyViewBucket, ListingDailyViewBucketId> {

    List<ListingDailyViewBucket> findByListingIdAndBucketDateBetweenOrderByBucketDateAsc(
            UUID listingId, LocalDate fromInclusive, LocalDate toInclusive);
}
