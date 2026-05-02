package com.sep.realvista.infrastructure.persistence.listing;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Listing entity.
 * All queries exclude soft-deleted records (deleted = false).
 */
public interface ListingJpaRepository extends JpaRepository<Listing, UUID>, JpaSpecificationExecutor<Listing> {

    @Query("SELECT pm.mediaUrl FROM ListingMedia lm "
            + "JOIN lm.propertyMedia pm "
            + "WHERE lm.listing.listingId = :listingId "
            + "AND lm.isPrimary = true AND lm.deleted = false AND pm.deleted = false")
    Optional<String> findThumbnailByListingId(@Param("listingId") UUID listingId);

    @Query("SELECT l FROM Listing l WHERE l.property.propertyId = :propertyId AND l.deleted = false")
    List<Listing> findByPropertyId(@Param("propertyId") UUID propertyId);

    @Query("SELECT l FROM Listing l WHERE l.user.userId = :userId AND l.deleted = false")
    List<Listing> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT l FROM Listing l "
            + "LEFT JOIN l.property p "
            + "LEFT JOIN l.user u "
            + "WHERE (l.user.userId = :userId "
            + "   OR (p.ownerId = :userId AND (u IS NULL OR u.deleted = false))) "
            + "AND l.deleted = false")
    List<Listing> findByUserIdOrPropertyOwnerId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(l) FROM Listing l "
            + "LEFT JOIN l.property p "
            + "WHERE (l.user.userId = :userId OR p.ownerId = :userId) "
            + "AND l.deleted = false "
            + "AND l.createdAt >= :start "
            + "AND l.createdAt < :end")
    long countByUserIdOrPropertyOwnerIdAndCreatedAtBetween(
            @Param("userId") UUID userId,
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);

    @Query("SELECT COUNT(l) FROM Listing l "
            + "LEFT JOIN l.property p "
            + "WHERE (l.user.userId = :userId OR p.ownerId = :userId) "
            + "AND l.listingType = :listingType "
            + "AND l.deleted = false "
            + "AND l.createdAt >= :start "
            + "AND l.createdAt < :end")
    long countByUserIdOrPropertyOwnerIdAndListingTypeAndCreatedAtBetween(
            @Param("userId") UUID userId,
            @Param("listingType") ListingType listingType,
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);

    @Query("SELECT l FROM Listing l WHERE l.status = :status AND l.deleted = false")
    List<Listing> findByStatus(@Param("status") ListingStatus status);

    /**
     * Find all PUBLISHED listings whose publishedAt timestamp is strictly before the given cutoff.
     * Used by the listing expiry scheduler.
     */
    @Query("SELECT l FROM Listing l "
            + "WHERE l.status = 'PUBLISHED' AND l.deleted = false "
            + "AND l.publishedAt < :cutoff")
    List<Listing> findPublishedListingsPublishedBefore(@Param("cutoff") java.time.LocalDateTime cutoff);

    /**
     * Find all PUBLISHED listings whose publishedAt timestamp is strictly before the given windowEnd
     * and greater than or equal to windowStart.
     * Used by the listing expiry scheduler to send warning notifications.
     */
    @Query("SELECT l FROM Listing l "
            + "WHERE l.status = 'PUBLISHED' AND l.deleted = false "
            + "AND l.publishedAt >= :windowStart AND l.publishedAt < :windowEnd")
    List<Listing> findPublishedListingsPublishedBetween(@Param("windowStart") java.time.LocalDateTime windowStart,
                                                        @Param("windowEnd") java.time.LocalDateTime windowEnd);

    @Query("SELECT l FROM Listing l "
            + "WHERE l.status = :status AND l.deleted = false "
            + "AND l.updatedAt < :cutoff")
    List<Listing> findByStatusAndUpdatedAtBefore(@Param("status") ListingStatus status,
                                                 @Param("cutoff") java.time.LocalDateTime cutoff);

    long countByStatus(ListingStatus status);

    List<Listing> findTop10ByOrderByUpdatedAtDesc();
    
    
    @Query("SELECT l.listingId, l.name, m.thumbnailUrl, "
            + "COALESCE(SUM(bp.price * 1.0 / NULLIF(bp.featuredQuota + bp.hotBadgeQuota, 0)), 0), "
            + "COUNT(CASE WHEN b.boostType = 'FEATURED' THEN 1 END), "
            + "COUNT(CASE WHEN b.boostType = 'HOT_BADGE' THEN 1 END) "
            + "FROM Listing l "
            + "LEFT JOIN l.property p "
            + "LEFT JOIN p.mediaList m ON m.isPrimary = true "
            + "LEFT JOIN l.boosts b ON b.deleted = false AND b.createdAt BETWEEN :startDate AND :endDate "
            + "LEFT JOIN b.boostPackage bp "
            + "WHERE l.deleted = false AND l.status <> com.sep.realvista.domain.listing.ListingStatus.BANNED "
            + "GROUP BY l.listingId, l.name, m.thumbnailUrl "
            + "ORDER BY COALESCE(SUM(bp.price * 1.0 / NULLIF(bp.featuredQuota + bp.hotBadgeQuota, 0)), 0) DESC, "
            + "l.name ASC")
    List<Object[]> findTopListings(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate,
                                  Pageable pageable);

    long countByUserIdAndCreatedAtBetween(UUID userId, LocalDateTime start, LocalDateTime end);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
 
    @Query("SELECT COUNT(g) > 0 FROM Property3DGeneration g "
            + "WHERE g.propertyId = (SELECT l.propertyId FROM Listing l WHERE l.listingId = :listingId) "
            + "AND g.status = com.sep.realvista.domain.property.Property3DGenerationStatus.SUCCEEDED "
            + "AND g.deleted = false")
    boolean has3dTour(@Param("listingId") UUID listingId);


    @Query("SELECT l FROM Listing l WHERE l.listingType = :listingType AND l.status = :status "
            + "AND l.deleted = false")
    List<Listing> findByListingTypeAndStatus(@Param("listingType") ListingType listingType,
            @Param("status") ListingStatus status);

    @Query("SELECT l FROM Listing l WHERE l.listingId = :id AND l.deleted = false AND l.status <> 'BANNED'")
    Optional<Listing> findActiveById(@Param("id") UUID id);

    @Query("SELECT l FROM Listing l WHERE l.slug = :slug AND l.deleted = false AND l.status <> 'BANNED'")
    Optional<Listing> findBySlugAndDeletedFalse(@Param("slug") String slug);

    @Query("SELECT l FROM Listing l WHERE l.deleted = false")
    List<Listing> findByDeletedFalse();

    @Query("SELECT COUNT(l) > 0 FROM Listing l WHERE l.property.propertyId = :propertyId "
            + "AND l.listingType = :listingType AND l.status = :status AND l.userId = :userId "
            + "AND l.deleted = false")
    boolean existsByPropertyIdAndListingTypeAndStatusAndUserId(
            @Param("propertyId") UUID propertyId,
            @Param("listingType") ListingType listingType,
            @Param("status") ListingStatus status,
            @Param("userId") UUID userId);

    /**
     * Find published listings within geographical bounds with all filters.
     * Supports filtering by property attributes (bedrooms, bathrooms),
     * property type (category), and area. Sorted by published_at DESC.
     */
    @Query(value = """
            SELECT l.* FROM listings l
            INNER JOIN properties p ON l.property_id = p.property_id
            LEFT JOIN locations loc ON p.location_id = loc.location_id
            LEFT JOIN property_types pt ON p.property_type_id = pt.property_type_id
            LEFT JOIN property_categories pc ON pt.property_category_id = pc.property_category_id
            LEFT JOIN (
                SELECT listing_id, MAX(CASE WHEN boost_type = 'FEATURED' THEN 3 
                    WHEN boost_type = 'HOT_BADGE' THEN 2 ELSE 1 END) as priority
                FROM listing_boosts
                WHERE status = 'ACTIVE'
                  AND start_date <= CURRENT_DATE
                  AND end_date >= CURRENT_DATE
                  AND deleted = false
                GROUP BY listing_id
            ) lb ON l.listing_id = lb.listing_id
            LEFT JOIN property_attribute_values pav_bed
                ON p.property_id = pav_bed.property_id
                AND pav_bed.property_attribute_id IN (
                    SELECT pa.property_attribute_id FROM property_attributes pa
                    WHERE LOWER(pa.name) = 'phòng ngủ'
                )
            LEFT JOIN property_attribute_values pav_bath
                ON p.property_id = pav_bath.property_id
                AND pav_bath.property_attribute_id IN (
                    SELECT pa.property_attribute_id FROM property_attributes pa
                    WHERE LOWER(pa.name) = 'phòng tắm'
                )
            WHERE l.status = 'PUBLISHED'
              AND l.deleted = false
              AND p.deleted = false
              AND p.latitude >= :southLat
              AND p.latitude <= :northLat
              AND p.longitude >= :westLng
              AND p.longitude <= :eastLng
              AND (:listingType IS NULL OR l.listing_type = CAST(:listingType AS VARCHAR))
              AND (:minPrice IS NULL OR l.price >= :minPrice)
              AND (:maxPrice IS NULL OR l.price <= :maxPrice)
              AND (:searchText IS NULL OR (
                  LOWER(l.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                  LOWER(p.street_address) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                  LOWER(p.descriptions) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                  LOWER(loc.name) LIKE LOWER(CONCAT('%', :searchText, '%'))
              ))
              AND (:filterByCategory = false OR LOWER(pc.code) IN (:categories))
              AND (:propertyCategory IS NULL OR LOWER(pc.code) = LOWER(:propertyCategory))
              AND (:propertyType IS NULL OR LOWER(pt.code) = LOWER(:propertyType))
              AND (:bedrooms IS NULL OR pav_bed.value_number >= CAST(:bedrooms AS NUMERIC))
              AND (:bathrooms IS NULL OR pav_bath.value_number >= CAST(:bathrooms AS NUMERIC))
              AND (:area IS NULL OR COALESCE(p.usable_size_m2, p.land_size_m2) >= :area)
            ORDER BY COALESCE(lb.priority, 0) DESC, l.published_at DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    @SuppressWarnings("checkstyle:ParameterNumber")
    List<Listing> findPublishedWithinBoundsSortByPublishedAt(
            @Param("northLat") BigDecimal northLat,
            @Param("southLat") BigDecimal southLat,
            @Param("eastLng") BigDecimal eastLng,
            @Param("westLng") BigDecimal westLng,
            @Param("listingType") String listingType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("searchText") String searchText,
            @Param("categories") List<String> categories,
            @Param("filterByCategory") boolean filterByCategory,
            @Param("propertyCategory") String propertyCategory,
            @Param("propertyType") String propertyType,
            @Param("bedrooms") Integer bedrooms,
            @Param("bathrooms") Integer bathrooms,
            @Param("area") BigDecimal area,
            @Param("limit") int limit,
            @Param("offset") int offset);

    /**
     * Same as above but sorted by price ASC.
     */
    @Query(value = """
            SELECT l.* FROM listings l
            INNER JOIN properties p ON l.property_id = p.property_id
            LEFT JOIN locations loc ON p.location_id = loc.location_id
            LEFT JOIN property_types pt ON p.property_type_id = pt.property_type_id
            LEFT JOIN property_categories pc ON pt.property_category_id = pc.property_category_id
            LEFT JOIN (
                SELECT listing_id, MAX(CASE WHEN boost_type = 'FEATURED' THEN 3 
                    WHEN boost_type = 'HOT_BADGE' THEN 2 ELSE 1 END) as priority
                FROM listing_boosts
                WHERE status = 'ACTIVE'
                  AND start_date <= CURRENT_DATE
                  AND end_date >= CURRENT_DATE
                  AND deleted = false
                GROUP BY listing_id
            ) lb ON l.listing_id = lb.listing_id
            LEFT JOIN property_attribute_values pav_bed
                ON p.property_id = pav_bed.property_id
                AND pav_bed.property_attribute_id IN (
                    SELECT pa.property_attribute_id FROM property_attributes pa
                    WHERE LOWER(pa.name) = 'phòng ngủ'
                )
            LEFT JOIN property_attribute_values pav_bath
                ON p.property_id = pav_bath.property_id
                AND pav_bath.property_attribute_id IN (
                    SELECT pa.property_attribute_id FROM property_attributes pa
                    WHERE LOWER(pa.name) = 'phòng tắm'
                )
            WHERE l.status = 'PUBLISHED'
              AND l.deleted = false
              AND p.deleted = false
              AND p.latitude >= :southLat
              AND p.latitude <= :northLat
              AND p.longitude >= :westLng
              AND p.longitude <= :eastLng
              AND (:listingType IS NULL OR l.listing_type = CAST(:listingType AS VARCHAR))
              AND (:minPrice IS NULL OR l.price >= :minPrice)
              AND (:maxPrice IS NULL OR l.price <= :maxPrice)
              AND (:searchText IS NULL OR (
                  LOWER(l.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                  LOWER(p.street_address) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                  LOWER(p.descriptions) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                  LOWER(loc.name) LIKE LOWER(CONCAT('%', :searchText, '%'))
              ))
              AND (:filterByCategory = false OR LOWER(pc.code) IN (:categories))
              AND (:propertyCategory IS NULL OR LOWER(pc.code) = LOWER(:propertyCategory))
              AND (:propertyType IS NULL OR LOWER(pt.code) = LOWER(:propertyType))
              AND (:bedrooms IS NULL OR pav_bed.value_number >= CAST(:bedrooms AS NUMERIC))
              AND (:bathrooms IS NULL OR pav_bath.value_number >= CAST(:bathrooms AS NUMERIC))
              AND (:area IS NULL OR COALESCE(p.usable_size_m2, p.land_size_m2) >= :area)
            ORDER BY COALESCE(lb.priority, 0) DESC, l.price ASC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    @SuppressWarnings("checkstyle:ParameterNumber")
    List<Listing> findPublishedWithinBoundsSortByPriceAsc(
            @Param("northLat") BigDecimal northLat,
            @Param("southLat") BigDecimal southLat,
            @Param("eastLng") BigDecimal eastLng,
            @Param("westLng") BigDecimal westLng,
            @Param("listingType") String listingType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("searchText") String searchText,
            @Param("categories") List<String> categories,
            @Param("filterByCategory") boolean filterByCategory,
            @Param("propertyCategory") String propertyCategory,
            @Param("propertyType") String propertyType,
            @Param("bedrooms") Integer bedrooms,
            @Param("bathrooms") Integer bathrooms,
            @Param("area") BigDecimal area,
            @Param("limit") int limit,
            @Param("offset") int offset);

    /**
     * Same as above but sorted by price DESC.
     */
    @Query(value = """
            SELECT l.* FROM listings l
            INNER JOIN properties p ON l.property_id = p.property_id
            LEFT JOIN locations loc ON p.location_id = loc.location_id
            LEFT JOIN property_types pt ON p.property_type_id = pt.property_type_id
            LEFT JOIN property_categories pc ON pt.property_category_id = pc.property_category_id
            LEFT JOIN (
                SELECT listing_id, MAX(CASE WHEN boost_type = 'FEATURED' THEN 3 
                    WHEN boost_type = 'HOT_BADGE' THEN 2 ELSE 1 END) as priority
                FROM listing_boosts
                WHERE status = 'ACTIVE'
                  AND start_date <= CURRENT_DATE
                  AND end_date >= CURRENT_DATE
                  AND deleted = false
                GROUP BY listing_id
            ) lb ON l.listing_id = lb.listing_id
            LEFT JOIN property_attribute_values pav_bed
                ON p.property_id = pav_bed.property_id
                AND pav_bed.property_attribute_id IN (
                    SELECT pa.property_attribute_id FROM property_attributes pa
                    WHERE LOWER(pa.name) = 'phòng ngủ'
                )
            LEFT JOIN property_attribute_values pav_bath
                ON p.property_id = pav_bath.property_id
                AND pav_bath.property_attribute_id IN (
                    SELECT pa.property_attribute_id FROM property_attributes pa
                    WHERE LOWER(pa.name) = 'phòng tắm'
                )
            WHERE l.status = 'PUBLISHED'
              AND l.deleted = false
              AND p.deleted = false
              AND p.latitude >= :southLat
              AND p.latitude <= :northLat
              AND p.longitude >= :westLng
              AND p.longitude <= :eastLng
              AND (:listingType IS NULL OR l.listing_type = CAST(:listingType AS VARCHAR))
              AND (:minPrice IS NULL OR l.price >= :minPrice)
              AND (:maxPrice IS NULL OR l.price <= :maxPrice)
              AND (:searchText IS NULL OR (
                  LOWER(l.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                  LOWER(p.street_address) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                  LOWER(p.descriptions) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                  LOWER(loc.name) LIKE LOWER(CONCAT('%', :searchText, '%'))
              ))
              AND (:filterByCategory = false OR LOWER(pc.code) IN (:categories))
              AND (:propertyCategory IS NULL OR LOWER(pc.code) = LOWER(:propertyCategory))
              AND (:propertyType IS NULL OR LOWER(pt.code) = LOWER(:propertyType))
              AND (:bedrooms IS NULL OR pav_bed.value_number >= CAST(:bedrooms AS NUMERIC))
              AND (:bathrooms IS NULL OR pav_bath.value_number >= CAST(:bathrooms AS NUMERIC))
              AND (:area IS NULL OR COALESCE(p.usable_size_m2, p.land_size_m2) >= :area)
            ORDER BY COALESCE(lb.priority, 0) DESC, l.price DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    @SuppressWarnings("checkstyle:ParameterNumber")
    List<Listing> findPublishedWithinBoundsSortByPriceDesc(
            @Param("northLat") BigDecimal northLat,
            @Param("southLat") BigDecimal southLat,
            @Param("eastLng") BigDecimal eastLng,
            @Param("westLng") BigDecimal westLng,
            @Param("listingType") String listingType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("searchText") String searchText,
            @Param("categories") List<String> categories,
            @Param("filterByCategory") boolean filterByCategory,
            @Param("propertyCategory") String propertyCategory,
            @Param("propertyType") String propertyType,
            @Param("bedrooms") Integer bedrooms,
            @Param("bathrooms") Integer bathrooms,
            @Param("area") BigDecimal area,
            @Param("limit") int limit,
            @Param("offset") int offset);

    /**
     * Same as above but sorted by created_at DESC.
     */
    @Query(value = """
            SELECT l.* FROM listings l
            INNER JOIN properties p ON l.property_id = p.property_id
            LEFT JOIN locations loc ON p.location_id = loc.location_id
            LEFT JOIN property_types pt ON p.property_type_id = pt.property_type_id
            LEFT JOIN property_categories pc ON pt.property_category_id = pc.property_category_id
            LEFT JOIN (
                SELECT listing_id, MAX(CASE WHEN boost_type = 'FEATURED' THEN 3 
                    WHEN boost_type = 'HOT_BADGE' THEN 2 ELSE 1 END) as priority
                FROM listing_boosts
                WHERE status = 'ACTIVE'
                  AND start_date <= CURRENT_DATE
                  AND end_date >= CURRENT_DATE
                  AND deleted = false
                GROUP BY listing_id
            ) lb ON l.listing_id = lb.listing_id
            LEFT JOIN property_attribute_values pav_bed
                ON p.property_id = pav_bed.property_id
                AND pav_bed.property_attribute_id IN (
                    SELECT pa.property_attribute_id FROM property_attributes pa
                    WHERE LOWER(pa.name) = 'phòng ngủ'
                )
            LEFT JOIN property_attribute_values pav_bath
                ON p.property_id = pav_bath.property_id
                AND pav_bath.property_attribute_id IN (
                    SELECT pa.property_attribute_id FROM property_attributes pa
                    WHERE LOWER(pa.name) = 'phòng tắm'
                )
            WHERE l.status = 'PUBLISHED'
              AND l.deleted = false
              AND p.deleted = false
              AND p.latitude >= :southLat
              AND p.latitude <= :northLat
              AND p.longitude >= :westLng
              AND p.longitude <= :eastLng
              AND (:listingType IS NULL OR l.listing_type = CAST(:listingType AS VARCHAR))
              AND (:minPrice IS NULL OR l.price >= :minPrice)
              AND (:maxPrice IS NULL OR l.price <= :maxPrice)
              AND (:searchText IS NULL OR (
                  LOWER(l.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                  LOWER(p.street_address) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                  LOWER(p.descriptions) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                  LOWER(loc.name) LIKE LOWER(CONCAT('%', :searchText, '%'))
              ))
              AND (:filterByCategory = false OR LOWER(pc.code) IN (:categories))
              AND (:propertyCategory IS NULL OR LOWER(pc.code) = LOWER(:propertyCategory))
              AND (:propertyType IS NULL OR LOWER(pt.code) = LOWER(:propertyType))
              AND (:bedrooms IS NULL OR pav_bed.value_number >= CAST(:bedrooms AS NUMERIC))
              AND (:bathrooms IS NULL OR pav_bath.value_number >= CAST(:bathrooms AS NUMERIC))
              AND (:area IS NULL OR COALESCE(p.usable_size_m2, p.land_size_m2) >= :area)
            ORDER BY COALESCE(lb.priority, 0) DESC, l.created_at DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    @SuppressWarnings("checkstyle:ParameterNumber")
    List<Listing> findPublishedWithinBoundsSortByCreatedAt(
            @Param("northLat") BigDecimal northLat,
            @Param("southLat") BigDecimal southLat,
            @Param("eastLng") BigDecimal eastLng,
            @Param("westLng") BigDecimal westLng,
            @Param("listingType") String listingType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("searchText") String searchText,
            @Param("categories") List<String> categories,
            @Param("filterByCategory") boolean filterByCategory,
            @Param("propertyCategory") String propertyCategory,
            @Param("propertyType") String propertyType,
            @Param("bedrooms") Integer bedrooms,
            @Param("bathrooms") Integer bathrooms,
            @Param("area") BigDecimal area,
            @Param("limit") int limit,
            @Param("offset") int offset);

    /**
     * Count published listings within geographical bounds with all filters.
     */
    @Query(value = """
            SELECT COUNT(*) FROM listings l
            INNER JOIN properties p ON l.property_id = p.property_id
            LEFT JOIN locations loc ON p.location_id = loc.location_id
            LEFT JOIN property_types pt ON p.property_type_id = pt.property_type_id
            LEFT JOIN property_categories pc ON pt.property_category_id = pc.property_category_id
            LEFT JOIN property_attribute_values pav_bed
                ON p.property_id = pav_bed.property_id
                AND pav_bed.property_attribute_id IN (
                    SELECT pa.property_attribute_id FROM property_attributes pa
                    WHERE LOWER(pa.name) = 'phòng ngủ'
                )
            LEFT JOIN property_attribute_values pav_bath
                ON p.property_id = pav_bath.property_id
                AND pav_bath.property_attribute_id IN (
                    SELECT pa.property_attribute_id FROM property_attributes pa
                    WHERE LOWER(pa.name) = 'phòng tắm'
                )
            WHERE l.status = 'PUBLISHED'
              AND l.deleted = false
              AND p.deleted = false
              AND p.latitude >= :southLat
              AND p.latitude <= :northLat
              AND p.longitude >= :westLng
              AND p.longitude <= :eastLng
              AND (:listingType IS NULL OR l.listing_type = CAST(:listingType AS VARCHAR))
              AND (:minPrice IS NULL OR l.price >= :minPrice)
              AND (:maxPrice IS NULL OR l.price <= :maxPrice)
              AND (:searchText IS NULL OR (
                  LOWER(l.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                  LOWER(p.street_address) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                  LOWER(p.descriptions) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                  LOWER(loc.name) LIKE LOWER(CONCAT('%', :searchText, '%'))
              ))
              AND (:filterByCategory = false OR LOWER(pc.code) IN (:categories))
              AND (:propertyCategory IS NULL OR LOWER(pc.code) = LOWER(:propertyCategory))
              AND (:propertyType IS NULL OR LOWER(pt.code) = LOWER(:propertyType))
              AND (:bedrooms IS NULL OR pav_bed.value_number >= CAST(:bedrooms AS NUMERIC))
              AND (:bathrooms IS NULL OR pav_bath.value_number >= CAST(:bathrooms AS NUMERIC))
              AND (:area IS NULL OR COALESCE(p.usable_size_m2, p.land_size_m2) >= :area)
            """, nativeQuery = true)
    @SuppressWarnings("checkstyle:ParameterNumber")
    Long countPublishedWithinBounds(
            @Param("northLat") BigDecimal northLat,
            @Param("southLat") BigDecimal southLat,
            @Param("eastLng") BigDecimal eastLng,
            @Param("westLng") BigDecimal westLng,
            @Param("listingType") String listingType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("searchText") String searchText,
            @Param("categories") List<String> categories,
            @Param("filterByCategory") boolean filterByCategory,
            @Param("propertyCategory") String propertyCategory,
            @Param("propertyType") String propertyType,
            @Param("bedrooms") Integer bedrooms,
            @Param("bathrooms") Integer bathrooms,
            @Param("area") BigDecimal area);
}
