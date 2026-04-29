package com.sep.realvista.presentation.rest.location;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.location.dto.AdminLocationListRequest;
import com.sep.realvista.application.location.dto.CreateLocationRequest;
import com.sep.realvista.application.location.dto.UpdateLocationRequest;
import com.sep.realvista.application.property.dto.LocationResponseDTO;
import com.sep.realvista.application.location.service.LocationApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Endpoints for retrieving administrative bounds")
@Slf4j
public class LocationController {

    private final LocationApplicationService locationApplicationService;

    // ── Public endpoints ─────────────────────────────────────────────────────

    @GetMapping("/api/v1/locations/cities")
    @Operation(summary = "Get all cities")
    public ResponseEntity<ApiResponse<List<LocationResponseDTO>>> getCities() {
        log.info("REST request to get all cities");
        List<LocationResponseDTO> cities = locationApplicationService.getCities();
        return ResponseEntity.ok(ApiResponse.success("Cities retrieved successfully", cities));
    }

    @GetMapping("/api/v1/locations/districts")
    @Operation(summary = "Get all districts", description = "Returns all locations of type DISTRICT across all cities")
    public ResponseEntity<ApiResponse<List<LocationResponseDTO>>> getAllDistricts() {
        log.info("REST request to get all districts");
        List<LocationResponseDTO> districts = locationApplicationService.getAllDistricts();
        return ResponseEntity.ok(ApiResponse.success("Districts retrieved successfully", districts));
    }

    @GetMapping("/api/v1/locations/{parentId}/children")
    @Operation(
            summary = "Get children locations by parent ID",
            description = "Get Districts by City ID or Wards by District ID"
    )
    public ResponseEntity<ApiResponse<List<LocationResponseDTO>>> getChildrenLocations(
            @PathVariable UUID parentId) {
        log.info("REST request to get children locations for parent ID: {}", parentId);
        List<LocationResponseDTO> locations = locationApplicationService.getChildrenLocations(parentId);
        return ResponseEntity.ok(ApiResponse.success("Locations retrieved successfully", locations));
    }

    @GetMapping("/api/v1/locations/search")
    @Operation(summary = "Find specific location by coordinates",
            description = "Find Ward > District > City containing the point")
    public ResponseEntity<ApiResponse<LocationResponseDTO>> searchByCoordinates(
            @Parameter(description = "Latitude") @RequestParam BigDecimal lat,
            @Parameter(description = "Longitude") @RequestParam BigDecimal lng) {
        log.info("REST request to search location by coordinates: [{}, {}]", lat, lng);
        return locationApplicationService.findSpecificLocationByCoordinates(lat, lng)
                .map(location -> ResponseEntity.ok(ApiResponse.success("Location found", location)))
                .orElseGet(() -> {
                    log.warn("No location found for coordinates: [{}, {}]", lat, lng);
                    return ResponseEntity.ok(ApiResponse.error("No location found at these coordinates"));
                });
    }

    // ── Admin endpoints ──────────────────────────────────────────────────────

    @GetMapping("/api/v1/admin/locations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: list locations with filtering and pagination")
    public ResponseEntity<ApiResponse<Page<LocationResponseDTO>>> adminListLocations(
            AdminLocationListRequest filter,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin REST request to list locations, filter={}", filter);
        Page<LocationResponseDTO> page = locationApplicationService.adminListLocations(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Locations retrieved successfully", page));
    }

    @PostMapping("/api/v1/admin/locations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: create a new location (geocoding applied automatically)")
    public ResponseEntity<ApiResponse<LocationResponseDTO>> createLocation(
            @Valid @RequestBody CreateLocationRequest req) {
        log.info("Admin REST request to create location name='{}' level={}", req.getName(), req.getLevel());
        LocationResponseDTO created = locationApplicationService.createLocation(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Location created successfully", created));
    }

    @PutMapping("/api/v1/admin/locations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: update location name/code")
    public ResponseEntity<ApiResponse<LocationResponseDTO>> updateLocation(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLocationRequest req) {
        log.info("Admin REST request to update location id={}", id);
        LocationResponseDTO updated = locationApplicationService.updateLocation(id, req);
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully", updated));
    }
}

