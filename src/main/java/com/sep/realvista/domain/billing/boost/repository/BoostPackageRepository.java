package com.sep.realvista.domain.billing.boost.repository;

import com.sep.realvista.domain.billing.boost.BoostPackage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoostPackageRepository {
    BoostPackage save(BoostPackage boostPackage);
    List<BoostPackage> findAllActive();
    List<BoostPackage> findAllIncludingInactive();
    Optional<BoostPackage> findByCode(String code);
    Optional<BoostPackage> findById(UUID id);
}
