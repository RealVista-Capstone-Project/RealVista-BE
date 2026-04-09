package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.boost.BoostPackage;
import com.sep.realvista.domain.billing.boost.repository.BoostPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BoostPackageRepositoryImpl implements BoostPackageRepository {

    private final BoostPackageJpaRepository jpa;

    @Override
    public List<BoostPackage> findAllActive() {
        return jpa.findByIsActiveTrue();
    }

    @Override
    public Optional<BoostPackage> findByCode(String code) {
        return jpa.findByCode(code);
    }
}
