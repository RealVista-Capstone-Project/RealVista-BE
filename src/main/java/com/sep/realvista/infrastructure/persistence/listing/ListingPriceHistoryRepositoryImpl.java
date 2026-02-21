package com.sep.realvista.infrastructure.persistence.listing;

import com.sep.realvista.domain.listing.analytics.ListingPriceHistory;
import com.sep.realvista.domain.listing.repository.ListingPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of ListingPriceHistoryRepository using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class ListingPriceHistoryRepositoryImpl implements ListingPriceHistoryRepository {

    private final ListingPriceHistoryJpaRepository jpaRepository;

    @Override
    public List<ListingPriceHistory> findByListingIdOrderByCreatedAtDesc(UUID listingId) {
        return jpaRepository.findByListingIdOrderByCreatedAtDesc(listingId);
    }

    @Override
    public ListingPriceHistory save(ListingPriceHistory priceHistory) {
        return jpaRepository.save(priceHistory);
    }
}
