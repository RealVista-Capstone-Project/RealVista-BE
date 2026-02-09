package com.sep.realvista.domain.listing.repository;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.search.MapBounds;

import java.math.BigDecimal;
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

    /**
     * Find published listings within geographical bounds.
     * Used for map-based property searches.
     *
     * @param bounds geographical bounds
     * @param listingType optional listing type filter (RENT/SALE), null for all types
     * @param limit maximum number of results to return
     * @param offset number of results to skip (for pagination)
     * @return list of published listings within the specified bounds
     */
    List<Listing> findPublishedWithinBounds(
            MapBounds bounds,
            ListingType listingType,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int limit,
            int offset
    );

    /**
     * Count published listings within geographical bounds.
     *
     * @param bounds geographical bounds
     * @param listingType optional listing type filter (RENT/SALE), null for all types
     * @return total count of published listings within bounds
     */
    Long countPublishedWithinBounds(
            MapBounds bounds,
            ListingType listingType,
            BigDecimal minPrice,
            BigDecimal maxPrice
    );
}
