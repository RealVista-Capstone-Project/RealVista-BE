package com.sep.realvista.domain.listing.exception;

import com.sep.realvista.domain.common.exception.DomainException;

import java.util.UUID;

/**
 * Exception thrown when a listing is not found.
 */
public class ListingNotFoundException extends DomainException {

    public ListingNotFoundException(UUID listingId) {
        super("Listing not found with ID: " + listingId, "LISTING_NOT_FOUND");
    }

    public ListingNotFoundException(String message) {
        super(message, "LISTING_NOT_FOUND");
    }
}