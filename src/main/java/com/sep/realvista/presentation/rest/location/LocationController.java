package com.sep.realvista.presentation.rest.location;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.property.dto.LocationResponseDTO;
import com.sep.realvista.application.location.service.LocationApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Endpoints for retrieving administrative bounds")
@Slf4j
public class LocationController {

    private final LocationApplicationService locationApplicationService;

    @GetMapping("/cities")
    @Operation(summary = "Get all cities")
    public ResponseEntity<ApiResponse<List<LocationResponseDTO>>> getCities() {
        log.info("REST request to get all cities");
        List<LocationResponseDTO> cities = locationApplicationService.getCities();
        return ResponseEntity.ok(ApiResponse.success("Cities retrieved successfully", cities));
    }

    @GetMapping("/{parentId}/children")
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
}
