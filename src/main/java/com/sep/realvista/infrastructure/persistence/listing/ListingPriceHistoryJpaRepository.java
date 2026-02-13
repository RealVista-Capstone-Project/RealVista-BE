package com.sep.realvista.infrastructure.persistence.listing;

import com.sep.realvista.domain.listing.analytics.ListingPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for ListingPriceHistory entity.
 * All queries exclude soft-deleted records (deleted = false).
 */
public interface ListingPriceHistoryJpaRepository extends JpaRepository<ListingPriceHistory, UUID> {

    @Query("SELECT ph FROM ListingPriceHistory ph WHERE ph.listingId = :listingId "
            + "AND ph.deleted = false ORDER BY ph.createdAt DESC")
    List<ListingPriceHistory> findByListingIdOrderByCreatedAtDesc(@Param("listingId") UUID listingId);
}
