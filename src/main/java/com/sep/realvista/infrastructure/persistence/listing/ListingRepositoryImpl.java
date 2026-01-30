package com.sep.realvista.infrastructure.persistence.listing;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of ListingRepository interface.
 */
@Repository
@RequiredArgsConstructor
public class ListingRepositoryImpl implements ListingRepository {

    private final ListingJpaRepository jpaRepository;

    @Override
    public Optional<Listing> findById(UUID listingId) {
        return jpaRepository.findById(listingId);
    }

    @Override
    public boolean existsById(UUID listingId) {
        return jpaRepository.existsById(listingId);
    }
}