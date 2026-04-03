package com.sep.realvista.infrastructure.persistence.property.location;

import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.domain.property.location.LocationRepository;
import com.sep.realvista.domain.property.location.LocationType;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LocationRepositoryImpl implements LocationRepository {

    private final LocationJpaRepository jpaRepository;

    @Override
    public Optional<Location> findById(UUID locationId) {
        return jpaRepository.findById(locationId);
    }

    @Override
    public List<Location> findByTypeOrderByNameAsc(LocationType type) {
        return jpaRepository.findByTypeOrderByNameAsc(type);
    }

    @Override
    public List<Location> findByParentIdOrderByNameAsc(UUID parentId) {
        return jpaRepository.findByParentIdOrderByNameAsc(parentId);
    }

    @Override
    public List<Location> findAllActiveDistricts() {
        return jpaRepository.findAllByTypeWithParent(LocationType.DISTRICT);
    }

    @Override
    public List<Location> findActiveWardsByParentIds(List<UUID> districtIds) {
        if (districtIds == null || districtIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findWardsByParentIds(districtIds);
    }

    @Override
    public List<UUID> findDescendantWardIds(UUID locationId) {
        Optional<Location> locationOpt = jpaRepository.findById(locationId);
        if (locationOpt.isEmpty()) {
            return List.of();
        }

        Location location = locationOpt.get();
        switch (location.getType()) {
            case CITY:
                return jpaRepository.findWardIdsByCityId(locationId);
            case DISTRICT:
                return jpaRepository.findWardIdsByDistrictId(locationId);
            case WARD:
                return List.of(locationId);
            default:
                return List.of();
        }
    }
}
