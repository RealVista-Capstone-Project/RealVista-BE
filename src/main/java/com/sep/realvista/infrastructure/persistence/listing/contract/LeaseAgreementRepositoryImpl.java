package com.sep.realvista.infrastructure.persistence.listing.contract;

import com.sep.realvista.domain.listing.contract.LeaseAgreement;
import com.sep.realvista.domain.listing.contract.LeaseAgreementRepository;
import com.sep.realvista.domain.listing.contract.LeaseStatus;
import com.sep.realvista.domain.listing.contract.SignedDocumentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
    public List<LeaseAgreement> findBySignedDocumentStatusIn(List<SignedDocumentStatus> statuses, int limit) {
        return jpaRepository.findBySignedDocumentStatusIn(statuses, PageRequest.of(0, limit));
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
    public Page<LeaseAgreement> findByRenterIdAndStatus(UUID renterId, LeaseStatus status, Pageable pageable) {
        return jpaRepository.findByRenterIdAndStatus(renterId, status, pageable);
    }

    @Override
    public Page<LeaseAgreement> findByLandlordId(UUID landlordId, Pageable pageable) {
        return jpaRepository.findByLandlordId(landlordId, pageable);
    }

    @Override
    public Page<LeaseAgreement> findByLandlordIdAndStatus(UUID landlordId, LeaseStatus status, Pageable pageable) {
        return jpaRepository.findByLandlordIdAndStatus(landlordId, status, pageable);
    }

    @Override
    public List<LeaseAgreement> findActiveLeasesByPropertyId(UUID propertyId) {
        return jpaRepository.findByPropertyIdAndStatus(propertyId, LeaseStatus.ACTIVE);
    }

    @Override
    public List<LeaseAgreement> findActiveLeasesWithAgentByPropertyId(UUID propertyId) {
        return jpaRepository.findActiveWithAgentByPropertyId(propertyId);
    }

    @Override
    public List<LeaseAgreement> findActiveLeasesEndingBefore(LocalDate date) {
        return jpaRepository.findByStatusAndLeaseEndDateBefore(LeaseStatus.ACTIVE, date);
    }

    @Override
    public List<LeaseAgreement> findActiveLeasesEndingOn(LocalDate date) {
        return jpaRepository.findByStatusAndLeaseEndDate(LeaseStatus.ACTIVE, date);
    }

    @Override
    public Page<LeaseAgreement> findByAgentId(UUID agentId, Pageable pageable) {
        return jpaRepository.findByAgentId(agentId, pageable);
    }

    @Override
    public Page<LeaseAgreement> findByAgentIdAndStatus(UUID agentId, LeaseStatus status, Pageable pageable) {
        return jpaRepository.findByAgentIdAndStatus(agentId, status, pageable);
    }
}
