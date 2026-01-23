package com.sep.realvista.domain.listing.bookmark;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite Primary Key for {@link Bookmark} entity.
 * <p>
 * Represents a unique combination of (user_id, listing_id) to track
 * which user has bookmarked which listing. Required by JPA when an entity
 * has multiple @Id fields.
 *
 * @see Bookmark
 */
public class BookmarkId implements Serializable {

    private UUID userId;
    private UUID listingId;

    public BookmarkId() {
    }

    public BookmarkId(UUID userId, UUID listingId) {
        this.userId = userId;
        this.listingId = listingId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BookmarkId that = (BookmarkId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(listingId, that.listingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, listingId);
    }
}
