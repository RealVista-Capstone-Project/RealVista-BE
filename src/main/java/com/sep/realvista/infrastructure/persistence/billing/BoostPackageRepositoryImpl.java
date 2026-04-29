package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.boost.BoostPackage;
import com.sep.realvista.domain.billing.boost.repository.BoostPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BoostPackageRepositoryImpl implements BoostPackageRepository {

    private final BoostPackageJpaRepository jpa;

    @Override
    public BoostPackage save(BoostPackage boostPackage) {
        return jpa.save(boostPackage);
    }

    @Override
    public List<BoostPackage> findAllActive() {
        return jpa.findByIsActiveTrue();
    }

    @Override
    public List<BoostPackage> findAllIncludingInactive() {
        return jpa.findByDeletedFalse();
    }

    @Override
    public Optional<BoostPackage> findByCode(String code) {
        return jpa.findByCode(code);
    }

    @Override
    public Optional<BoostPackage> findById(UUID id) {
        return jpa.findByBoostPackageIdAndDeletedFalse(id);
    }
}
