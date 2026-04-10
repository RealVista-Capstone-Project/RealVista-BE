package com.sep.realvista.domain.property.repository;

import com.sep.realvista.domain.property.PropertyType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyTypeRepository {
    PropertyType save(PropertyType propertyType);

    Optional<PropertyType> findById(UUID id);

    Optional<PropertyType> findByCode(String code);

    /**
     * Batch load by id (empty collection returns empty list).
     */
    List<PropertyType> findAllByIdIn(Collection<UUID> ids);

    void deleteAll();
}
