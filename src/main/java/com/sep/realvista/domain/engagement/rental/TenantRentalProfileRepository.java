package com.sep.realvista.domain.engagement.rental;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRentalProfileRepository extends JpaRepository<TenantRentalProfile, UUID> {
    List<TenantRentalProfile> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId);
    Optional<TenantRentalProfile> findByProfileIdAndUserIdAndDeletedFalse(UUID profileId, UUID userId);
}
