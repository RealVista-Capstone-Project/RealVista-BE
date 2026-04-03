package com.sep.realvista.infrastructure.persistence.property.attribute;

import com.sep.realvista.domain.property.attribute.PropertyAttribute;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyAttributeJpaRepository 
        extends JpaRepository<PropertyAttribute, UUID>, PropertyAttributeRepository {
    
    @Override
    Optional<PropertyAttribute> findByCode(String code);

    @Override
    default List<PropertyAttribute> findAllSearchable() {
        return findByIsSearchableTrue();
    }

    List<PropertyAttribute> findByIsSearchableTrue();
}
