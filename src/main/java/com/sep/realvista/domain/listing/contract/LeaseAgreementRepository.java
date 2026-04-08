package com.sep.realvista.domain.listing.contract;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    Page<LeaseAgreement> findByPropertyId(UUID propertyId, Pageable pageable);

    Page<LeaseAgreement> findByRenterId(UUID renterId, Pageable pageable);

    Page<LeaseAgreement> findByLandlordId(UUID landlordId, Pageable pageable);

    List<LeaseAgreement> findActiveLeasesByPropertyId(UUID propertyId);
}
