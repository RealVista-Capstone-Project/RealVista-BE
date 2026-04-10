package com.sep.realvista.infrastructure.persistence.profile;

import com.sep.realvista.domain.profile.CustomerProfile;
import com.sep.realvista.domain.profile.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CustomerProfileRepositoryImpl implements CustomerProfileRepository {

    private final CustomerProfileJpaRepository jpaRepository;

    @Override
    public CustomerProfile save(CustomerProfile customerProfile) {
        return jpaRepository.save(customerProfile);
    }

    @Override
    public Optional<CustomerProfile> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<CustomerProfile> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<CustomerProfile> findByUserIdAndIsActiveTrue(UUID userId) {
        return jpaRepository.findByUserIdAndIsActiveTrue(userId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpaRepository.existsByUserId(userId);
    }
}
