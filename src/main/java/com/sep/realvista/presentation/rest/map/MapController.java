package com.sep.realvista.presentation.rest.map;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.listing.dto.map.MapSearchRequest;
import com.sep.realvista.application.listing.dto.map.MapSearchResponse;
import com.sep.realvista.application.listing.service.MapSearchApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST Controller for map-based property searches.
 * Provides endpoints for fetching property listings within geographical bounds.
 */
@RestController
@RequestMapping("/api/v1/map")
@RequiredArgsConstructor
@Tag(name = "Map Search", description = "Map-based property search endpoints")
@Slf4j
public class MapController {

    private final MapSearchApplicationService mapSearchService;

    /**
     * POST /api/v1/map/listings
     * Search properties within map bounds.
     * Uses POST with JSON body to support snake_case parameters and complex filters.
     *
     * @param request map search request with bounds and filters
     * @return response with property markers and metadata
     */
    @PostMapping("/listings")
    @Operation(
            summary = "Search properties on map",
            description = "Returns property markers within specified geographical "
                    + "bounding box. Supports filtering by listing type, price range, "
                    + "and result limiting. Returns lightweight markers optimized for "
                    + "map rendering. Uses POST to support complex search criteria."
    )
    public ResponseEntity<ApiResponse<MapSearchResponse>> searchPropertiesOnMap(
            @Valid @RequestBody MapSearchRequest request
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Map search request - traceId: {}, bounds: ({},{}) to ({},{})",
                traceId,
                request.getSouthLat(), request.getWestLng(),
                request.getNorthLat(), request.getEastLng());

        try {
            MapSearchResponse response = mapSearchService.searchPropertiesOnMap(request);

            log.info("Map search completed - traceId: {}, markers: {}, total: {}",
                    traceId, response.getContent().size(), response.getTotalElements());

            return ResponseEntity.ok(
                    ApiResponse.success("Properties retrieved successfully", response)
            );
        } catch (IllegalArgumentException e) {
            log.error("Invalid map search request - traceId: {}, error: {}",
                    traceId, e.getMessage());
            throw e;
        } finally {
            MDC.remove("traceId");
        }
    }
}

