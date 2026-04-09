package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.PropertyMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PropertyMediaJpaRepository extends JpaRepository<PropertyMedia, UUID> {
    @Query("SELECT pm FROM PropertyMedia pm WHERE pm.propertyId = :propertyId "
           + "AND pm.deleted = false ORDER BY pm.isPrimary DESC")
    List<PropertyMedia> findByPropertyId(@Param("propertyId") UUID propertyId);

    List<PropertyMedia> findByPropertyIdAndDeletedIsFalse(UUID propertyId);

    void deleteByPropertyId(UUID propertyId);
}
