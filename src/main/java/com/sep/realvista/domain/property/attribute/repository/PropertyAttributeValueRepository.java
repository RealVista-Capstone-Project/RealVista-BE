package com.sep.realvista.domain.property.attribute.repository;

import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for PropertyAttributeValue entity.
 * Follows DDD layering - application layer depends on this interface,
 * infrastructure layer provides the implementation.
 */
public interface PropertyAttributeValueRepository {

    /**
     * Find all attribute values for a property with fetched attribute details.
     * Results are ordered by PropertyType Attribute priority, then by attribute code.
     *
     * @param propertyId the property ID
     * @return list of property attribute values with attribute details
     */
    List<PropertyAttributeValue> findByPropertyIdWithAttribute(UUID propertyId);

    /**
     * Find a single attribute value by ID with fetched attribute details.
     *
     * @param id the property attribute value ID
     * @return optional property attribute value with attribute details
     */
    Optional<PropertyAttributeValue> findByIdWithAttribute(UUID id);

    /**
     * Find required attributes for multiple properties in a single batch query.
     * Only returns attributes marked as is_required = true in property_type_attributes.
     * Used for similar listings cards and search results.
     *
     * @param propertyIds list of property IDs
     * @return list of required property attribute values with attribute details
     */
    List<PropertyAttributeValue> findRequiredAttributesByPropertyIds(List<UUID> propertyIds);

    /**
     * Find all attributes for multiple properties in a single batch query.
     * Returns all non-deleted attribute values ordered by property_type_attributes.priority then name.
     * Used for search listing cards to display dynamic attributes.
     *
     * @param propertyIds list of property IDs
     * @return list of all property attribute values with attribute details
     */
    List<PropertyAttributeValue> findAllAttributesByPropertyIds(List<UUID> propertyIds);
}
