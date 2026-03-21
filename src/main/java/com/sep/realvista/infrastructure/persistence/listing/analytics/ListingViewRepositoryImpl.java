package com.sep.realvista.infrastructure.persistence.listing.analytics;

import com.sep.realvista.domain.listing.analytics.ListingView;
import com.sep.realvista.domain.listing.analytics.ListingViewId;
import com.sep.realvista.domain.listing.analytics.ListingViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link ListingViewRepository} using Spring Data JPA.
 * <p>
 * This adapter class bridges the domain layer repository interface
 * with the infrastructure layer JPA repository.
 */
@Repository
@RequiredArgsConstructor
public class ListingViewRepositoryImpl implements ListingViewRepository {

    private final ListingViewJpaRepository jpaRepository;

    @Override
    public ListingView save(ListingView listingView) {
        return jpaRepository.save(listingView);
    }

    @Override
    public Optional<ListingView> findByListingIdAndUserId(UUID listingId, UUID userId) {
        ListingViewId id = new ListingViewId(listingId, userId);
        return jpaRepository.findById(id);
    }

    @Override
    public Integer countDistinctUsersByListingId(UUID listingId) {
        return jpaRepository.countDistinctUsersByListingId(listingId);
    }

    @Override
    public Integer getTotalViewCountByListingId(UUID listingId) {
        return jpaRepository.getTotalViewCountByListingId(listingId);
    }
}
