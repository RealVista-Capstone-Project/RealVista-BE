package com.sep.realvista.infrastructure.persistence.property.location;

import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.domain.property.location.LocationStatus;
import com.sep.realvista.domain.property.location.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LocationJpaRepository extends JpaRepository<Location, UUID>, JpaSpecificationExecutor<Location> {

    List<Location> findByTypeOrderByNameAsc(LocationType type);

    @Query("SELECT l FROM Location l WHERE l.type = :type AND l.status = :status "
            + "AND l.deleted = false ORDER BY l.sortOrder ASC, l.name ASC")
    List<Location> findActiveByTypeOrderByNameAsc(@Param("type") LocationType type,
                                                  @Param("status") LocationStatus status);

    List<Location> findByParentIdOrderByNameAsc(UUID parentId);

    @Query("SELECT l FROM Location l WHERE l.parentId = :parentId AND l.status = :status "
            + "AND l.deleted = false ORDER BY l.sortOrder ASC, l.name ASC")
    List<Location> findActiveByParentIdOrderByNameAsc(@Param("parentId") UUID parentId,
                                                      @Param("status") LocationStatus status);

    @Query("SELECT COUNT(l) > 0 FROM Location l WHERE LOWER(l.code) = LOWER(:code) "
            + "AND l.deleted = false AND (:excludedId IS NULL OR l.locationId <> :excludedId)")
    boolean existsByCodeIgnoreCase(@Param("code") String code, @Param("excludedId") UUID excludedId);

    @Query("SELECT COUNT(l) > 0 FROM Location l WHERE LOWER(l.name) = LOWER(:name) "
            + "AND l.deleted = false "
            + "AND ((:parentId IS NULL AND l.parentId IS NULL) OR l.parentId = :parentId) "
            + "AND (:excludedId IS NULL OR l.locationId <> :excludedId)")
    boolean existsByNameAndParentIdIgnoreCase(@Param("name") String name,
                                              @Param("parentId") UUID parentId,
                                              @Param("excludedId") UUID excludedId);

    @Query("SELECT l FROM Location l LEFT JOIN FETCH l.parent "
            + "WHERE l.type = :type AND l.deleted = false AND l.status = :status "
            + "ORDER BY l.sortOrder ASC, l.name ASC")
    List<Location> findAllByTypeWithParent(@Param("type") LocationType type,
                                           @Param("status") LocationStatus status);

    @Query("SELECT l FROM Location l WHERE l.type = 'WARD' "
            + "AND l.parentId IN :parentIds AND l.deleted = false AND l.status = :status "
            + "ORDER BY l.sortOrder ASC, l.name ASC")
    List<Location> findWardsByParentIds(@Param("parentIds") List<UUID> parentIds,
                                        @Param("status") LocationStatus status);

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

    @Query("SELECT l FROM Location l "
           + "WHERE l.southLat <= :lat AND l.northLat >= :lat "
           + "AND l.westLng <= :lng AND l.eastLng >= :lng "
           + "AND l.deleted = false "
           + "ORDER BY CASE l.type WHEN 'WARD' THEN 1 WHEN 'DISTRICT' THEN 2 WHEN 'CITY' THEN 3 ELSE 4 END")
    List<Location> findContainingLocations(@Param("lat") java.math.BigDecimal lat,
                                           @Param("lng") java.math.BigDecimal lng);
}
