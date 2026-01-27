package com.sep.realvista.presentation.rest.listing;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.listing.dto.ListingDetailResponse;
import com.sep.realvista.application.listing.service.ListingApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST Controller for Listing operations.
 */
@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
@Tag(name = "Listing Management", description = "Endpoints for managing property listings")
@Slf4j
public class ListingController {

    private final ListingApplicationService listingApplicationService;

    @GetMapping("/{id}")
    @Operation(summary = "Get listing detail by ID",
            description = "Retrieves complete listing information including media, property, location, type, category, and agent/owner")
    public ResponseEntity<ApiResponse<ListingDetailResponse>> getListingDetail(@PathVariable UUID id) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching listing detail - traceId: {}, listingId: {}", traceId, id);

        ListingDetailResponse listing = listingApplicationService.getListingDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Listing retrieved successfully", listing));
    }
}
