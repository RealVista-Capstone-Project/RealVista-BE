package com.sep.realvista.infrastructure.persistence.listing.contract;

import com.sep.realvista.domain.listing.contract.LeaseAgreement;
import com.sep.realvista.domain.listing.contract.LeaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaseAgreementJpaRepository extends JpaRepository<LeaseAgreement, UUID> {

    Optional<LeaseAgreement> findByDocusignEnvelopeId(String envelopeId);

    @Query("SELECT la FROM LeaseAgreement la WHERE la.listingId = :listingId AND la.deleted = false")
    Page<LeaseAgreement> findByListingId(@Param("listingId") UUID listingId, Pageable pageable);

    @Query("SELECT la FROM LeaseAgreement la WHERE la.renterId = :renterId AND la.deleted = false")
    Page<LeaseAgreement> findByRenterId(@Param("renterId") UUID renterId, Pageable pageable);

    @Query("SELECT la FROM LeaseAgreement la WHERE la.landlordId = :landlordId AND la.deleted = false")
    Page<LeaseAgreement> findByLandlordId(@Param("landlordId") UUID landlordId, Pageable pageable);

    @Query("SELECT la FROM LeaseAgreement la "
            + "WHERE la.listingId = :listingId AND la.status = :status AND la.deleted = false")
    List<LeaseAgreement> findByListingIdAndStatus(
            @Param("listingId") UUID listingId,
            @Param("status") LeaseStatus status
    );
}
