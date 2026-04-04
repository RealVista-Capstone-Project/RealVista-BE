package com.sep.realvista.application.location.service;

import com.sep.realvista.application.location.dto.DistrictLocationResponse;
import com.sep.realvista.application.location.dto.WardResponse;
import com.sep.realvista.application.property.dto.LocationResponseDTO;
import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.domain.property.location.LocationRepository;
import com.sep.realvista.domain.property.location.LocationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional(readOnly = true)
    public List<LocationResponseDTO> getCities() {
        log.info("Fetching all cities");
        return locationRepository.findByTypeOrderByNameAsc(LocationType.CITY)
                .stream()
                .map(location -> LocationResponseDTO.builder()
                        .locationId(location.getLocationId())
                        .code(location.getCode())
                        .name(location.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationResponseDTO> getChildrenLocations(UUID parentId) {
        log.info("Fetching children locations for parent ID: {}", parentId);
        return locationRepository.findByParentIdOrderByNameAsc(parentId)
                .stream()
                .map(location -> LocationResponseDTO.builder()
                        .locationId(location.getLocationId())
                        .code(location.getCode())
                        .name(location.getName())
                        .build())
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

    private LocationResponseDTO mapToDTO(com.sep.realvista.domain.property.location.Location location) {
        return LocationResponseDTO.builder()
                .locationId(location.getLocationId())
                .code(location.getCode())
                .name(location.getName())
                .build();
    }
}
