package com.sep.realvista.infrastructure.persistence.property.media;

import com.sep.realvista.domain.property.PropertyMedia;
import com.sep.realvista.domain.property.repository.PropertyMediaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyMediaJpaRepository extends JpaRepository<PropertyMedia, UUID>, PropertyMediaRepository {

    @Query("SELECT pm FROM PropertyMedia pm WHERE pm.propertyId = :propertyId "
           + "AND pm.isPropertyStandard = true AND pm.deleted = false ORDER BY pm.isPrimary DESC")
    List<PropertyMedia> findByPropertyId(@Param("propertyId") UUID propertyId);

    void deleteByPropertyId(UUID propertyId);
}
