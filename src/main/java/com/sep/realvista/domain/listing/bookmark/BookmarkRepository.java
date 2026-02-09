package com.sep.realvista.domain.listing.bookmark;

import com.sep.realvista.domain.listing.ListingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Bookmark aggregate.
 *
 * Defines contract for bookmark data access operations.
 * Implementation will be provided in infrastructure layer.
 */
public interface BookmarkRepository {

    /**
     * Saves a bookmark.
     *
     * @param bookmark the bookmark to save
     * @return the saved bookmark
     */
    Bookmark save(Bookmark bookmark);

    /**
     * Finds a bookmark by user and listing IDs.
     *
     * @param userId the user ID
     * @param listingId the listing ID
     * @return optional bookmark if found
     */
    Optional<Bookmark> findByUserIdAndListingId(UUID userId, UUID listingId);

    /**
     * Deletes a bookmark by user and listing IDs.
     *
     * @param userId the user ID
     * @param listingId the listing ID
     */
    void deleteByUserIdAndListingId(UUID userId, UUID listingId);

    /**
     * Checks if a bookmark exists for user and listing.
     *
     * @param userId the user ID
     * @param listingId the listing ID
     * @return true if bookmark exists
     */
    boolean existsByUserIdAndListingId(UUID userId, UUID listingId);

    /**
     * Finds all bookmarks for a user with filters and pagination.
     *
     * @param userId the user ID
     * @param propertyTypeIds optional list of property type IDs to filter by
     * @param listingType optional listing type filter (SALE or RENT)
     * @param pageable pagination and sorting parameters
     * @return page of bookmarks with full listing and property data
     */
    Page<Bookmark> findBookmarksByUserWithFilters(
            UUID userId,
            List<UUID> propertyTypeIds,
            ListingType listingType,
            Pageable pageable
    );
}