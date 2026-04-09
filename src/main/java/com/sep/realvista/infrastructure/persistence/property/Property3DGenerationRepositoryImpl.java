package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.Property3DGeneration;
import com.sep.realvista.domain.property.Property3DGenerationStatus;
import com.sep.realvista.domain.property.repository.Property3DGenerationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class Property3DGenerationRepositoryImpl implements Property3DGenerationRepository {

    private final Property3DGenerationJpaRepository jpaRepository;

    @Override
    public Property3DGeneration save(Property3DGeneration generation) {
        return jpaRepository.save(generation);
    }

    @Override
    public Optional<Property3DGeneration> findById(UUID id) {
        return jpaRepository.findById(id).filter(gen -> Boolean.FALSE.equals(gen.getDeleted()));
    }

    @Override
    public Optional<Property3DGeneration> findByOperationId(String operationId) {
        return jpaRepository.findByOperationIdAndDeletedIsFalse(operationId);
    }

    @Override
    public List<Property3DGeneration> findByPropertyId(UUID propertyId) {
        return jpaRepository.findByPropertyIdAndDeletedIsFalseOrderByCreatedAtDesc(propertyId);
    }

    @Override
    public List<Property3DGeneration> findByStatus(Property3DGenerationStatus status) {
        return jpaRepository.findByStatusAndDeletedIsFalse(status);
    }

    @Override
    public void delete(Property3DGeneration generation) {
        generation.markAsDeleted();
        jpaRepository.save(generation);
    }
}
