package com.sep.realvista.domain.billing.snapshot.repository;

import com.sep.realvista.domain.billing.snapshot.FeaturePackageSnapshot;

import java.util.List;
import java.util.UUID;

public interface FeaturePackageSnapshotRepository {
    FeaturePackageSnapshot save(FeaturePackageSnapshot snapshot);
    List<FeaturePackageSnapshot> findByFeaturePackageId(UUID featurePackageId);
}
