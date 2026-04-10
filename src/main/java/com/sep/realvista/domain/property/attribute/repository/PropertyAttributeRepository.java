package com.sep.realvista.domain.property.attribute.repository;

import com.sep.realvista.domain.property.attribute.PropertyAttribute;

import java.util.List;
import java.util.Optional;

public interface PropertyAttributeRepository {
    Optional<PropertyAttribute> findByCode(String code);
    List<PropertyAttribute> findAllSearchable();
}
