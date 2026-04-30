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
           + "LEFT JOIN Engagement e ON e.propertyId = p.propertyId AND e.status = 'ACCEPTED' "
           + "LEFT JOIN com.sep.realvista.domain.agent.PropertyAgent pa ON pa.propertyId = p.propertyId "
           + "LEFT JOIN FETCH p.propertyType pt "
           + "LEFT JOIN FETCH pt.propertyCategory "
           + "LEFT JOIN FETCH p.location loc "
           + "LEFT JOIN FETCH loc.parent dist "
           + "LEFT JOIN FETCH dist.parent city "
           + "WHERE ( (e.initiatorId = :agentId OR e.receiverId = :agentId) "
           + "OR (pa.agentId = :agentId AND pa.deleted = false) ) "
           + "AND p.deleted = false AND (:status IS NULL OR p.status = :status) AND "
           + "(:keyword IS NULL OR LOWER(p.streetAddress) LIKE :keyword OR "
           + "LOWER(p.descriptions) LIKE :keyword)",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Property p "
           + "LEFT JOIN Engagement e ON e.propertyId = p.propertyId AND e.status = 'ACCEPTED' "
           + "LEFT JOIN com.sep.realvista.domain.agent.PropertyAgent pa ON pa.propertyId = p.propertyId "
           + "WHERE ( (e.initiatorId = :agentId OR e.receiverId = :agentId) "
           + "OR (pa.agentId = :agentId AND pa.deleted = false) ) "
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

    @Query("SELECT COUNT(p) FROM Property p WHERE p.locationId IN :locationIds AND p.deleted = false")
    long countByLocationIds(@Param("locationIds") List<UUID> locationIds);

    /**
     * Admin-only query: all non-deleted properties with optional keyword, status, user,
     * property type, and location filters.
     * When userId is provided, matches properties where the user is either the owner or an active agent.
     */
    @Query(value = "SELECT DISTINCT p FROM Property p "
           + "LEFT JOIN FETCH p.propertyType pt "
           + "LEFT JOIN FETCH pt.propertyCategory "
           + "LEFT JOIN FETCH p.location loc "
           + "LEFT JOIN FETCH loc.parent dist "
           + "LEFT JOIN FETCH dist.parent city "
           + "WHERE p.deleted = false "
           + "AND (:status IS NULL OR p.status = :status) "
           + "AND (:propertyTypeId IS NULL OR p.propertyTypeId = :propertyTypeId) "
           + "AND (:locationId IS NULL OR p.locationId = :locationId "
           + "  OR loc.locationId = :locationId OR dist.locationId = :locationId "
           + "  OR city.locationId = :locationId) "
           + "AND (:keyword IS NULL OR LOWER(p.streetAddress) LIKE :keyword "
           + "  OR LOWER(p.descriptions) LIKE :keyword "
           + "  OR EXISTS (SELECT owner FROM com.sep.realvista.domain.user.User owner "
           + "    WHERE owner.userId = p.ownerId "
           + "    AND (LOWER(owner.firstName) LIKE :keyword "
           + "      OR LOWER(owner.lastName) LIKE :keyword "
           + "      OR LOWER(owner.businessName) LIKE :keyword "
           + "      OR LOWER(owner.email.value) LIKE :keyword "
           + "      OR LOWER(owner.phone) LIKE :keyword))) "
           + "AND (:userId IS NULL OR p.ownerId = :userId "
           + "  OR EXISTS (SELECT pa FROM com.sep.realvista.domain.agent.PropertyAgent pa "
           + "    WHERE pa.propertyId = p.propertyId AND pa.agentId = :userId AND pa.deleted = false))",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Property p "
           + "LEFT JOIN p.location loc "
           + "LEFT JOIN loc.parent dist "
           + "LEFT JOIN dist.parent city "
           + "WHERE p.deleted = false "
           + "AND (:status IS NULL OR p.status = :status) "
           + "AND (:propertyTypeId IS NULL OR p.propertyTypeId = :propertyTypeId) "
           + "AND (:locationId IS NULL OR p.locationId = :locationId "
           + "  OR loc.locationId = :locationId OR dist.locationId = :locationId "
           + "  OR city.locationId = :locationId) "
           + "AND (:keyword IS NULL OR LOWER(p.streetAddress) LIKE :keyword "
           + "  OR LOWER(p.descriptions) LIKE :keyword "
           + "  OR EXISTS (SELECT owner FROM com.sep.realvista.domain.user.User owner "
           + "    WHERE owner.userId = p.ownerId "
           + "    AND (LOWER(owner.firstName) LIKE :keyword "
           + "      OR LOWER(owner.lastName) LIKE :keyword "
           + "      OR LOWER(owner.businessName) LIKE :keyword "
           + "      OR LOWER(owner.email.value) LIKE :keyword "
           + "      OR LOWER(owner.phone) LIKE :keyword))) "
           + "AND (:userId IS NULL OR p.ownerId = :userId "
           + "  OR EXISTS (SELECT pa FROM com.sep.realvista.domain.agent.PropertyAgent pa "
           + "    WHERE pa.propertyId = p.propertyId AND pa.agentId = :userId AND pa.deleted = false))")
    org.springframework.data.domain.Page<Property> findByAdminCriteria(
            @Param("keyword") String keyword,
            @Param("status") PropertyStatus status,
            @Param("userId") UUID userId,
            @Param("propertyTypeId") UUID propertyTypeId,
            @Param("locationId") UUID locationId,
            Pageable pageable);

    /**
     * Finds AVAILABLE properties not already assigned to the given agent,
     * with optional keyword, propertyType, and location filters.
     *
     * <p>Eagerly fetches propertyType (with category) and location (ward → district → city)
     * to avoid N+1 queries when building the feed response.
     *
     * <p>Excludes properties where the agent already has an active PropertyAgent link.
     * Note: Price filtering is handled in service layer due to JPQL limitations.
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
