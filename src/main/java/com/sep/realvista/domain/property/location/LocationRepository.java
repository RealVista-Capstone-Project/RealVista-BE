package com.sep.realvista.domain.property.location;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Location domain entity.
 * Implementations reside in the infrastructure layer.
 */
public interface LocationRepository {

    /**
     * Find a location by its ID.
     */
    Optional<Location> findById(UUID locationId);

    /**
     * Find all locations of a given type, ordered by name ascending.
     */
    List<Location> findByTypeOrderByNameAsc(LocationType type);

    List<Location> findActiveByTypeOrderByNameAsc(LocationType type);

    /**
     * Find all child locations of a given parent, ordered by name ascending.
     */
    List<Location> findByParentIdOrderByNameAsc(UUID parentId);

    List<Location> findActiveByParentIdOrderByNameAsc(UUID parentId);

    /**
     * Find all active districts (non-deleted locations with type DISTRICT).
     */
    List<Location> findAllActiveDistricts();

    /**
     * Find all active wards belonging to a given district (by parent ID).
     */
    List<Location> findActiveWardsByParentIds(List<UUID> districtIds);

    /**
     * Find all active location IDs that are descendants of the given location.
     * For a city: returns all ward IDs under all its districts.
     * For a district: returns all ward IDs under it.
     * For a ward: returns just that ward's ID.
     */
    List<UUID> findDescendantWardIds(UUID locationId);

    boolean existsByCodeIgnoreCase(String code, UUID excludedId);

    boolean existsByNameAndParentIdIgnoreCase(String name, UUID parentId, UUID excludedId);

    List<Location> findContainingLocations(@Param("lat") java.math.BigDecimal lat,
                                           @Param("lng") java.math.BigDecimal lng);

    Location save(Location location);

    Page<Location> findAll(Specification<Location> spec, Pageable pageable);
}
