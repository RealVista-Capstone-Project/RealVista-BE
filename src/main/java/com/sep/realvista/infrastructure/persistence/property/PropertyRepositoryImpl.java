package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PropertyRepositoryImpl implements PropertyRepository {

    private final PropertyJpaRepository jpaRepository;

    @Override
    public Property save(Property property) {
        return jpaRepository.save(property);
    }

    @Override
    public Optional<Property> findById(UUID id) {
        return jpaRepository.findActiveById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
