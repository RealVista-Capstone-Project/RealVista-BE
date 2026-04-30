package com.sep.realvista.domain.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyAgentRepository extends JpaRepository<PropertyAgent, UUID> {
    Optional<PropertyAgent> findByPropertyId(UUID propertyId);
    boolean existsByPropertyIdAndAgentId(UUID propertyId, UUID agentId);

    @Query("SELECT pa FROM PropertyAgent pa WHERE pa.propertyId = :propertyId AND pa.deleted = false")
    List<PropertyAgent> findActiveByPropertyId(@Param("propertyId") UUID propertyId);
}
