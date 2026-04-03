package com.sep.realvista.application.property.service;

import com.sep.realvista.application.property.dto.LocationResponseDTO;
import com.sep.realvista.domain.property.location.LocationType;
import com.sep.realvista.domain.property.location.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LocationApplicationService {

    private final LocationRepository locationRepository;

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

    public List<LocationResponseDTO> getChildrenLocations(UUID parentId) {
        log.info("Fetching children locations for parent ID: {}", parentId);
        return locationRepository.findByParentIdOrderByNameAsc(parentId)
                .stream()
                .map(this::mapToDTO)
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
