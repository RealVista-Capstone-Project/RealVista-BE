package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PropertyTypeJpaRepository extends JpaRepository<PropertyType, UUID> {
    Optional<PropertyType> findByCode(String code);
}
