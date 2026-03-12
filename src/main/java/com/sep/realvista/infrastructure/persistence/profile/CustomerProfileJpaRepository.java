package com.sep.realvista.infrastructure.persistence.profile;

import com.sep.realvista.domain.profile.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerProfileJpaRepository extends JpaRepository<CustomerProfile, UUID> {

    List<CustomerProfile> findByUserId(UUID userId);

    Optional<CustomerProfile> findByUserIdAndIsActiveTrue(UUID userId);

    boolean existsByUserId(UUID userId);
}
