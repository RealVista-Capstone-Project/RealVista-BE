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
            List<PropertyStatus> statuses,
            Pageable pageable);

    org.springframework.data.domain.Page<Property> findByAgentIdAndCriteria(
            UUID agentId,
            String keyword,
            PropertyStatus status,
            List<PropertyStatus> statuses,
            Pageable pageable);

    boolean existsById(UUID id);

    void deleteById(UUID id);

    void deleteAll();

    List<Property> findInLocationRange(java.math.BigDecimal northLat, java.math.BigDecimal southLat,
                                       java.math.BigDecimal eastLng, java.math.BigDecimal westLng);

    List<Property> searchByAddress(String address);

    long countByLocationIds(List<UUID> locationIds);

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
