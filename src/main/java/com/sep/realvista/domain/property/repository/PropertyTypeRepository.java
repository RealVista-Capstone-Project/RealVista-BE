package com.sep.realvista.domain.property.repository;

import com.sep.realvista.domain.property.PropertyType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyTypeRepository {
    PropertyType save(PropertyType propertyType);
    Optional<PropertyType> findById(UUID id);
    Optional<PropertyType> findByCode(String code);
    List<PropertyType> findAllActive();
    void deleteAll();
}
