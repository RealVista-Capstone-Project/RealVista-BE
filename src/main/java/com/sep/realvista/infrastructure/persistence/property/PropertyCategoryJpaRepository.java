package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.PropertyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PropertyCategoryJpaRepository extends JpaRepository<PropertyCategory, UUID> {
    Optional<PropertyCategory> findByCode(String code);
}
