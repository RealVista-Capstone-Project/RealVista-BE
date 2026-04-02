package com.sep.realvista.domain.property.repository;

import com.sep.realvista.domain.property.PropertyMedia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyMediaRepository {
    PropertyMedia save(PropertyMedia media);
    Optional<PropertyMedia> findById(UUID id);
    List<PropertyMedia> findByPropertyId(UUID propertyId);
    void saveAll(List<PropertyMedia> medias);
}
