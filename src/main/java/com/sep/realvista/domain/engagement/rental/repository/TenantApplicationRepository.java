package com.sep.realvista.domain.engagement.rental.repository;

import com.sep.realvista.domain.engagement.rental.TenantApplication;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantApplicationRepository {
    TenantApplication save(TenantApplication tenantApplication);
    Optional<TenantApplication> findById(UUID id);
    List<TenantApplication> findByUserId(UUID userId);
    void delete(TenantApplication tenantApplication);
}
