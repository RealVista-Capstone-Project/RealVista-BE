package com.sep.realvista.domain.property.repository;

import com.sep.realvista.domain.property.amenity.PropertyAmenity;

import java.util.List;
import java.util.UUID;

/**
 * Domain repository interface for PropertyAmenity entity.
 * Follows DDD layering - application layer depends on this interface,
 * infrastructure layer provides the implementation.
 */
public interface PropertyAmenityRepository {

    /**
     * Find all amenities for a property with fetched amenity details.
     *
     * @param propertyId the property ID
     * @return list of property amenities with amenity details
     */
    List<PropertyAmenity> findByPropertyIdWithAmenity(UUID propertyId);

    /**
     * Find all amenities for multiple properties in a single batch query.
     *
     * @param propertyIds list of property IDs
     * @return list of property amenities with amenity details
     */
    List<PropertyAmenity> findByPropertyIdsWithAmenity(List<UUID> propertyIds);
}
