package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.snapshot.FeaturePackageSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeaturePackageSnapshotJpaRepository extends JpaRepository<FeaturePackageSnapshot, UUID> {
    List<FeaturePackageSnapshot> findByFeaturePackageIdOrderByCreatedAtDesc(UUID featurePackageId);
}
