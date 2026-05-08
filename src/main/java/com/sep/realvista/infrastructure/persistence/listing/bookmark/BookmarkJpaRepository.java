package com.sep.realvista.infrastructure.persistence.listing.bookmark;

import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.bookmark.Bookmark;
import com.sep.realvista.domain.listing.bookmark.BookmarkId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * JPA repository interface for Bookmark entity.
 *
 * Provides data access methods using Spring Data JPA.
 */
@Repository
public interface BookmarkJpaRepository extends JpaRepository<Bookmark, BookmarkId> {

    /**
     * Finds bookmark by user and listing IDs.
     *
     * @param userId the user ID
     * @param listingId the listing ID
     * @return optional bookmark
     */
    Optional<Bookmark> findByUserIdAndListingId(UUID userId, UUID listingId);

    /**
     * Deletes bookmark by user and listing IDs.
     *
     * @param userId the user ID
     * @param listingId the listing ID
     */
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM Bookmark b WHERE b.userId = :userId AND b.listingId = :listingId AND b.deleted = false")
    void deleteByUserIdAndListingId(@Param("userId") UUID userId, @Param("listingId") UUID listingId);

    /**
     * Checks if bookmark exists by user and listing IDs.
     * Only returns true if bookmark exists and is not deleted.
     *
     * @param userId the user ID
     * @param listingId the listing ID
     * @return true if exists and not deleted
     */
    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM Bookmark b
            WHERE b.userId = :userId
            AND b.listingId = :listingId
            AND b.deleted = false
            """)
    boolean existsByUserIdAndListingId(@Param("userId") UUID userId, @Param("listingId") UUID listingId);

    /**
     * Returns the subset of listingIds that the user has bookmarked.
     * Used for bulk isFavorite population in search results.
     */
    @Query("SELECT b.listingId FROM Bookmark b "
            + "WHERE b.userId = :userId AND b.listingId IN :listingIds AND b.deleted = false")
    Set<UUID> findBookmarkedListingIds(@Param("userId") UUID userId, @Param("listingIds") Collection<UUID> listingIds);

    /**
     * Finds bookmarks for a user with optional filters.
     * Eagerly fetches listing, property, property type, and location data.
     * Sort direction is driven by the Pageable parameter.
     *
     * countQuery is required because Hibernate cannot derive a COUNT from a
     * query that contains JOIN FETCH — without it Hibernate falls back to
     * in-memory pagination which loads the entire result set before paging.
     *
     * @param userId the user ID
     * @param propertyTypes optional property type codes filter (e.g. APARTMENT, VILLA)
     * @param listingType optional listing type filter
     * @param pageable pagination and sort
     * @return page of bookmarks
     */
    @Query(
        value = """
                SELECT DISTINCT b FROM Bookmark b
                LEFT JOIN FETCH b.listing l
                LEFT JOIN FETCH l.property p
                LEFT JOIN FETCH p.propertyType pt
                LEFT JOIN FETCH p.location loc
                WHERE b.userId = :userId
                AND b.deleted = false
                AND l.deleted = false
                AND (:listingType IS NULL OR l.listingType = :listingType)
                AND (:propertyTypes IS NULL OR pt.code IN :propertyTypes)
                """,
        countQuery = """
                SELECT COUNT(DISTINCT b) FROM Bookmark b
                LEFT JOIN b.listing l
                LEFT JOIN l.property p
                LEFT JOIN p.propertyType pt
                WHERE b.userId = :userId
                AND b.deleted = false
                AND l.deleted = false
                AND (:listingType IS NULL OR l.listingType = :listingType)
                AND (:propertyTypes IS NULL OR pt.code IN :propertyTypes)
                """
    )
    Page<Bookmark> findBookmarksByUserWithFilters(
            @Param("userId") UUID userId,
            @Param("propertyTypes") List<String> propertyTypes,
            @Param("listingType") ListingType listingType,
            Pageable pageable
    );

    @Query("SELECT b.userId FROM Bookmark b WHERE b.listingId = :listingId AND b.deleted = false")
    List<UUID> findActiveUserIdsByListingId(@Param("listingId") UUID listingId);
}