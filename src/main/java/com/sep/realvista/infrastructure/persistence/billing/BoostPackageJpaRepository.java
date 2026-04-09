package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.boost.BoostPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoostPackageJpaRepository extends JpaRepository<BoostPackage, UUID> {
    List<BoostPackage> findByIsActiveTrue();
    Optional<BoostPackage> findByCode(String code);
}
