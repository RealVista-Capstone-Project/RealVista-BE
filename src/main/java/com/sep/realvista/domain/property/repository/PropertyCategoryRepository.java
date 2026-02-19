package com.sep.realvista.domain.property.repository;

import com.sep.realvista.domain.property.PropertyCategory;
import java.util.Optional;
import java.util.UUID;

public interface PropertyCategoryRepository {
    PropertyCategory save(PropertyCategory propertyCategory);
    Optional<PropertyCategory> findById(UUID id);
    Optional<PropertyCategory> findByCode(String code);
    void deleteAll();
}
