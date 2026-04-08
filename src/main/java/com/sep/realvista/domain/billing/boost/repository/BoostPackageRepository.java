package com.sep.realvista.domain.billing.boost.repository;

import com.sep.realvista.domain.billing.boost.BoostPackage;

import java.util.List;
import java.util.Optional;

public interface BoostPackageRepository {
    List<BoostPackage> findAllActive();
    Optional<BoostPackage> findByCode(String code);
}
