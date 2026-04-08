package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyStatus;
import org.springframework.data.domain.Pageable;
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
           + "AND (:status IS NULL OR p.status = :status) AND "
           + "(:keyword IS NULL OR LOWER(p.streetAddress) LIKE :keyword OR "
           + "LOWER(p.descriptions) LIKE :keyword)",
           countQuery = "SELECT COUNT(p) FROM Property p "
           + "WHERE p.ownerId = :ownerId AND p.deleted = false "
           + "AND (:status IS NULL OR p.status = :status) AND "
           + "(:keyword IS NULL OR LOWER(p.streetAddress) LIKE :keyword OR "
           + "LOWER(p.descriptions) LIKE :keyword)")
    org.springframework.data.domain.Page<Property> findByOwnerIdAndKeyword(
            @Param("ownerId") UUID ownerId,
            @Param("keyword") String keyword,
            @Param("status") PropertyStatus status,
            Pageable pageable);
 
    @Query(value = "SELECT DISTINCT p FROM Property p "
           + "JOIN Engagement e ON e.propertyId = p.propertyId "
           + "LEFT JOIN FETCH p.propertyType pt "
           + "LEFT JOIN FETCH pt.propertyCategory "
           + "LEFT JOIN FETCH p.location loc "
           + "LEFT JOIN FETCH loc.parent dist "
           + "LEFT JOIN FETCH dist.parent city "
           + "WHERE e.status = 'ACCEPTED' AND (e.initiatorId = :agentId OR e.receiverId = :agentId) "
           + "AND p.deleted = false AND (:status IS NULL OR p.status = :status) AND "
           + "(:keyword IS NULL OR LOWER(p.streetAddress) LIKE :keyword OR "
           + "LOWER(p.descriptions) LIKE :keyword)",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Property p "
           + "JOIN Engagement e ON e.propertyId = p.propertyId "
           + "WHERE e.status = 'ACCEPTED' AND (e.initiatorId = :agentId OR e.receiverId = :agentId) "
           + "AND p.deleted = false AND (:status IS NULL OR p.status = :status) AND "
           + "(:keyword IS NULL OR LOWER(p.streetAddress) LIKE :keyword OR "
           + "LOWER(p.descriptions) LIKE :keyword)")
    org.springframework.data.domain.Page<Property> findByAgentIdAndKeyword(
            @Param("agentId") UUID agentId,
            @Param("keyword") String keyword,
            @Param("status") PropertyStatus status,
            Pageable pageable);

    @Query("SELECT p FROM Property p WHERE (p.ownerId = :userId OR "
           + "EXISTS (SELECT pa FROM com.sep.realvista.domain.agent.PropertyAgent pa "
           + "WHERE pa.propertyId = p.propertyId AND pa.agentId = :userId AND pa.deleted = false)) "
           + "AND p.deleted = false")
    List<Property> findByOwnerIdOrAgentId(@Param("userId") UUID userId);

    @Query("SELECT p FROM Property p WHERE p.latitude BETWEEN :southLat AND :northLat "
           + "AND p.longitude BETWEEN :westLng AND :eastLng AND p.deleted = false")
    List<Property> findByLocationRange(@Param("northLat") java.math.BigDecimal northLat,
                                        @Param("southLat") java.math.BigDecimal southLat,
                                        @Param("eastLng") java.math.BigDecimal eastLng,
                                        @Param("westLng") java.math.BigDecimal westLng);

    @Query("SELECT p FROM Property p "
            + "WHERE LOWER(p.streetAddress) LIKE LOWER(CONCAT('%', :address, '%')) AND p.deleted = false")
    List<Property> searchByAddress(@Param("address") String address);

    /**
     * Finds AVAILABLE properties not already assigned to the given agent,
     * with optional keyword, propertyType, and location filters.
     *
     * <p>Eagerly fetches propertyType (with category) and location (ward → district → city)
     * to avoid N+1 queries when building the feed response.
     *
     * <p>Excludes properties where the agent already has an active PropertyAgent link.
     */
    @Query(value = "SELECT DISTINCT p FROM Property p "
            + "LEFT JOIN FETCH p.propertyType pt "
            + "LEFT JOIN FETCH pt.propertyCategory "
            + "LEFT JOIN FETCH p.location loc "
            + "LEFT JOIN FETCH loc.parent dist "
            + "LEFT JOIN FETCH dist.parent city "
            + "WHERE p.status = 'AVAILABLE' AND p.deleted = false "
            + "AND NOT EXISTS (SELECT pa FROM com.sep.realvista.domain.agent.PropertyAgent pa "
            + "  WHERE pa.propertyId = p.propertyId AND pa.agentId = :agentId AND pa.deleted = false) "
            + "AND (:propertyTypeId IS NULL OR p.propertyTypeId = :propertyTypeId) "
            + "AND (:locationId IS NULL OR p.locationId = :locationId) "
            + "AND (:keyword IS NULL OR LOWER(p.streetAddress) LIKE :keyword "
            + "  OR LOWER(p.descriptions) LIKE :keyword)",
            countQuery = "SELECT COUNT(DISTINCT p) FROM Property p "
            + "WHERE p.status = 'AVAILABLE' AND p.deleted = false "
            + "AND NOT EXISTS (SELECT pa FROM com.sep.realvista.domain.agent.PropertyAgent pa "
            + "  WHERE pa.propertyId = p.propertyId AND pa.agentId = :agentId AND pa.deleted = false) "
            + "AND (:propertyTypeId IS NULL OR p.propertyTypeId = :propertyTypeId) "
            + "AND (:locationId IS NULL OR p.locationId = :locationId) "
            + "AND (:keyword IS NULL OR LOWER(p.streetAddress) LIKE :keyword "
            + "  OR LOWER(p.descriptions) LIKE :keyword)")
    org.springframework.data.domain.Page<Property> findPropertyFeed(
            @Param("agentId") UUID agentId,
            @Param("keyword") String keyword,
            @Param("propertyTypeId") UUID propertyTypeId,
            @Param("locationId") UUID locationId,
            Pageable pageable);
}
