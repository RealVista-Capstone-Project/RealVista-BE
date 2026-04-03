package com.sep.realvista.infrastructure.persistence.property.attribute;

import com.sep.realvista.domain.property.attribute.PropertyAttributeRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyAttributeRangeJpaRepository extends JpaRepository<PropertyAttributeRange, UUID> {
    List<PropertyAttributeRange> findByPropertyAttributeIdOrderByDisplayOrderAsc(UUID propertyAttributeId);
}
