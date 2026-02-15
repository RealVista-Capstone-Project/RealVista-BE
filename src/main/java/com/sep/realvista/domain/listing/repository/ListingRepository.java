package com.sep.realvista.domain.listing.repository;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingRepository {

    Listing save(Listing listing);

    Optional<Listing> findById(UUID id);

    List<Listing> findByPropertyId(UUID propertyId);

    List<Listing> findByUserId(UUID userId);

    List<Listing> findByStatus(ListingStatus status);

    List<Listing> findByListingTypeAndStatus(ListingType listingType, ListingStatus status);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
