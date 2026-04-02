package com.sep.realvista.domain.property.repository;

import com.sep.realvista.domain.property.Property;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository {

    Property save(Property property);

    Optional<Property> findById(UUID id);

    List<Property> findByOwnerId(UUID ownerId);

    List<Property> findByOwnerIdOrAgentId(UUID userId);

    boolean existsById(UUID id);

    void deleteById(UUID id);

    void deleteAll();

    List<Property> findInLocationRange(java.math.BigDecimal northLat, java.math.BigDecimal southLat, 
                                        java.math.BigDecimal eastLng, java.math.BigDecimal westLng);

    List<Property> searchByAddress(String address);
}
