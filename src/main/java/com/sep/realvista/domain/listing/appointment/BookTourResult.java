package com.sep.realvista.domain.listing.appointment;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.user.User;

import java.util.List;

/**
 * Result of a tour booking operation, carrying the created appointments
 * along with the related listing, sender, and owner for downstream processing
 * (e.g., email notifications).
 */
public record BookTourResult(
        List<Appointment> appointments,
        Listing listing,
        User sender,
        User owner
) {
}
