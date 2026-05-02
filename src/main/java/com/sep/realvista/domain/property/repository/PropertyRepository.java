package com.sep.realvista.domain.property.repository;

import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository {

    Property save(Property property);
    List<Property> saveAll(List<Property> properties);

    Optional<Property> findById(UUID id);

    List<Property> findByOwnerId(UUID ownerId);

    List<Property> findByOwnerIdOrAgentId(UUID userId);

    org.springframework.data.domain.Page<Property> findByOwnerIdAndCriteria(
            UUID ownerId,
            String keyword,
            PropertyStatus status,
            Pageable pageable);

    org.springframework.data.domain.Page<Property> findByAgentIdAndCriteria(
            UUID agentId,
            String keyword,
            PropertyStatus status,
            Pageable pageable);

    boolean existsById(UUID id);

    void deleteById(UUID id);

    void deleteAll();

    List<Property> findInLocationRange(java.math.BigDecimal northLat, java.math.BigDecimal southLat,
                                       java.math.BigDecimal eastLng, java.math.BigDecimal westLng);

    List<Property> searchByAddress(String address);

    long countByLocationIds(List<UUID> locationIds);

    /**
     * Admin-only: finds all non-deleted properties with optional keyword, status, user,
     * property type, and location filters.
     *
     * @param keyword  optional keyword to search street address or descriptions
     * @param status   optional status filter
     * @param userId         optional user ID — when provided, matches properties where the user is owner
     *                       or active agent
     * @param propertyTypeId optional property type ID
     * @param locationId     optional city, district, ward, or exact location ID
     * @param pageable pagination parameters
     * @return page of properties
     */
    org.springframework.data.domain.Page<Property> findByAdminCriteria(
            String keyword,
            PropertyStatus status,
            UUID userId,
            UUID propertyTypeId,
            UUID locationId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Finds a paginated feed of properties available for agent proposals.
     *
     * <p>Returns AVAILABLE properties that are not already assigned to the given agent,
     * with optional filtering by keyword, property type, and location.
     *
     * @param agentId        the agent's user ID (to exclude already-assigned properties)
     * @param keyword        optional keyword to search street address or descriptions
     * @param propertyTypeId optional property type filter
     * @param locationId     optional location (ward/district/city) filter
     * @param pageable       pagination parameters
     * @return page of properties available for proposal
     */
    org.springframework.data.domain.Page<Property> findPropertyFeed(
            UUID agentId,
            String keyword,
            UUID propertyTypeId,
            UUID locationId,
            org.springframework.data.domain.Pageable pageable);
}
