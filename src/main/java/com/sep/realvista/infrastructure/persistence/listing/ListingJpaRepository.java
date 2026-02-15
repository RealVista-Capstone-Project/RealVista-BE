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
              AND p.latitude >= :#{#bounds.southLat}
              AND p.latitude <= :#{#bounds.northLat}
              AND p.longitude >= :#{#bounds.westLng}
              AND p.longitude <= :#{#bounds.eastLng}
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
              AND (:bedrooms IS NULL OR pav_bed.value_number >= CAST(:bedrooms AS NUMERIC))
              AND (:bathrooms IS NULL OR pav_bath.value_number >= CAST(:bathrooms AS NUMERIC))
              AND (:area IS NULL OR COALESCE(p.usable_size_m2, p.land_size_m2) >= :area)
            ORDER BY l.published_at DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    @SuppressWarnings("checkstyle:ParameterNumber")
    List<Listing> findPublishedWithinBoundsSortByPublishedAt(
            @Param("bounds") com.sep.realvista.domain.listing.search.MapBounds bounds,
            @Param("listingType") String listingType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("searchText") String searchText,
            @Param("categories") List<String> categories,
            @Param("filterByCategory") boolean filterByCategory,
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
              AND p.latitude >= :#{#bounds.southLat}
              AND p.latitude <= :#{#bounds.northLat}
              AND p.longitude >= :#{#bounds.westLng}
              AND p.longitude <= :#{#bounds.eastLng}
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
              AND (:bedrooms IS NULL OR pav_bed.value_number >= CAST(:bedrooms AS NUMERIC))
              AND (:bathrooms IS NULL OR pav_bath.value_number >= CAST(:bathrooms AS NUMERIC))
              AND (:area IS NULL OR COALESCE(p.usable_size_m2, p.land_size_m2) >= :area)
            ORDER BY l.price ASC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    @SuppressWarnings("checkstyle:ParameterNumber")
    List<Listing> findPublishedWithinBoundsSortByPriceAsc(
            @Param("bounds") com.sep.realvista.domain.listing.search.MapBounds bounds,
            @Param("listingType") String listingType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("searchText") String searchText,
            @Param("categories") List<String> categories,
            @Param("filterByCategory") boolean filterByCategory,
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
              AND p.latitude >= :#{#bounds.southLat}
              AND p.latitude <= :#{#bounds.northLat}
              AND p.longitude >= :#{#bounds.westLng}
              AND p.longitude <= :#{#bounds.eastLng}
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
              AND (:bedrooms IS NULL OR pav_bed.value_number >= CAST(:bedrooms AS NUMERIC))
              AND (:bathrooms IS NULL OR pav_bath.value_number >= CAST(:bathrooms AS NUMERIC))
              AND (:area IS NULL OR COALESCE(p.usable_size_m2, p.land_size_m2) >= :area)
            ORDER BY l.price DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    @SuppressWarnings("checkstyle:ParameterNumber")
    List<Listing> findPublishedWithinBoundsSortByPriceDesc(
            @Param("bounds") com.sep.realvista.domain.listing.search.MapBounds bounds,
            @Param("listingType") String listingType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("searchText") String searchText,
            @Param("categories") List<String> categories,
            @Param("filterByCategory") boolean filterByCategory,
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
              AND p.latitude >= :#{#bounds.southLat}
              AND p.latitude <= :#{#bounds.northLat}
              AND p.longitude >= :#{#bounds.westLng}
              AND p.longitude <= :#{#bounds.eastLng}
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
              AND (:bedrooms IS NULL OR pav_bed.value_number >= CAST(:bedrooms AS NUMERIC))
              AND (:bathrooms IS NULL OR pav_bath.value_number >= CAST(:bathrooms AS NUMERIC))
              AND (:area IS NULL OR COALESCE(p.usable_size_m2, p.land_size_m2) >= :area)
            ORDER BY l.created_at DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    @SuppressWarnings("checkstyle:ParameterNumber")
    List<Listing> findPublishedWithinBoundsSortByCreatedAt(
            @Param("bounds") com.sep.realvista.domain.listing.search.MapBounds bounds,
            @Param("listingType") String listingType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("searchText") String searchText,
            @Param("categories") List<String> categories,
            @Param("filterByCategory") boolean filterByCategory,
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
              AND p.latitude >= :#{#bounds.southLat}
              AND p.latitude <= :#{#bounds.northLat}
              AND p.longitude >= :#{#bounds.westLng}
              AND p.longitude <= :#{#bounds.eastLng}
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
              AND (:bedrooms IS NULL OR pav_bed.value_number >= CAST(:bedrooms AS NUMERIC))
              AND (:bathrooms IS NULL OR pav_bath.value_number >= CAST(:bathrooms AS NUMERIC))
              AND (:area IS NULL OR COALESCE(p.usable_size_m2, p.land_size_m2) >= :area)
            """, nativeQuery = true)
    @SuppressWarnings("checkstyle:ParameterNumber")
    Long countPublishedWithinBounds(
            @Param("bounds") com.sep.realvista.domain.listing.search.MapBounds bounds,
            @Param("listingType") String listingType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("searchText") String searchText,
            @Param("categories") List<String> categories,
            @Param("filterByCategory") boolean filterByCategory,
            @Param("bedrooms") Integer bedrooms,
            @Param("bathrooms") Integer bathrooms,
            @Param("area") BigDecimal area);
}
