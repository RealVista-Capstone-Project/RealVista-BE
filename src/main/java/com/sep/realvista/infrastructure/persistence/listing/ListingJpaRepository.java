package com.sep.realvista.infrastructure.persistence.listing;

import com.sep.realvista.domain.listing.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ListingJpaRepository extends JpaRepository<Listing, UUID>, JpaSpecificationExecutor<Listing> {
}
