package com.sep.realvista.infrastructure.persistence.listing;

import com.sep.realvista.domain.listing.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA repository interface for Listing entity.
 */
@Repository
public interface ListingJpaRepository extends JpaRepository<Listing, UUID> {
}