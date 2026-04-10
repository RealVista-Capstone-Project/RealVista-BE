package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.subscription.FeaturePackage;
import com.sep.realvista.domain.billing.subscription.FeatureType;
import com.sep.realvista.domain.billing.subscription.repository.FeaturePackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FeaturePackageRepositoryImpl implements FeaturePackageRepository {

    private final FeaturePackageJpaRepository jpa;

    @Override
    public List<FeaturePackage> findAllActive() {
        return jpa.findByIsActiveTrueAndDeletedFalse();
    }

    @Override
    public List<FeaturePackage> findAllActiveByFeatureType(FeatureType featureType) {
        return jpa.findByFeatureTypeAndIsActiveTrueAndDeletedFalse(featureType);
    }

    @Override
    public Optional<FeaturePackage> findByCode(String code) {
        return jpa.findByCodeAndDeletedFalse(code);
    }

    @Override
    public Optional<FeaturePackage> findById(UUID id) {
        return jpa.findById(id);
    }
}
