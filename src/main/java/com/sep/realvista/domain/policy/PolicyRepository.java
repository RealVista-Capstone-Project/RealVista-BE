package com.sep.realvista.domain.policy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyRepository {
    Policy save(Policy policy);
    Optional<Policy> findById(UUID id);
    Optional<Policy> findBySlug(String slug);
    List<Policy> findAll();
}
