package com.sep.realvista.infrastructure.persistence.property.fee;

import com.sep.realvista.domain.property.fee.PropertyFeeService;
import com.sep.realvista.domain.property.fee.PropertyFeeServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of PropertyFeeServiceRepository.
 */
@Repository
@RequiredArgsConstructor
public class PropertyFeeServiceRepositoryImpl implements PropertyFeeServiceRepository {

    private final PropertyFeeServiceJpaRepository jpaRepository;

    @Override
    public List<PropertyFeeService> findByPropertyId(UUID propertyId) {
        return jpaRepository.findByPropertyId(propertyId);
    }
}
