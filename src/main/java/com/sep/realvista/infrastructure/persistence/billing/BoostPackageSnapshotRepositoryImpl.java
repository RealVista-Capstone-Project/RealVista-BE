package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.snapshot.BoostPackageSnapshot;
import com.sep.realvista.domain.billing.snapshot.repository.BoostPackageSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BoostPackageSnapshotRepositoryImpl implements BoostPackageSnapshotRepository {

    private final BoostPackageSnapshotJpaRepository jpa;

    @Override
    public BoostPackageSnapshot save(BoostPackageSnapshot snapshot) {
        return jpa.save(snapshot);
    }

    @Override
    public List<BoostPackageSnapshot> findByBoostPackageId(UUID boostPackageId) {
        return jpa.findByBoostPackageIdOrderByCreatedAtDesc(boostPackageId);
    }
}
