package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Property entity.
 */
public interface PropertyJpaRepository extends JpaRepository<Property, UUID> {

    @Query("SELECT p FROM Property p WHERE p.propertyId = :id AND p.deleted = false")
    Optional<Property> findActiveById(@Param("id") UUID id);
}
