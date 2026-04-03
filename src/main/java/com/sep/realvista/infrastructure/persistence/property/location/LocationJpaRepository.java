package com.sep.realvista.infrastructure.persistence.property.location;

import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.domain.property.location.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LocationJpaRepository extends JpaRepository<Location, UUID> {

    List<Location> findByTypeOrderByNameAsc(LocationType type);

    List<Location> findByParentIdOrderByNameAsc(UUID parentId);

    @Query("SELECT l FROM Location l LEFT JOIN FETCH l.parent "
            + "WHERE l.type = :type AND l.deleted = false ORDER BY l.name")
    List<Location> findAllByTypeWithParent(@Param("type") LocationType type);

    @Query("SELECT l FROM Location l WHERE l.type = 'WARD' "
            + "AND l.parentId IN :parentIds AND l.deleted = false ORDER BY l.name")
    List<Location> findWardsByParentIds(@Param("parentIds") List<UUID> parentIds);

    /**
     * Find all ward IDs under a given district (direct children of type WARD).
     */
    @Query("SELECT l.locationId FROM Location l "
            + "WHERE l.parentId = :districtId AND l.type = 'WARD' AND l.deleted = false")
    List<UUID> findWardIdsByDistrictId(@Param("districtId") UUID districtId);

    /**
     * Find all ward IDs under a given city (wards whose parent district has the city as parent).
     */
    @Query("SELECT w.locationId FROM Location w "
            + "WHERE w.type = 'WARD' AND w.deleted = false "
            + "AND w.parentId IN ("
            + "  SELECT d.locationId FROM Location d "
            + "  WHERE d.parentId = :cityId AND d.type = 'DISTRICT' AND d.deleted = false"
            + ")")
    List<UUID> findWardIdsByCityId(@Param("cityId") UUID cityId);
}
