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

import java.util.List;
import java.util.Optional;
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
    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.userId = :userId AND b.listingId = :listingId")
    void deleteByUserIdAndListingId(@Param("userId") UUID userId, @Param("listingId") UUID listingId);

    /**
     * Checks if bookmark exists by user and listing IDs.
     *
     * @param userId the user ID
     * @param listingId the listing ID
     * @return true if exists
     */
    boolean existsByUserIdAndListingId(UUID userId, UUID listingId);

    /**
     * Finds bookmarks for a user with optional filters.
     * Eagerly fetches listing, property, property type, and location data for efficient querying.
     *
     * @param userId the user ID
     * @param propertyTypeIds optional property type filter
     * @param listingType optional listing type filter
     * @param pageable pagination and sorting
     * @return page of bookmarks
     */
    @Query("""
            SELECT DISTINCT b FROM Bookmark b
            LEFT JOIN FETCH b.listing l
            LEFT JOIN FETCH l.property p
            LEFT JOIN FETCH p.propertyType pt
            LEFT JOIN FETCH p.location loc
            WHERE b.userId = :userId
            AND b.deleted = false
            AND l.deleted = false
            AND (:listingType IS NULL OR l.listingType = :listingType)
            AND (:propertyTypeIds IS NULL OR p.propertyTypeId IN :propertyTypeIds)
            ORDER BY b.createdAt DESC
            """)
    Page<Bookmark> findByUserIdWithFiltersOrderByCreatedAtDesc(
            @Param("userId") UUID userId,
            @Param("propertyTypeIds") List<UUID> propertyTypeIds,
            @Param("listingType") ListingType listingType,
            Pageable pageable
    );

    /**
     * Finds bookmarks for a user with optional filters, ordered by oldest first.
     * Eagerly fetches listing, property, property type, and location data for efficient querying.
     *
     * @param userId the user ID
     * @param propertyTypeIds optional property type filter
     * @param listingType optional listing type filter
     * @param pageable pagination
     * @return page of bookmarks
     */
    @Query("""
            SELECT DISTINCT b FROM Bookmark b
            LEFT JOIN FETCH b.listing l
            LEFT JOIN FETCH l.property p
            LEFT JOIN FETCH p.propertyType pt
            LEFT JOIN FETCH p.location loc
            WHERE b.userId = :userId
            AND b.deleted = false
            AND l.deleted = false
            AND (:listingType IS NULL OR l.listingType = :listingType)
            AND (:propertyTypeIds IS NULL OR p.propertyTypeId IN :propertyTypeIds)
            ORDER BY b.createdAt ASC
            """)
    Page<Bookmark> findByUserIdWithFiltersOrderByCreatedAtAsc(
            @Param("userId") UUID userId,
            @Param("propertyTypeIds") List<UUID> propertyTypeIds,
            @Param("listingType") ListingType listingType,
            Pageable pageable
    );
}