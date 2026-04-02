package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.Property3DGeneration;
import com.sep.realvista.domain.property.Property3DGenerationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Property3DGenerationJpaRepository extends JpaRepository<Property3DGeneration, UUID> {
    Optional<Property3DGeneration> findByOperationIdAndDeletedIsFalse(String operationId);
    List<Property3DGeneration> findByPropertyIdAndDeletedIsFalseOrderByCreatedAtDesc(UUID propertyId);
    List<Property3DGeneration> findByStatusAndDeletedIsFalse(Property3DGenerationStatus status);
}
