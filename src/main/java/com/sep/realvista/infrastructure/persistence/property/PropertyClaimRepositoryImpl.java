package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.claim.PropertyClaim;
import com.sep.realvista.domain.property.claim.PropertyClaimRepository;
import com.sep.realvista.domain.property.claim.PropertyClaimStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PropertyClaimRepositoryImpl implements PropertyClaimRepository {

    private final PropertyClaimJpaRepository jpaRepository;

    @Override
    public PropertyClaim save(PropertyClaim claim) {
        return jpaRepository.save(claim);
    }

    @Override
    public Optional<PropertyClaim> findById(UUID claimId) {
        return jpaRepository.findById(claimId);
    }

    @Override
    public List<PropertyClaim> findPendingByPropertyId(UUID propertyId) {
        return jpaRepository.findPendingByPropertyId(propertyId);
    }

    @Override
    public List<PropertyClaim> findExpiredPendingBefore(LocalDateTime cutoff) {
        return jpaRepository.findByStatusAndExpiresAtBefore(PropertyClaimStatus.PENDING, cutoff);
    }
}
