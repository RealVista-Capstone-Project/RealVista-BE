package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.PropertyType;
import com.sep.realvista.domain.property.repository.PropertyTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PropertyTypeRepositoryImpl implements PropertyTypeRepository {

    private final PropertyTypeJpaRepository jpaRepository;

    @Override
    public PropertyType save(PropertyType propertyType) {
        return jpaRepository.save(propertyType);
    }

    @Override
    public Optional<PropertyType> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<PropertyType> findByCode(String code) {
        return jpaRepository.findByCode(code);
    }

    @Override
    public List<PropertyType> findAllByIdIn(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return jpaRepository.findAllById(ids);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }
}
