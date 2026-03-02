package com.sep.realvista.infrastructure.persistence.engagement.rental;

import com.sep.realvista.domain.engagement.rental.TenantApplication;
import com.sep.realvista.domain.engagement.rental.TenantApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantApplicationJpaRepository extends JpaRepository<TenantApplication, UUID> {
    List<TenantApplication> findByUserIdAndStatusNot(UUID userId, TenantApplicationStatus status);
    Optional<TenantApplication> findByTenantApplicationIdAndStatusNot(UUID id, TenantApplicationStatus status);
}
