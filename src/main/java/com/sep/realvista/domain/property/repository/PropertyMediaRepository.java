package com.sep.realvista.domain.property.repository;

import com.sep.realvista.domain.property.PropertyMedia;

import java.util.List;
import java.util.UUID;

public interface PropertyMediaRepository {
    List<PropertyMedia> findByPropertyId(UUID propertyId);
    void deleteByPropertyId(UUID propertyId);
    <S extends PropertyMedia> List<S> saveAll(Iterable<S> entities);
}
