package com.sep.realvista.domain.property.attribute.repository;

import com.sep.realvista.domain.property.attribute.PropertyAttributeRange;

import java.util.List;
import java.util.UUID;

public interface PropertyAttributeRangeRepository {
    List<PropertyAttributeRange> findByPropertyAttributeId(UUID propertyAttributeId);
    List<PropertyAttributeRange> findAll();
}
