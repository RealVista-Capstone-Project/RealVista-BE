package com.sep.realvista.domain.property.location.repository;

import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.domain.property.location.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {
    List<Location> findByTypeOrderByNameAsc(LocationType type);
    List<Location> findByParentIdOrderByNameAsc(UUID parentId);
}
