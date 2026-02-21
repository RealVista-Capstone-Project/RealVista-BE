package com.sep.realvista.infrastructure.persistence.engagement.rental;

import com.sep.realvista.domain.engagement.rental.TenantApplication;
import com.sep.realvista.domain.engagement.rental.TenantApplicationStatus;
import com.sep.realvista.domain.engagement.rental.repository.TenantApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TenantApplicationRepositoryImpl implements TenantApplicationRepository {

    private final TenantApplicationJpaRepository jpaRepository;

    @Override
    public TenantApplication save(TenantApplication tenantApplication) {
        return jpaRepository.save(tenantApplication);
    }

    @Override
    public Optional<TenantApplication> findById(UUID id) {
        return jpaRepository.findByTenantApplicationIdAndStatusNot(id, TenantApplicationStatus.ARCHIVED);
    }

    @Override
    public List<TenantApplication> findByUserId(UUID userId) {
        return jpaRepository.findByUserIdAndStatusNot(userId, TenantApplicationStatus.ARCHIVED);
    }

    @Override
    public void delete(TenantApplication tenantApplication) {
        tenantApplication.archive();
        jpaRepository.save(tenantApplication);
    }
}
