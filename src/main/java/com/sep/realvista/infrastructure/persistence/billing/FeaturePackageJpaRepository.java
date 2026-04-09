package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.subscription.FeaturePackage;
import com.sep.realvista.domain.billing.subscription.FeatureType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeaturePackageJpaRepository extends JpaRepository<FeaturePackage, UUID> {
    List<FeaturePackage> findByIsActiveTrueAndDeletedFalse();
    List<FeaturePackage> findByFeatureTypeAndIsActiveTrueAndDeletedFalse(FeatureType featureType);
    Optional<FeaturePackage> findByCodeAndDeletedFalse(String code);
}
