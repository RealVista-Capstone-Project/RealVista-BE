package com.sep.realvista.infrastructure.persistence.listing;

import com.sep.realvista.domain.listing.similarity.SimilarListing;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Custom repository implementation for Listing-specific queries.
 * Contains complex native queries that cannot be expressed through Spring Data JPA.
 */
@Repository
public class ListingCustomRepository {

    // @formatter:off - Checkstyle has trouble parsing SQL in text blocks
    private static final String SIMILAR_LISTINGS_SQL = """
                WITH current_listing AS (
                    SELECT
                        l.listing_id,
                        l.property_id,
                        l.listing_type,
                        l.price,
                        p.usable_size_m2 as area,
                        p.property_type_id,
                        p.location_id,
                        loc.parent_id as location_parent_id,
                        pt.name as property_type_name,
                        loc.name as location_name
                    FROM listings l
                    JOIN properties p ON l.property_id = p.property_id
                    JOIN property_types pt ON p.property_type_id = pt.property_type_id
                    JOIN locations loc ON p.location_id = loc.location_id
                    WHERE l.listing_id = CAST(:listingId AS UUID)
                      AND l.deleted = false
                      AND p.deleted = false
                ),
                current_attributes AS (
                    SELECT
                        pav.property_attribute_id,
                        pav.value_number
                    FROM property_attribute_values pav
                    JOIN current_listing cl ON pav.property_id = cl.property_id
                    WHERE pav.value_number IS NOT NULL
                      AND pav.deleted = false
                ),
                candidate_listings AS (
                    SELECT
                        l.listing_id,
                        l.property_id,
                        l.name,
                        l.slug,
                        l.listing_type,
                        l.status,
                        l.price,
                        l.published_at,
                        p.usable_size_m2 as area,
                        p.property_type_id,
                        p.location_id,
                        loc.parent_id as location_parent_id,
                        pt.name as property_type_name,
                        loc.name as location_name,
                        p.street_address as street_address,
                        loc.name as ward_name,
                        loc_district.name as district_name,
                        loc_city.name as city_name,
                        (SELECT pm.media_url FROM listing_medias lm
                         JOIN property_medias pm ON lm.property_media_id = pm.property_media_id
                         WHERE lm.listing_id = l.listing_id
                           AND pm.media_type = 'IMAGE'
                           AND lm.deleted = false
                           AND pm.deleted = false
                         ORDER BY lm.display_order ASC
                         LIMIT 1) as thumbnail_url
                    FROM listings l
                    JOIN properties p ON l.property_id = p.property_id
                    JOIN property_types pt ON p.property_type_id = pt.property_type_id
                    JOIN locations loc ON p.location_id = loc.location_id
                    LEFT JOIN locations loc_district ON loc.parent_id = loc_district.location_id
                    LEFT JOIN locations loc_city ON loc_district.parent_id = loc_city.location_id
                    JOIN current_listing cl ON l.listing_type = cl.listing_type
                    WHERE l.status = 'PUBLISHED'
                      AND l.listing_id != CAST(:listingId AS UUID)
                      AND l.deleted = false
                      AND p.deleted = false
                ),
                candidate_attributes AS (
                    SELECT
                        cl.listing_id,
                        cav.property_attribute_id,
                        cav.value_number
                    FROM candidate_listings cl
                    JOIN property_attribute_values cav ON cav.property_id = cl.property_id
                    WHERE cav.value_number IS NOT NULL
                      AND cav.deleted = false
                ),
                attribute_similarity AS (
                    SELECT
                        ca.listing_id,
                        COUNT(*) FILTER (
                            WHERE ca.property_attribute_id IN (
                                SELECT property_attribute_id FROM current_attributes
                            )
                        ) as common_count,
                        COUNT(*) FILTER (
                            WHERE ca.property_attribute_id IN (
                                SELECT property_attribute_id FROM current_attributes
                            )
                            AND (
                                ca.value_number = cur_attr.value_number
                                OR (
                                    cur_attr.value_number <= 10
                                    AND ABS(ca.value_number - cur_attr.value_number) <= 1
                                )
                                OR (
                                    cur_attr.value_number > 10
                                    AND ABS(ca.value_number - cur_attr.value_number)
                                        / ((ca.value_number + cur_attr.value_number) / 2) <= 0.20
                                )
                            )
                        ) as similar_count
                    FROM candidate_attributes ca
                    LEFT JOIN current_attributes cur_attr
                        ON ca.property_attribute_id = cur_attr.property_attribute_id
                    GROUP BY ca.listing_id
                )
                SELECT
                    cl.listing_id,
                    cl.property_id,
                    cl.property_type_id,
                    cl.location_id,
                    cl.name,
                    cl.slug,
                    cl.listing_type,
                    cl.status,
                    cl.price,
                    cl.area,
                    cl.location_name,
                    cl.street_address,
                    cl.ward_name,
                    cl.district_name,
                    cl.city_name,
                    cl.property_type_name,
                    cl.thumbnail_url,
                    cl.published_at,
                    (
                        CASE
                            WHEN cl.property_type_id = (SELECT property_type_id FROM current_listing) THEN 0.30
                            ELSE 0.0
                        END +
                        CASE
                            WHEN cl.location_id = (SELECT location_id FROM current_listing) THEN 0.15
                            WHEN cl.location_parent_id = (SELECT location_parent_id FROM current_listing)
                                 AND cl.location_parent_id IS NOT NULL THEN 0.075
                            ELSE 0.0
                        END +
                        CASE
                            WHEN cl.price IS NOT NULL
                              AND (SELECT price FROM current_listing) IS NOT NULL THEN
                                CASE
                                    WHEN ABS(cl.price - (SELECT price FROM current_listing))
                                        / NULLIF((cl.price + (SELECT price FROM current_listing))
                                            / 2, 0) <= 0.20 THEN 0.25
                                    WHEN ABS(cl.price - (SELECT price FROM current_listing))
                                        / NULLIF((cl.price + (SELECT price FROM current_listing))
                                            / 2, 0) <= 0.30 THEN 0.175
                                    WHEN ABS(cl.price - (SELECT price FROM current_listing))
                                        / NULLIF((cl.price + (SELECT price FROM current_listing))
                                            / 2, 0) <= 0.40 THEN 0.10
                                    ELSE 0.0
                                END
                            ELSE 0.0
                        END +
                        CASE
                            WHEN cl.area IS NOT NULL
                              AND (SELECT area FROM current_listing) IS NOT NULL THEN
                                CASE
                                    WHEN ABS(cl.area - (SELECT area FROM current_listing))
                                        / NULLIF((cl.area + (SELECT area FROM current_listing))
                                            / 2, 0) <= 0.15 THEN 0.15
                                    WHEN ABS(cl.area - (SELECT area FROM current_listing))
                                        / NULLIF((cl.area + (SELECT area FROM current_listing))
                                            / 2, 0) <= 0.25 THEN 0.09
                                    WHEN ABS(cl.area - (SELECT area FROM current_listing))
                                        / NULLIF((cl.area + (SELECT area FROM current_listing))
                                            / 2, 0) <= 0.40 THEN 0.045
                                    ELSE 0.0
                                END
                            ELSE 0.0
                        END +
                        CASE
                            WHEN asim.common_count > 0 THEN
                                (asim.similar_count::NUMERIC / asim.common_count) * 0.15
                            ELSE 0.0
                        END
                    ) as similarity_score
                FROM candidate_listings cl
                LEFT JOIN attribute_similarity asim ON cl.listing_id = asim.listing_id
                ORDER BY similarity_score DESC, cl.published_at DESC
                LIMIT CAST(:limit AS INTEGER)
                """;
    // @formatter:on

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Find similar listings based on property type, location, price, area, and common attributes.
     * Uses a complex native query with CTEs to calculate similarity scores.
     *
     * Similarity scoring:
     * - Property Type (30%): exact match
     * - Location (15%): same location=100%, same parent location (district)=50%
     * - Price Range (25%): ±20%=100%, ±30%=70%, ±40%=40%
     * - Area (15%): ±15%=100%, ±25%=60%, ±40%=30%
     * - Common Attributes (15%): ratio of similar NUMBER attributes
     *
     * @param listingId the reference listing ID
     * @param limit maximum number of results to return
     * @return list of similar listings with similarity scores
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<SimilarListing> findSimilarListings(UUID listingId, int limit) {
        return (List<SimilarListing>) entityManager.createNativeQuery(SIMILAR_LISTINGS_SQL, "SimilarListingMapping")
                .setParameter("listingId", listingId.toString())
                .setParameter("limit", limit)
                .getResultList();
    }
}
