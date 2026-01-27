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

    List<ListingMedia> findByListingId(UUID listingId);

    @Query("SELECT lm FROM ListingMedia lm WHERE lm.listingId = :listingId "
            + "AND lm.deleted = false ORDER BY lm.displayOrder ASC")
    List<ListingMedia> findByListingIdOrderByDisplayOrder(@Param("listingId") UUID listingId);

    @Query("SELECT lm FROM ListingMedia lm WHERE lm.listingId = :listingId "
            + "AND lm.isPrimary = true AND lm.deleted = false")
    Optional<ListingMedia> findPrimaryByListingId(@Param("listingId") UUID listingId);
}
