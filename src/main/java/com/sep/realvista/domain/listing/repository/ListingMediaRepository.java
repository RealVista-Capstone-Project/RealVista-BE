package com.sep.realvista.domain.listing.repository;

import com.sep.realvista.domain.listing.ListingMedia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingMediaRepository {

    ListingMedia save(ListingMedia listingMedia);

    Optional<ListingMedia> findById(UUID id);

    List<ListingMedia> findByListingId(UUID listingId);

    List<ListingMedia> findByListingIdOrderByDisplayOrderAsc(UUID listingId);

    Optional<ListingMedia> findPrimaryByListingId(UUID listingId);

    void deleteById(UUID id);

    void deleteByListingId(UUID listingId);
}
