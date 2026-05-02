package com.sep.realvista.domain.policy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyRepository {
    Policy save(Policy policy);
    Optional<Policy> findById(UUID id);
    Optional<Policy> findBySlug(String slug);
    Optional<Policy> findActiveBySlug(String slug);
    List<Policy> findAll();
    List<Policy> findAllActive();
    boolean existsById(UUID id);
    void deleteById(UUID id);
}
