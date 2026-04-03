package com.sep.realvista.domain.property.location.repository;

import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.domain.property.location.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {
    List<Location> findByTypeOrderByNameAsc(LocationType type);
    List<Location> findByParentIdOrderByNameAsc(UUID parentId);
    
    @org.springframework.data.jpa.repository.Query("SELECT l FROM Location l WHERE "
            + ":lat >= l.southLat AND :lat <= l.northLat AND "
            + ":lng >= l.westLng AND :lng <= l.eastLng "
            + "ORDER BY CASE l.type "
            + "WHEN 'WARD' THEN 1 WHEN 'DISTRICT' THEN 2 WHEN 'CITY' THEN 3 ELSE 4 END ASC")
    List<Location> findContainingLocations(@Param("lat") java.math.BigDecimal lat,
                                           @Param("lng") java.math.BigDecimal lng);
}
