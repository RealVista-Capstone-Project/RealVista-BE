package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Property entity.
 */
public interface PropertyJpaRepository extends JpaRepository<Property, UUID> {

    @Query("SELECT p FROM Property p WHERE p.propertyId = :id AND p.deleted = false")
    Optional<Property> findActiveById(@Param("id") UUID id);

    List<Property> findByOwnerId(UUID ownerId);

    @Query("SELECT p FROM Property p WHERE (p.ownerId = :userId OR "
           + "EXISTS (SELECT pa FROM com.sep.realvista.domain.agent.PropertyAgent pa "
           + "WHERE pa.propertyId = p.propertyId AND pa.agentId = :userId AND pa.deleted = false)) "
           + "AND p.deleted = false")
    List<Property> findByOwnerIdOrAgentId(@Param("userId") UUID userId);

    @Query("SELECT p FROM Property p WHERE p.latitude BETWEEN :southLat AND :northLat "
           + "AND p.longitude BETWEEN :westLng AND :eastLng AND p.deleted = false")
    List<Property> findByLocationRange(@Param("northLat") java.math.BigDecimal northLat, 
                                        @Param("southLat") java.math.BigDecimal southLat, 
                                        @Param("eastLng") java.math.BigDecimal eastLng, 
                                        @Param("westLng") java.math.BigDecimal westLng);

    @Query("SELECT p FROM Property p WHERE LOWER(p.streetAddress) LIKE LOWER(CONCAT('%', :address, '%')) AND p.deleted = false")
    List<Property> searchByAddress(@Param("address") String address);
}
