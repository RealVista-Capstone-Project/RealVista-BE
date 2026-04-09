package com.sep.realvista.domain.profile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerProfileRepository {

    CustomerProfile save(CustomerProfile customerProfile);

    Optional<CustomerProfile> findById(UUID id);

    List<CustomerProfile> findByUserId(UUID userId);

    Optional<CustomerProfile> findByUserIdAndIsActiveTrue(UUID userId);

    void deleteById(UUID id);

    boolean existsByUserId(UUID userId);
}
