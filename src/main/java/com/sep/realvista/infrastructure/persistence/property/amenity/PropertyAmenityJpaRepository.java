package com.sep.realvista.infrastructure.persistence.property.amenity;

import com.sep.realvista.domain.property.amenity.PropertyAmenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for PropertyAmenity entity.
 */
public interface PropertyAmenityJpaRepository extends JpaRepository<PropertyAmenity, UUID> {

    /**
     * Find all amenities for a property with fetched amenity details.
     *
     * @param propertyId the property ID
     * @return list of property amenities with amenity details
     */
    @Query("SELECT pa FROM PropertyAmenity pa "
           + "JOIN FETCH pa.amenity a "
           + "WHERE pa.propertyId = :propertyId AND pa.deleted = false "
           + "ORDER BY a.amenityType, a.amenityName")
    List<PropertyAmenity> findByPropertyIdWithAmenity(@Param("propertyId") UUID propertyId);

    /**
     * Find all amenities for multiple properties in a single batch query.
     *
     * @param propertyIds list of property IDs
     * @return list of property amenities with amenity details
     */
    @Query("SELECT pa FROM PropertyAmenity pa "
           + "JOIN FETCH pa.amenity a "
           + "WHERE pa.propertyId IN :propertyIds AND pa.deleted = false "
           + "ORDER BY pa.propertyId, a.amenityType, a.amenityName")
    List<PropertyAmenity> findByPropertyIdsWithAmenity(@Param("propertyIds") List<UUID> propertyIds);
}
