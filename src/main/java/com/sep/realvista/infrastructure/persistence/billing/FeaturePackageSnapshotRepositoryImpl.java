package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.snapshot.FeaturePackageSnapshot;
import com.sep.realvista.domain.billing.snapshot.repository.FeaturePackageSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FeaturePackageSnapshotRepositoryImpl implements FeaturePackageSnapshotRepository {

    private final FeaturePackageSnapshotJpaRepository jpa;

    @Override
    public FeaturePackageSnapshot save(FeaturePackageSnapshot snapshot) {
        return jpa.save(snapshot);
    }

    @Override
    public List<FeaturePackageSnapshot> findByFeaturePackageId(UUID featurePackageId) {
        return jpa.findByFeaturePackageIdOrderByCreatedAtDesc(featurePackageId);
    }
}
