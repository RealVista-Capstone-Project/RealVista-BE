package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.PropertyMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PropertyMediaJpaRepository extends JpaRepository<PropertyMedia, UUID> {
    List<PropertyMedia> findByPropertyIdAndDeletedIsFalse(UUID propertyId);
    void deleteByPropertyId(UUID propertyId);
}
