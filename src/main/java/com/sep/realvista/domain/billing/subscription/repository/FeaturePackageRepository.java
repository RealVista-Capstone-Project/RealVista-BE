package com.sep.realvista.domain.billing.subscription.repository;

import com.sep.realvista.domain.billing.subscription.FeaturePackage;
import com.sep.realvista.domain.billing.subscription.FeatureType;

import java.util.List;
import java.util.Optional;

public interface FeaturePackageRepository {
    List<FeaturePackage> findAllActive();
    List<FeaturePackage> findAllActiveByFeatureType(FeatureType featureType);
    Optional<FeaturePackage> findByCode(String code);
    Optional<FeaturePackage> findById(java.util.UUID id);
}
