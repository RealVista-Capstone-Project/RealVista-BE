package com.sep.realvista.infrastructure.persistence.listing;

import com.sep.realvista.domain.property.attribute.PropertyTypeAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyTypeAttributeRepository extends JpaRepository<PropertyTypeAttribute, UUID> {
    List<PropertyTypeAttribute> findByPropertyTypeIdAndIsRequiredTrue(UUID propertyTypeId);
    List<PropertyTypeAttribute> findByPropertyTypeId(UUID propertyTypeId);
}
