package com.sep.realvista.domain.property.repository;

import com.sep.realvista.domain.property.PropertyMedia;

import java.util.Optional;

import java.util.List;
import java.util.UUID;

public interface PropertyMediaRepository {
    List<PropertyMedia> findByPropertyId(UUID propertyId);
    void deleteByPropertyId(UUID propertyId);
    PropertyMedia save(PropertyMedia propertyMedia);
    Optional<PropertyMedia> findById(UUID id);
    void delete(PropertyMedia propertyMedia);
    <S extends PropertyMedia> List<S> saveAll(Iterable<S> entities);
}
