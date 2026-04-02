package com.sep.realvista.domain.profile.repository;

import com.sep.realvista.domain.profile.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, UUID> {
    Optional<CustomerProfile> findByUserIdAndIsActiveTrueAndDeletedFalse(UUID userId);
}
