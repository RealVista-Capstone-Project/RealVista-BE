package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.snapshot.BoostPackageSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BoostPackageSnapshotJpaRepository extends JpaRepository<BoostPackageSnapshot, UUID> {
    List<BoostPackageSnapshot> findByBoostPackageIdOrderByCreatedAtDesc(UUID boostPackageId);
}
