package com.sep.realvista.infrastructure.persistence.property.attribute;

import com.sep.realvista.domain.property.attribute.PropertyAttributeRange;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeRangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PropertyAttributeRangeRepositoryImpl implements PropertyAttributeRangeRepository {

    private final PropertyAttributeRangeJpaRepository jpaRepository;

    @Override
    public List<PropertyAttributeRange> findByPropertyAttributeId(UUID propertyAttributeId) {
        return jpaRepository.findByPropertyAttributeIdOrderByDisplayOrderAsc(propertyAttributeId);
    }

    @Override
    public List<PropertyAttributeRange> findAll() {
        return jpaRepository.findAll();
    }
}
