package com.sep.realvista.domain.listing.contract;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for LeaseAgreement aggregate.
 * <p>
 * Defines the contract for lease agreement data access.
 * Implementation is provided in the infrastructure layer.
 */
public interface LeaseAgreementRepository {

    LeaseAgreement save(LeaseAgreement leaseAgreement);

    Optional<LeaseAgreement> findById(UUID id);

    Optional<LeaseAgreement> findByDocusignEnvelopeId(String envelopeId);

    List<LeaseAgreement> findBySignedDocumentStatusIn(List<SignedDocumentStatus> statuses, int limit);

    Page<LeaseAgreement> findByPropertyId(UUID propertyId, Pageable pageable);

    Page<LeaseAgreement> findByRenterId(UUID renterId, Pageable pageable);

    Page<LeaseAgreement> findByRenterIdAndStatus(UUID renterId, LeaseStatus status, Pageable pageable);

    Page<LeaseAgreement> findByLandlordId(UUID landlordId, Pageable pageable);

    Page<LeaseAgreement> findByLandlordIdAndStatus(UUID landlordId, LeaseStatus status, Pageable pageable);

    List<LeaseAgreement> findActiveLeasesByPropertyId(UUID propertyId);

    List<LeaseAgreement> findActiveLeasesWithAgentByPropertyId(UUID propertyId);

    List<LeaseAgreement> findActiveLeasesEndingBefore(LocalDate date);

    List<LeaseAgreement> findActiveLeasesEndingOn(LocalDate date);

    Page<LeaseAgreement> findByAgentId(UUID agentId, Pageable pageable);

    Page<LeaseAgreement> findByAgentIdAndStatus(UUID agentId, LeaseStatus status, Pageable pageable);
}
