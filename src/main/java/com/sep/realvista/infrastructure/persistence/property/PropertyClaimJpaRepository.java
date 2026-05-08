package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.claim.PropertyClaim;
import com.sep.realvista.domain.property.claim.PropertyClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PropertyClaimJpaRepository extends JpaRepository<PropertyClaim, UUID> {

    @Query("SELECT c FROM PropertyClaim c WHERE c.propertyId = :propertyId "
            + "AND c.status = 'PENDING' AND c.deleted = false")
    List<PropertyClaim> findPendingByPropertyId(@Param("propertyId") UUID propertyId);

    @Query("SELECT c FROM PropertyClaim c WHERE c.status = :status "
            + "AND c.expiresAt < :cutoff AND c.deleted = false")
    List<PropertyClaim> findByStatusAndExpiresAtBefore(
            @Param("status") PropertyClaimStatus status,
            @Param("cutoff") LocalDateTime cutoff);
}
