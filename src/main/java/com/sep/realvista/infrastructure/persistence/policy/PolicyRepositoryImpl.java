package com.sep.realvista.infrastructure.persistence.policy;

import com.sep.realvista.domain.policy.Policy;
import com.sep.realvista.domain.policy.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PolicyRepositoryImpl implements PolicyRepository {

    private final PolicyJpaRepository jpaRepository;

    @Override
    public Policy save(Policy policy) {
        return jpaRepository.save(policy);
    }

    @Override
    public Optional<Policy> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Policy> findBySlug(String slug) {
        return jpaRepository.findBySlug(slug);
    }

    @Override
    public List<Policy> findAll() {
        return jpaRepository.findAll();
    }
}
