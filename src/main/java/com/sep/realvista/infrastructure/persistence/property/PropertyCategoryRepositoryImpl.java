package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.PropertyCategory;
import com.sep.realvista.domain.property.repository.PropertyCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PropertyCategoryRepositoryImpl implements PropertyCategoryRepository {

    private final PropertyCategoryJpaRepository jpaRepository;

    @Override
    public PropertyCategory save(PropertyCategory propertyCategory) {
        return jpaRepository.save(propertyCategory);
    }

    @Override
    public Optional<PropertyCategory> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<PropertyCategory> findByCode(String code) {
        return jpaRepository.findByCode(code);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }
}
