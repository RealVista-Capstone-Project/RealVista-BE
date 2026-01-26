package com.sep.realvista.domain.listing.analytics;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite Primary Key for {@link ListingView} entity.
 * <p>
 * Represents a unique combination of (listing_id, user_id) to track
 * which user has viewed which listing. Required by JPA when an entity
 * has multiple @Id fields.
 *
 * @see ListingView
 */
public class ListingViewId implements Serializable {

    private UUID listingId;
    private UUID userId;

    public ListingViewId() {
    }

    public ListingViewId(UUID listingId, UUID userId) {
        this.listingId = listingId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ListingViewId that = (ListingViewId) o;
        return Objects.equals(listingId, that.listingId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listingId, userId);
    }
}
