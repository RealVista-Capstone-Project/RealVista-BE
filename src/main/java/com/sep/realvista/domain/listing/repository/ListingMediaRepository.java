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

    /**
     * Find primary media for multiple listings in a single batch query.
     * Returns only the primary (display_order = 0) media for each listing.
     *
     * @param listingIds list of listing IDs
     * @return list of primary listing media
     */
    List<ListingMedia> findPrimaryByListingIds(List<UUID> listingIds);

    /**
     * Find all listing IDs that have 3D media among the given listing IDs.
     * Returns distinct listing IDs that have at least one non-deleted 3D media.
     *
     * @param listingIds list of listing IDs to check
     * @return list of listing IDs that have 3D media
     */
    List<UUID> findListingIdsWith3DMedia(List<UUID> listingIds);

    void deleteById(UUID id);

    void deleteByListingId(UUID listingId);
}
