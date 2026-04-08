package com.sep.realvista.infrastructure.persistence.listing.contract;

import com.sep.realvista.domain.listing.contract.LeaseAgreement;
import com.sep.realvista.domain.listing.contract.LeaseAgreementRepository;
import com.sep.realvista.domain.listing.contract.LeaseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LeaseAgreementRepositoryImpl implements LeaseAgreementRepository {

    private final LeaseAgreementJpaRepository jpaRepository;

    @Override
    public LeaseAgreement save(LeaseAgreement leaseAgreement) {
        return jpaRepository.save(leaseAgreement);
    }

    @Override
    public Optional<LeaseAgreement> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<LeaseAgreement> findByDocusignEnvelopeId(String envelopeId) {
        return jpaRepository.findByDocusignEnvelopeId(envelopeId);
    }

    @Override
    public Page<LeaseAgreement> findByPropertyId(UUID propertyId, Pageable pageable) {
        return jpaRepository.findByPropertyId(propertyId, pageable);
    }

    @Override
    public Page<LeaseAgreement> findByRenterId(UUID renterId, Pageable pageable) {
        return jpaRepository.findByRenterId(renterId, pageable);
    }

    @Override
    public Page<LeaseAgreement> findByLandlordId(UUID landlordId, Pageable pageable) {
        return jpaRepository.findByLandlordId(landlordId, pageable);
    }

    @Override
    public List<LeaseAgreement> findActiveLeasesByPropertyId(UUID propertyId) {
        return jpaRepository.findByPropertyIdAndStatus(propertyId, LeaseStatus.ACTIVE);
    }
}
