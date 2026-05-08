package com.sep.realvista.domain.property.claim;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyClaimRepository {

    PropertyClaim save(PropertyClaim claim);

    Optional<PropertyClaim> findById(UUID claimId);

    List<PropertyClaim> findPendingByPropertyId(UUID propertyId);

    List<PropertyClaim> findExpiredPendingBefore(LocalDateTime cutoff);
}
