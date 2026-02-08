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
     * Find published listings within geographical bounds using optimized native SQL.
     * Uses composite spatial index on (latitude, longitude) for better performance.
     * Query optimizations:
     * - Composite index on p.latitude, p.longitude for bounding box search
     * - Index on l.status and l.deleted for quick filtering
     * - Price index for future price range filtering
     */
    @Query(value = """
        SELECT l.* FROM listings l
        INNER JOIN properties p ON l.property_id = p.property_id
        WHERE l.status = 'PUBLISHED'
          AND l.deleted = false
          AND p.deleted = false
          AND p.latitude >= :southLat
          AND p.latitude <= :northLat
          AND p.longitude >= :westLng
          AND p.longitude <= :eastLng
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
     * Optimized to match the search query structure.
     */
    @Query(value = """
        SELECT COUNT(*) FROM listings l
        INNER JOIN properties p ON l.property_id = p.property_id
        WHERE l.status = 'PUBLISHED'
          AND l.deleted = false
          AND p.deleted = false
          AND p.latitude >= :southLat
          AND p.latitude <= :northLat
          AND p.longitude >= :westLng
          AND p.longitude <= :eastLng
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

