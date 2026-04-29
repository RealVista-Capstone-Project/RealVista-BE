package com.sep.realvista.application.location.service;

import com.sep.realvista.application.location.dto.AdminLocationListRequest;
import com.sep.realvista.application.location.dto.CreateLocationRequest;
import com.sep.realvista.application.location.dto.DistrictLocationResponse;
import com.sep.realvista.application.location.dto.UpdateLocationRequest;
import com.sep.realvista.application.location.dto.WardResponse;
import com.sep.realvista.application.property.dto.LocationResponseDTO;
import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.domain.property.location.LocationRepository;
import com.sep.realvista.domain.property.location.LocationSpecification;
import com.sep.realvista.domain.property.location.LocationType;
import com.sep.realvista.infrastructure.external.geocoding.GeocodingService;
import com.sep.realvista.infrastructure.external.geocoding.GeocodingService.BoundingBox;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationApplicationService {

    private final LocationRepository locationRepository;
    private final GeocodingService geocodingService;

    @Transactional(readOnly = true)
    public List<LocationResponseDTO> getCities() {
        log.info("Fetching all cities");
        return locationRepository.findByTypeOrderByNameAsc(LocationType.CITY)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationResponseDTO> getChildrenLocations(UUID parentId) {
        log.info("Fetching children locations for parent ID: {}", parentId);
        return locationRepository.findByParentIdOrderByNameAsc(parentId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationResponseDTO> getAllDistricts() {
        log.info("Fetching all districts");
        return locationRepository.findByTypeOrderByNameAsc(LocationType.DISTRICT)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DistrictLocationResponse> getAllDistrictsWithWards() {
        log.debug("Fetching all districts with wards for internal AI service");

        // 1. Fetch all active districts (with parent city eagerly loaded)
        List<Location> districts = locationRepository.findAllActiveDistricts();

        if (districts.isEmpty()) {
            return List.of();
        }

        // 2. Collect district IDs and fetch all wards in a single query
        List<UUID> districtIds = districts.stream()
                .map(Location::getLocationId)
                .collect(Collectors.toList());

        List<Location> allWards = locationRepository.findActiveWardsByParentIds(districtIds);

        // 3. Group wards by their parent district ID as WardResponse objects
        Map<UUID, List<WardResponse>> wardsByDistrictId = allWards.stream()
                .collect(Collectors.groupingBy(
                        Location::getParentId,
                        Collectors.mapping(
                                ward -> WardResponse.builder()
                                        .id(ward.getLocationId())
                                        .name(ward.getName())
                                        .build(),
                                Collectors.toList()
                        )
                ));

        // 4. Map districts to DTOs including cityId
        return districts.stream()
                .map(district -> DistrictLocationResponse.builder()
                        .id(district.getLocationId())
                        .name(district.getName())
                        .type(district.getType().name())
                        .cityId(district.getParent() != null
                                ? district.getParent().getLocationId() : null)
                        .cityName(district.getParent() != null
                                ? district.getParent().getName() : null)
                        .wards(wardsByDistrictId.getOrDefault(
                                district.getLocationId(), Collections.emptyList()))
                        .build())
                .collect(Collectors.toList());
    }

    public Optional<LocationResponseDTO> findSpecificLocationByCoordinates(BigDecimal lat, BigDecimal lng) {
        log.info("Finding specific location for coordinates: [{}, {}]", lat, lng);
        var locations = locationRepository.findContainingLocations(lat, lng);
        if (locations.isEmpty()) {
            return Optional.empty();
        }
        // Return the first one (already sorted by WARD -> DISTRICT -> CITY)
        return Optional.of(mapToDTO(locations.getFirst()));
    }

    // ── Admin endpoints ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<LocationResponseDTO> adminListLocations(AdminLocationListRequest filter, Pageable pageable) {
        log.info("Admin: listing locations with filter={}", filter);
        Specification<Location> spec = LocationSpecification.filterBy(
                filter.getSearch(), filter.getLevel(), filter.getParentId());
        return locationRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    @Transactional
    public LocationResponseDTO createLocation(CreateLocationRequest req) {
        log.info("Admin: creating location name='{}' level={}", req.getName(), req.getLevel());

        // Validate parent constraint
        String parentName = null;
        if (req.getLevel() == LocationType.DISTRICT || req.getLevel() == LocationType.WARD) {
            if (req.getParentId() == null) {
                throw new IllegalArgumentException(
                        req.getLevel() + " requires a parent_id");
            }
            Location parent = locationRepository.findById(req.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Parent location not found: " + req.getParentId()));
            LocationType expectedParentType = req.getLevel() == LocationType.DISTRICT
                    ? LocationType.CITY : LocationType.DISTRICT;
            if (parent.getType() != expectedParentType) {
                throw new IllegalArgumentException(
                        "Parent of a " + req.getLevel() + " must be a " + expectedParentType
                        + " but was " + parent.getType());
            }
            parentName = parent.getName();
        }

        // Build geocoding query: "Ward/District Name, Parent Name, Vietnam"
        String geocodeQuery = buildGeocodeQuery(req.getName(), parentName);
        BoundingBox bbox = geocodingService.getBoundingBox(geocodeQuery);

        Location location = Location.builder()
                .parentId(req.getParentId())
                .type(req.getLevel())
                .name(req.getName())
                .code(req.getCode())
                .northLat(bbox.northLat())
                .southLat(bbox.southLat())
                .eastLng(bbox.eastLng())
                .westLng(bbox.westLng())
                .build();

        Location saved = locationRepository.save(location);
        log.info("Admin: created location id={}", saved.getLocationId());
        return mapToDTO(saved);
    }

    @Transactional
    public LocationResponseDTO updateLocation(UUID id, UpdateLocationRequest req) {
        log.info("Admin: updating location id={}", id);
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location not found: " + id));
        location.update(req.getName(), req.getCode());
        Location saved = locationRepository.save(location);
        return mapToDTO(saved);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private LocationResponseDTO mapToDTO(Location location) {
        return LocationResponseDTO.builder()
                .locationId(location.getLocationId())
                .code(location.getCode())
                .name(location.getName())
                .parentId(location.getParentId())
                .level(location.getType() != null ? location.getType().name() : null)
                .build();
    }

    private String buildGeocodeQuery(String name, String parentName) {
        if (parentName != null) {
            return name + ", " + parentName + ", Vietnam";
        }
        return name + ", Vietnam";
    }
}

