package com.sep.realvista.application.property.service;

import com.sep.realvista.application.property.dto.LocationResponseDTO;
import com.sep.realvista.domain.property.location.LocationType;
import com.sep.realvista.domain.property.location.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
                .map(location -> LocationResponseDTO.builder()
                        .locationId(location.getLocationId())
                        .code(location.getCode())
                        .name(location.getName())
                        .build())
                .collect(Collectors.toList());
    }
}
