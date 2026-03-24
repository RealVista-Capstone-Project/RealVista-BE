package com.sep.realvista.infrastructure.persistence.property.attribute;

import com.sep.realvista.domain.property.attribute.PropertyAttribute;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PropertyAttributeJpaRepository 
        extends JpaRepository<PropertyAttribute, UUID>, PropertyAttributeRepository {
    
    Optional<PropertyAttribute> findByCode(String code);
}
