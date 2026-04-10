package com.sep.realvista.domain.property.repository;

import com.sep.realvista.domain.property.Property3DGeneration;
import com.sep.realvista.domain.property.Property3DGenerationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Property3DGenerationRepository {
    Property3DGeneration save(Property3DGeneration generation);
    Optional<Property3DGeneration> findById(UUID id);
    Optional<Property3DGeneration> findByOperationId(String operationId);
    List<Property3DGeneration> findByPropertyId(UUID propertyId);
    List<Property3DGeneration> findByStatus(Property3DGenerationStatus status);
    void delete(Property3DGeneration generation);
}
