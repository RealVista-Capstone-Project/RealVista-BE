package com.sep.realvista.infrastructure.persistence.property.fee;

import com.sep.realvista.domain.property.fee.PropertyFeeService;
import com.sep.realvista.domain.property.fee.PropertyFeeServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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

    @Override
    public Optional<PropertyFeeService> findById(UUID feeId) {
        return jpaRepository.findById(feeId);
    }

    @Override
    public PropertyFeeService save(PropertyFeeService fee) {
        return jpaRepository.save(fee);
    }

    @Override
    public void softDelete(UUID feeId) {
        jpaRepository.findById(feeId).ifPresent(fee -> {
            fee.markAsDeleted();
            jpaRepository.save(fee);
        });
    }

    @Override
    public void softDeleteAllByPropertyId(UUID propertyId) {
        jpaRepository.findByPropertyId(propertyId).forEach(fee -> {
            fee.markAsDeleted();
            jpaRepository.save(fee);
        });
    }
}
