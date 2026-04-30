package com.sep.realvista.domain.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyAgentRepository extends JpaRepository<PropertyAgent, UUID> {
    Optional<PropertyAgent> findByPropertyId(UUID propertyId);
    List<PropertyAgent> findByAgentId(UUID agentId);
    boolean existsByPropertyIdAndAgentId(UUID propertyId, UUID agentId);
}
