package com.sep.realvista.infrastructure.persistence.listing;

import com.sep.realvista.domain.listing.ListingMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for ListingMedia entity.
 */
public interface ListingMediaJpaRepository extends JpaRepository<ListingMedia, UUID> {

        @Query("SELECT lm FROM ListingMedia lm "
                        + "LEFT JOIN FETCH lm.propertyMedia pm "
                        + "WHERE lm.listingId = :listingId "
                        + "AND lm.deleted = false "
                        + "AND (pm IS NULL OR pm.deleted = false) "
                        + "ORDER BY lm.displayOrder ASC")
        List<ListingMedia> findByListingId(UUID listingId);

        @Query("SELECT lm FROM ListingMedia lm "
                        + "LEFT JOIN FETCH lm.propertyMedia "
                        + "WHERE lm.listingId = :listingId "
                        + "ORDER BY lm.displayOrder ASC")
        List<ListingMedia> findAllByListingId(@Param("listingId") UUID listingId);

        @Query("SELECT lm FROM ListingMedia lm "
                        + "LEFT JOIN FETCH lm.propertyMedia pm "
                        + "WHERE lm.listingId = :listingId "
                        + "AND lm.deleted = false "
                        + "AND (pm IS NULL OR pm.deleted = false) "
                        + "ORDER BY lm.displayOrder ASC")
        List<ListingMedia> findByListingIdOrderByDisplayOrder(@Param("listingId") UUID listingId);

        @Query("SELECT lm FROM ListingMedia lm WHERE lm.listingId = :listingId "
                        + "AND lm.isPrimary = true AND lm.deleted = false")
        Optional<ListingMedia> findPrimaryByListingId(@Param("listingId") UUID listingId);

        /**
         * Batch fetch primary media for multiple listings.
         * Returns only primary (isPrimary = true) media for each listing.
         */
        @Query("SELECT lm FROM ListingMedia lm WHERE lm.listingId IN :listingIds "
                        + "AND lm.isPrimary = true AND lm.deleted = false")
        List<ListingMedia> findPrimaryByListingIds(@Param("listingIds") List<UUID> listingIds);

        /**
         * Find all listing IDs that have 3D media among the given listing IDs.
         * Returns distinct listing IDs that have at least one non-deleted 3D media.
         */
        @Query("SELECT DISTINCT lm.listingId FROM ListingMedia lm "
                        + "WHERE lm.listingId IN :listingIds "
                        + "AND lm.propertyMedia.mediaType = '3D' "
                        + "AND lm.deleted = false")
        List<UUID> findListingIdsWith3DMedia(@Param("listingIds") List<UUID> listingIds);
}
