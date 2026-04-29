package com.sep.realvista.domain.billing.snapshot.repository;

import com.sep.realvista.domain.billing.snapshot.BoostPackageSnapshot;

import java.util.List;
import java.util.UUID;

public interface BoostPackageSnapshotRepository {
    BoostPackageSnapshot save(BoostPackageSnapshot snapshot);
    List<BoostPackageSnapshot> findByBoostPackageId(UUID boostPackageId);
}
