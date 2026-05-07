package com.sep.realvista.domain.property.repository;

import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyStatus;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
            UUID propertyTypeId,
            Pageable pageable);

    org.springframework.data.domain.Page<Property> findByAgentIdAndCriteria(
            UUID agentId,
            String keyword,
            PropertyStatus status,
            List<PropertyStatus> statuses,
            UUID propertyTypeId,
            Pageable pageable);

    boolean existsById(UUID id);

    void deleteById(UUID id);

    void deleteAll();

    List<Property> findInLocationRange(java.math.BigDecimal northLat, java.math.BigDecimal southLat,
                                       java.math.BigDecimal eastLng, java.math.BigDecimal westLng);

    List<Property> searchByAddress(String address);

    long countByLocationIds(List<UUID> locationIds);

    /**
     * Finds properties that may be duplicates of the given address.
     * Uses two detection strategies:
     * 1. Exact normalized text match: LOWER(TRIM(street_address)) + same location_id
     * 2. Coordinate proximity: within ~30 metres (0.0003 degree bounding box)
     *
     * @param locationId       ward-level location ID
     * @param normalizedAddress LOWER(TRIM(streetAddress)) for comparison
     * @param latitude         center latitude
     * @param longitude        center longitude
     * @param excludeId        property to exclude (e.g. when editing)
     * @return list of candidate duplicate properties
     */
    List<Property> findPotentialDuplicates(UUID locationId, String normalizedAddress,
                                           BigDecimal latitude, BigDecimal longitude,
                                           UUID excludeId);

    /**
     * Finds ACTIVE/AVAILABLE/RESERVED/RENTED properties that have had no active listing
     * for longer than the given cutoff and are not yet STALE.
     */
    List<Property> findPropertiesEligibleForStale(LocalDateTime lastActiveListingCutoff);

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
