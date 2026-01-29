package com.sep.realvista.infrastructure.persistence.listing;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT l FROM Listing l WHERE l.listingType = :listingType AND l.status = :status AND l.deleted = false")
    List<Listing> findByListingTypeAndStatus(@Param("listingType") ListingType listingType, @Param("status") ListingStatus status);

    @Query("SELECT l FROM Listing l WHERE l.listingId = :id AND l.deleted = false")
    Optional<Listing> findActiveById(@Param("id") UUID id);
}
