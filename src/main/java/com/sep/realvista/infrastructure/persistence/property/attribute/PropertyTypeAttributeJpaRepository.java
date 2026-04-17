package com.sep.realvista.infrastructure.persistence.property.attribute;

import com.sep.realvista.domain.property.attribute.PropertyTypeAttribute;
import com.sep.realvista.domain.property.attribute.repository.PropertyTypeAttributeRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PropertyTypeAttributeJpaRepository
        extends JpaRepository<PropertyTypeAttribute, UUID>, PropertyTypeAttributeRepository {

    @Override
    @Query("SELECT pta FROM PropertyTypeAttribute pta JOIN FETCH pta.propertyAttribute "
            + "WHERE pta.propertyTypeId = :propertyTypeId ORDER BY pta.priority ASC")
    List<PropertyTypeAttribute> findByPropertyTypeId(@Param("propertyTypeId") UUID propertyTypeId);
}
