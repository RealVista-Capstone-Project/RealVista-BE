package com.sep.realvista.infrastructure.persistence.listing.analytics;

import com.sep.realvista.domain.listing.analytics.ListingDailyViewBucket;
import com.sep.realvista.domain.listing.analytics.ListingDailyViewBucketId;
import com.sep.realvista.domain.listing.analytics.ListingDailyViewBucketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ListingDailyViewBucketRepositoryImpl implements ListingDailyViewBucketRepository {

    private final ListingDailyViewBucketJpaRepository jpaRepository;

    @Override
    public Optional<ListingDailyViewBucket> findById(ListingDailyViewBucketId id) {
        return jpaRepository.findById(id);
    }

    @Override
    public ListingDailyViewBucket save(ListingDailyViewBucket bucket) {
        return jpaRepository.save(bucket);
    }

    @Override
    public List<ListingDailyViewBucket> findByListingIdAndBucketDateBetweenOrderByBucketDateAsc(
            UUID listingId, LocalDate fromInclusive, LocalDate toInclusive) {
        return jpaRepository.findByListingIdAndBucketDateBetweenOrderByBucketDateAsc(
                listingId, fromInclusive, toInclusive);
    }
}
