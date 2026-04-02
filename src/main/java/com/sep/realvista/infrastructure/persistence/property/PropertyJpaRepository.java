package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Property entity.
 */
public interface PropertyJpaRepository extends JpaRepository<Property, UUID> {

    @Query("SELECT p FROM Property p WHERE p.propertyId = :id AND p.deleted = false")
    Optional<Property> findActiveById(@Param("id") UUID id);

    List<Property> findByOwnerId(UUID ownerId);

    @Query(value = "SELECT DISTINCT p FROM Property p "
           + "LEFT JOIN FETCH p.propertyType pt "
           + "LEFT JOIN FETCH pt.propertyCategory "
           + "LEFT JOIN FETCH p.location loc "
           + "LEFT JOIN FETCH loc.parent dist "
           + "LEFT JOIN FETCH dist.parent city "
           + "WHERE p.ownerId = :ownerId AND p.deleted = false "
           + "AND p.status = 'AVAILABLE' AND "
           + "(:keyword IS NULL OR LOWER(p.streetAddress) LIKE :keyword OR "
           + "LOWER(p.descriptions) LIKE :keyword)",
           countQuery = "SELECT COUNT(p) FROM Property p "
           + "WHERE p.ownerId = :ownerId AND p.deleted = false "
           + "AND p.status = 'AVAILABLE' AND "
           + "(:keyword IS NULL OR LOWER(p.streetAddress) LIKE :keyword OR "
           + "LOWER(p.descriptions) LIKE :keyword)")
    org.springframework.data.domain.Page<Property> findByOwnerIdAndKeyword(
            @Param("ownerId") UUID ownerId,
            @Param("keyword") String keyword,
            org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Property p "
           + "JOIN Engagement e ON e.propertyId = p.propertyId "
           + "LEFT JOIN FETCH p.propertyType pt "
           + "LEFT JOIN FETCH pt.propertyCategory "
           + "LEFT JOIN FETCH p.location loc "
           + "LEFT JOIN FETCH loc.parent dist "
           + "LEFT JOIN FETCH dist.parent city "
           + "WHERE e.status = 'ACCEPTED' AND (e.initiatorId = :agentId OR e.receiverId = :agentId) "
           + "AND p.deleted = false AND p.status = 'AVAILABLE' AND "
           + "(:keyword IS NULL OR LOWER(p.streetAddress) LIKE :keyword OR "
           + "LOWER(p.descriptions) LIKE :keyword)",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Property p "
           + "JOIN Engagement e ON e.propertyId = p.propertyId "
           + "WHERE e.status = 'ACCEPTED' AND (e.initiatorId = :agentId OR e.receiverId = :agentId) "
           + "AND p.deleted = false AND p.status = 'AVAILABLE' AND "
           + "(:keyword IS NULL OR LOWER(p.streetAddress) LIKE :keyword OR "
           + "LOWER(p.descriptions) LIKE :keyword)")
    org.springframework.data.domain.Page<Property> findByAgentIdAndKeyword(
            @Param("agentId") UUID agentId,
            @Param("keyword") String keyword,
            org.springframework.data.domain.Pageable pageable);
}
