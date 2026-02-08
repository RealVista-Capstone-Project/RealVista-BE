package com.sep.realvista.infrastructure.persistence.listing;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Listing entity.
 * All queries exclude soft-deleted records (deleted = false).
 */
public interface ListingJpaRepository extends JpaRepository<Listing, UUID> {

    @Query("SELECT l FROM Listing l WHERE l.property.propertyId = :propertyId AND l.deleted = false")
    List<Listing> findByPropertyId(@Param("propertyId") UUID propertyId);

    @Query("SELECT l FROM Listing l WHERE l.user.userId = :userId AND l.deleted = false")
    List<Listing> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT l FROM Listing l WHERE l.status = :status AND l.deleted = false")
    List<Listing> findByStatus(@Param("status") ListingStatus status);

    @Query("SELECT l FROM Listing l WHERE l.listingType = :listingType AND l.status = :status "
            + "AND l.deleted = false")
    List<Listing> findByListingTypeAndStatus(@Param("listingType") ListingType listingType,
            @Param("status") ListingStatus status);

    @Query("SELECT l FROM Listing l WHERE l.listingId = :id AND l.deleted = false")
    Optional<Listing> findActiveById(@Param("id") UUID id);

    /**
     * Find published listings within geographical bounds using native SQL for performance.
     * Uses spatial indexing on lat/lng columns.
     */
    @Query(value = """
        SELECT l.* FROM listings l
        JOIN properties p ON l.property_id = p.property_id
        WHERE l.status = 'PUBLISHED'
          AND l.deleted = false
          AND p.deleted = false
          AND p.latitude BETWEEN :southLat AND :northLat
          AND p.longitude BETWEEN :westLng AND :eastLng
          AND (:listingType IS NULL OR l.listing_type = CAST(:listingType AS VARCHAR))
        ORDER BY l.published_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Listing> findPublishedWithinBounds(
            @Param("northLat") BigDecimal northLat,
            @Param("southLat") BigDecimal southLat,
            @Param("eastLng") BigDecimal eastLng,
            @Param("westLng") BigDecimal westLng,
            @Param("listingType") String listingType,
            @Param("limit") int limit
    );

    /**
     * Count published listings within geographical bounds.
     */
    @Query(value = """
        SELECT COUNT(*) FROM listings l
        JOIN properties p ON l.property_id = p.property_id
        WHERE l.status = 'PUBLISHED'
          AND l.deleted = false
          AND p.deleted = false
          AND p.latitude BETWEEN :southLat AND :northLat
          AND p.longitude BETWEEN :westLng AND :eastLng
          AND (:listingType IS NULL OR l.listing_type = CAST(:listingType AS VARCHAR))
        """, nativeQuery = true)
    Long countPublishedWithinBounds(
            @Param("northLat") BigDecimal northLat,
            @Param("southLat") BigDecimal southLat,
            @Param("eastLng") BigDecimal eastLng,
            @Param("westLng") BigDecimal westLng,
            @Param("listingType") String listingType
    );
}

