package com.sep.realvista.infrastructure.persistence.property.amenity;

import com.sep.realvista.domain.property.amenity.PropertyAmenity;
import com.sep.realvista.domain.property.repository.PropertyAmenityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Infrastructure implementation of PropertyAmenityRepository.
 * Delegates to Spring Data JPA repository.
 */
@Repository
@RequiredArgsConstructor
public class PropertyAmenityRepositoryImpl implements PropertyAmenityRepository {

    private final PropertyAmenityJpaRepository jpaRepository;

    @Override
    public List<PropertyAmenity> findByPropertyIdWithAmenity(UUID propertyId) {
        return jpaRepository.findByPropertyIdWithAmenity(propertyId);
    }

    @Override
    public List<PropertyAmenity> findByPropertyIdsWithAmenity(List<UUID> propertyIds) {
        return jpaRepository.findByPropertyIdsWithAmenity(propertyIds);
    }
}
