package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyTypeJpaRepository extends JpaRepository<PropertyType, UUID> {
    Optional<PropertyType> findByCode(String code);

    @Query("SELECT pt FROM PropertyType pt LEFT JOIN FETCH pt.propertyCategory "
            + "WHERE pt.status = 'ACTIVE' AND pt.deleted = false ORDER BY pt.name ASC")
    List<PropertyType> findAllActive();
}
