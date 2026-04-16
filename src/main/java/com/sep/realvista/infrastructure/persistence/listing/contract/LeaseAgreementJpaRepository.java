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

    @Query("SELECT la FROM LeaseAgreement la WHERE la.propertyId = :propertyId AND la.deleted = false")
    Page<LeaseAgreement> findByPropertyId(@Param("propertyId") UUID propertyId, Pageable pageable);

    @Query("SELECT la FROM LeaseAgreement la WHERE la.renterId = :renterId AND la.deleted = false")
    Page<LeaseAgreement> findByRenterId(@Param("renterId") UUID renterId, Pageable pageable);

    @Query("SELECT la FROM LeaseAgreement la "
            + "WHERE la.renterId = :renterId AND la.status = :status AND la.deleted = false")
    Page<LeaseAgreement> findByRenterIdAndStatus(
            @Param("renterId") UUID renterId,
            @Param("status") LeaseStatus status,
            Pageable pageable
    );

    @Query("SELECT la FROM LeaseAgreement la WHERE la.landlordId = :landlordId AND la.deleted = false")
    Page<LeaseAgreement> findByLandlordId(@Param("landlordId") UUID landlordId, Pageable pageable);

    @Query("SELECT la FROM LeaseAgreement la "
            + "WHERE la.landlordId = :landlordId AND la.status = :status AND la.deleted = false")
    Page<LeaseAgreement> findByLandlordIdAndStatus(
            @Param("landlordId") UUID landlordId,
            @Param("status") LeaseStatus status,
            Pageable pageable
    );

    @Query("SELECT la FROM LeaseAgreement la "
            + "WHERE la.propertyId = :propertyId AND la.status = :status AND la.deleted = false")
    List<LeaseAgreement> findByPropertyIdAndStatus(
            @Param("propertyId") UUID propertyId,
            @Param("status") LeaseStatus status
    );

    @Query("SELECT la FROM LeaseAgreement la WHERE la.agentId = :agentId AND la.deleted = false")
    Page<LeaseAgreement> findByAgentId(@Param("agentId") UUID agentId, Pageable pageable);

    @Query("SELECT la FROM LeaseAgreement la "
            + "WHERE la.agentId = :agentId AND la.status = :status AND la.deleted = false")
    Page<LeaseAgreement> findByAgentIdAndStatus(
            @Param("agentId") UUID agentId,
            @Param("status") LeaseStatus status,
            Pageable pageable
    );
}
