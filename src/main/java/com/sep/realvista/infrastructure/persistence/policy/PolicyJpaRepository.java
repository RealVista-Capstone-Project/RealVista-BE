package com.sep.realvista.infrastructure.persistence.policy;

import com.sep.realvista.domain.policy.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyJpaRepository extends JpaRepository<Policy, UUID> {
    Optional<Policy> findBySlug(String slug);
    Optional<Policy> findBySlugAndIsActiveTrue(String slug);
    List<Policy> findAllByIsActiveTrue();
}
