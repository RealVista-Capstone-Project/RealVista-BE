package com.sep.realvista.domain.property.repository;

import com.sep.realvista.domain.property.Property;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository {

    Property save(Property property);

    Optional<Property> findById(UUID id);

    List<Property> findByOwnerId(UUID ownerId);

    org.springframework.data.domain.Page<Property> findByOwnerIdAndCriteria(
            UUID ownerId, 
            String keyword, 
            org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<Property> findByAgentIdAndCriteria(
            UUID agentId,
            String keyword,
            org.springframework.data.domain.Pageable pageable);

    boolean existsById(UUID id);

    void deleteById(UUID id);

    void deleteAll();
}
