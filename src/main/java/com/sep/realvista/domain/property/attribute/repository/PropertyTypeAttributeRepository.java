package com.sep.realvista.domain.property.attribute.repository;

import com.sep.realvista.domain.property.attribute.PropertyTypeAttribute;

import java.util.List;
import java.util.UUID;

public interface PropertyTypeAttributeRepository {
    List<PropertyTypeAttribute> findByPropertyTypeId(UUID propertyTypeId);
}
