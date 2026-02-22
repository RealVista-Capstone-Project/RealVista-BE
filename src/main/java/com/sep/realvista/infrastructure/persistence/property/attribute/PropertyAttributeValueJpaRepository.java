package com.sep.realvista.infrastructure.persistence.property.attribute;

import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for PropertyAttributeValue entity.
 */
public interface PropertyAttributeValueJpaRepository extends JpaRepository<PropertyAttributeValue, UUID> {

        @Query("SELECT pav FROM PropertyAttributeValue pav "
                        + "LEFT JOIN FETCH pav.propertyAttribute pa "
                        + "LEFT JOIN FETCH pav.property p "
                        + "WHERE p.propertyId = :propertyId AND pav.deleted = false "
                        + "ORDER BY pa.name")
        List<PropertyAttributeValue> findByPropertyIdWithAttribute(@Param("propertyId") UUID propertyId);

        @Query("SELECT pav FROM PropertyAttributeValue pav "
                        + "LEFT JOIN FETCH pav.propertyAttribute pa "
                        + "WHERE pav.propertyAttributeValueId = :id AND pav.deleted = false")
        Optional<PropertyAttributeValue> findByIdWithAttribute(@Param("id") UUID id);

        /**
         * Fetch required attributes for multiple properties in a single batch query.
         * Only returns attributes marked as is_required = true in
         * property_type_attributes
         * for each property's type. Used for similar listings cards.
         */
        @Query("SELECT pav FROM PropertyAttributeValue pav "
                        + "JOIN FETCH pav.propertyAttribute pa "
                        + "JOIN pav.property p "
                        + "WHERE p.propertyId IN :propertyIds "
                        + "AND pav.deleted = false "
                        + "AND EXISTS (SELECT 1 FROM PropertyTypeAttribute pta "
                        + "  WHERE pta.propertyAttributeId = pa.propertyAttributeId "
                        + "  AND pta.propertyTypeId = p.propertyTypeId "
                        + "  AND pta.isRequired = true "
                        + "  AND pta.deleted = false) "
                        + "ORDER BY p.propertyId, pa.name")
        List<PropertyAttributeValue> findRequiredAttributesByPropertyIds(
                        @Param("propertyIds") List<UUID> propertyIds);

}
