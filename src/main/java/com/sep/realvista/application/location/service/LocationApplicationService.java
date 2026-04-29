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
import com.sep.realvista.domain.property.location.LocationStatus;
import com.sep.realvista.domain.property.location.LocationType;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.infrastructure.external.geocoding.GeocodingService;
import com.sep.realvista.infrastructure.external.geocoding.GeocodingService.BoundingBox;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationApplicationService {

    private final LocationRepository locationRepository;
    private final PropertyRepository propertyRepository;
    private final GeocodingService geocodingService;

    @Transactional(readOnly = true)
    public List<LocationResponseDTO> getCities() {
        log.info("Fetching all cities");
        return locationRepository.findActiveByTypeOrderByNameAsc(LocationType.CITY)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationResponseDTO> getChildrenLocations(UUID parentId) {
        log.info("Fetching children locations for parent ID: {}", parentId);
        return locationRepository.findActiveByParentIdOrderByNameAsc(parentId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationResponseDTO> getAllDistricts() {
        log.info("Fetching all districts");
        return locationRepository.findActiveByTypeOrderByNameAsc(LocationType.DISTRICT)
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
                filter.getSearch(), filter.getLevel(), filter.getParentId(), filter.getStatus());
        Pageable effectivePageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        Sort.by("sortOrder").ascending().and(Sort.by("name").ascending()));
        return locationRepository.findAll(spec, effectivePageable).map(this::mapToDTOWithUsage);
    }

    @Transactional
    public LocationResponseDTO createLocation(CreateLocationRequest req) {
        log.info("Admin: creating location name='{}' level={}", req.getName(), req.getLevel());

        Location parent = validateHierarchy(req.getLevel(), req.getParentId());
        validateUniqueCode(req.getCode(), null);
        String parentName = parent != null ? parent.getName() : null;
        validateUniqueName(req.getName(), req.getParentId(), null, parentName);

        String geocodeQuery = buildGeocodeQuery(req.getName(), parentName);
        BoundingBox bbox = geocodingService.getBoundingBox(geocodeQuery);
        if (isZeroBounds(bbox)) {
            throw new IllegalArgumentException(
                    "Không thể xác định vùng bản đồ cho địa điểm này (geocoding thất bại hoặc chưa được cấu hình)");
        }

        Location location = Location.builder()
                .parentId(req.getParentId())
                .type(req.getLevel())
                .name(req.getName())
                .code(req.getCode())
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .northLat(bbox.northLat())
                .southLat(bbox.southLat())
                .eastLng(bbox.eastLng())
                .westLng(bbox.westLng())
                .build();

        location.initializeNormalizedName();
        Location saved = locationRepository.save(location);
        log.info("Admin: created location id={}", saved.getLocationId());
        return mapToDTO(saved);
    }

    @Transactional
    public LocationResponseDTO updateLocation(UUID id, UpdateLocationRequest req) {
        log.info("Admin: updating location id={}", id);
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location not found: " + id));
        if (req.getCode() != null) {
            validateUniqueCode(req.getCode(), id);
        }
        if (req.getName() != null && !req.getName().isBlank()) {
            String parentName = location.getParentId() != null
                    ? locationRepository.findById(location.getParentId()).map(Location::getName).orElse(null)
                    : null;
            validateUniqueName(req.getName(), location.getParentId(), id, parentName);
        }
        location.update(req.getName(), req.getCode());
        location.updateSortOrder(req.getSortOrder());
        Location saved = locationRepository.save(location);
        return mapToDTOWithUsage(saved);
    }

    @Transactional
    public LocationResponseDTO archiveLocation(UUID id) {
        log.info("Admin: archiving location id={}", id);
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location not found: " + id));
        location.archive();
        return mapToDTOWithUsage(locationRepository.save(location));
    }

    @Transactional
    public LocationResponseDTO activateLocation(UUID id) {
        log.info("Admin: activating location id={}", id);
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location not found: " + id));
        location.activate();
        return mapToDTOWithUsage(locationRepository.save(location));
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private LocationResponseDTO mapToDTO(Location location) {
        return LocationResponseDTO.builder()
                .locationId(location.getLocationId())
                .code(location.getCode())
                .name(location.getName())
                .parentId(location.getParentId())
                .level(location.getType() != null ? location.getType().name() : null)
                .status(location.getStatus() != null ? location.getStatus().name() : null)
                .sortOrder(location.getSortOrder())
                .northLat(location.getNorthLat())
                .southLat(location.getSouthLat())
                .eastLng(location.getEastLng())
                .westLng(location.getWestLng())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .build();
    }

    private LocationResponseDTO mapToDTOWithUsage(Location location) {
        LocationResponseDTO dto = mapToDTO(location);
        dto.setUsedByPropertiesCount(countUsedByProperties(location));
        return dto;
    }

    private Location validateHierarchy(LocationType level, UUID parentId) {
        if (level == LocationType.CITY) {
            if (parentId != null) {
                throw new IllegalArgumentException("Tỉnh/Thành phố không thể thuộc đơn vị hành chính khác");
            }
            return null;
        }
        if (parentId == null) {
            throw new IllegalArgumentException(
                    levelLabel(level) + " phải thuộc một "
                            + levelLabel(level == LocationType.DISTRICT
                            ? LocationType.CITY : LocationType.DISTRICT)
            );
        }
        Location parent = locationRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy đơn vị hành chính cấp trên: " + parentId));
        LocationType expectedParentType = level == LocationType.DISTRICT
                ? LocationType.CITY : LocationType.DISTRICT;
        if (parent.getType() != expectedParentType) {
            throw new IllegalArgumentException(
                    levelLabel(level) + " phải thuộc " + levelLabel(expectedParentType)
                    + ", không thể thuộc " + levelLabel(parent.getType()));
        }
        if (parent.getStatus() != LocationStatus.ACTIVE) {
            throw new IllegalArgumentException("Đơn vị hành chính cấp trên chưa được kích hoạt");
        }
        return parent;
    }

    private static String levelLabel(LocationType type) {
        return switch (type) {
            case CITY -> "Tỉnh/Thành phố";
            case DISTRICT -> "Quận/Huyện";
            case WARD -> "Phường/Xã";
        };
    }

    private void validateUniqueCode(String code, UUID excludedId) {
        if (code != null && locationRepository.existsByCodeIgnoreCase(code, excludedId)) {
            throw new IllegalArgumentException("Mã địa điểm \"" + code + "\" đã tồn tại. Vui lòng chọn mã khác.");
        }
    }

    private void validateUniqueName(String name, UUID parentId, UUID excludedId, String parentName) {
        if (locationRepository.existsByNameAndParentIdIgnoreCase(name, parentId, excludedId)) {
            String context = parentName != null
                    ? " đã tồn tại trong " + parentName
                    : " đã tồn tại ở cấp tỉnh/thành phố";
            throw new IllegalArgumentException("\"" + name + "\"" + context
                    + ". Vui lòng chọn tên khác.");
        }
    }

    private long countUsedByProperties(Location location) {
        Set<UUID> locationIds = new LinkedHashSet<>();
        locationIds.add(location.getLocationId());
        locationIds.addAll(locationRepository.findDescendantWardIds(location.getLocationId()));
        return propertyRepository.countByLocationIds(List.copyOf(locationIds));
    }

    private boolean isZeroBounds(BoundingBox bbox) {
        return BigDecimal.ZERO.compareTo(bbox.northLat()) == 0
                && BigDecimal.ZERO.compareTo(bbox.southLat()) == 0
                && BigDecimal.ZERO.compareTo(bbox.eastLng()) == 0
                && BigDecimal.ZERO.compareTo(bbox.westLng()) == 0;
    }

    private String buildGeocodeQuery(String name, String parentName) {
        if (parentName != null) {
            return name + ", " + parentName + ", Vietnam";
        }
        return name + ", Vietnam";
    }
}
