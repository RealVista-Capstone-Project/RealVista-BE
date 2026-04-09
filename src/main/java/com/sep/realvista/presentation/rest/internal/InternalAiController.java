package com.sep.realvista.presentation.rest.internal;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.listing.dto.ListingSearchCriteria;
import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.application.listing.service.ListingSearchService;
import com.sep.realvista.application.location.dto.DistrictLocationResponse;
import com.sep.realvista.application.location.service.LocationApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Internal API controller for AI service integration.
 * Protected by API key authentication via x-service-api-key header.
 */
@Slf4j
@RestController
@RequestMapping("/internal/ai")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INTERNAL_SERVICE')")
@Tag(name = "Internal - AI", description = "Internal endpoints for AI service consumption")
public class InternalAiController {

    private final LocationApplicationService locationApplicationService;
    private final ListingSearchService listingSearchService;

    @GetMapping("/locations")
    @Operation(
            summary = "Get all districts with wards",
            description = "Returns all districts grouped by city with their ward names. "
                    + "Authenticated via x-service-api-key header."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Locations retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or missing API key")
    })
    public ResponseEntity<ApiResponse<List<DistrictLocationResponse>>> getLocations() {
        MDC.put("traceId", UUID.randomUUID().toString());
        log.info("Internal AI request: fetching all district locations with wards");

        List<DistrictLocationResponse> locations = locationApplicationService.getAllDistrictsWithWards();

        log.info("Returning {} districts with wards", locations.size());
        return ResponseEntity.ok(ApiResponse.success("Locations retrieved successfully", locations));
    }

    @GetMapping("/listings")
    @Operation(
            summary = "Search listings for AI service",
            description = "Search published listings using filter criteria. "
                    + "Same functionality as /api/v1/listings/search but authenticated "
                    + "via x-service-api-key header instead of JWT. "
                    + "Bookmark/favorite status is not included."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Listings retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or missing API key")
    })
    public ResponseEntity<ApiResponse<PageResponse<ListingSearchResponse>>> searchListings(
            @org.springdoc.core.annotations.ParameterObject ListingSearchCriteria criteria,
            @PageableDefault(size = 20) Pageable pageable) {

        MDC.put("traceId", UUID.randomUUID().toString());
        log.info("Internal AI request: searching listings with criteria: {}", criteria);

        Page<ListingSearchResponse> results = listingSearchService.search(criteria, pageable, null);

        PageResponse<ListingSearchResponse> pageResponse = PageResponse.<ListingSearchResponse>builder()
                .content(results.getContent())
                .page(results.getNumber())
                .size(results.getSize())
                .totalElements(results.getTotalElements())
                .totalPages(results.getTotalPages())
                .first(results.isFirst())
                .last(results.isLast())
                .build();

        log.info("Returning {} listings (page {}/{})",
                results.getNumberOfElements(), results.getNumber(), results.getTotalPages());
        return ResponseEntity.ok(ApiResponse.success("Listings retrieved successfully", pageResponse));
    }
}
