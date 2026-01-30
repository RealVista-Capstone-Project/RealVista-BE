package com.sep.realvista.domain.listing;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Listing aggregate.
 * 
 * Defines contract for listing data access operations.
 */
public interface ListingRepository {

    /**
     * Finds a listing by ID.
     *
     * @param listingId the listing ID
     * @return optional listing if found
     */
    Optional<Listing> findById(UUID listingId);

    /**
     * Checks if a listing exists by ID.
     *
     * @param listingId the listing ID
     * @return true if listing exists
     */
    boolean existsById(UUID listingId);
}