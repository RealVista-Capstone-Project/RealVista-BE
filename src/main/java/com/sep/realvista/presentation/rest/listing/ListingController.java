package com.sep.realvista.presentation.rest.listing;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.listing.dto.ListingDetailResponse;
import com.sep.realvista.application.listing.dto.PriceHistoryResponse;
import com.sep.realvista.application.listing.dto.ListingSearchCriteria;
import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.application.listing.dto.SimilarListingsResponse;
import com.sep.realvista.application.listing.service.ListingApplicationService;
import com.sep.realvista.application.listing.service.ListingSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final ListingSearchService listingSearchService;

    @Operation(summary = "Search Listings", description = "Search for published listings using various filter criteria.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved matching listings", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PageResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid search criteria provided", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ListingSearchResponse>>> search(
            @org.springdoc.core.annotations.ParameterObject ListingSearchCriteria criteria,
            @org.springframework.data.web.PageableDefault(size = 20) org.springframework.data.domain.Pageable pageable) {

        log.info("Searching listings with criteria: {}", criteria);

        org.springframework.data.domain.Page<ListingSearchResponse> results = listingSearchService.search(criteria,
                pageable);

        PageResponse<ListingSearchResponse> pageResponse = PageResponse.<ListingSearchResponse>builder()
                .content(results.getContent())
                .page(results.getNumber())
                .size(results.getSize())
                .totalElements(results.getTotalElements())
                .totalPages(results.getTotalPages())
                .first(results.isFirst())
                .last(results.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Listings retrieved successfully", pageResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get listing detail by ID",

            description = "Retrieves complete listing information including media, property, "
                    + "location, type, category, and agent/owner")
    public ResponseEntity<ApiResponse<ListingDetailResponse>> getListingDetail(@PathVariable UUID id) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching listing detail - traceId: {}, listingId: {}", traceId, id);

        ListingDetailResponse listing = listingApplicationService.getListingDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Listing retrieved successfully", listing));
    }

    @GetMapping("/{id}/price-history")
    @Operation(summary = "Get listing price history", description = "Retrieves the price history for a listing including all price changes "
            + "with calculated differences and percentages")
    public ResponseEntity<ApiResponse<PriceHistoryResponse>> getPriceHistory(@PathVariable UUID id) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching price history - traceId: {}, listingId: {}", traceId, id);

        PriceHistoryResponse priceHistory = listingApplicationService.getPriceHistory(id);
        return ResponseEntity.ok(ApiResponse.success("Price history retrieved successfully", priceHistory));
    }

    @GetMapping("/{id}/similar")
    @Operation(summary = "Get similar listings by ID", description = "Retrieves listings similar to the given listing "
            + "based on property type, price range, area, and common attributes. "
            + "Results are sorted by similarity score (descending) "
            + "and published date (descending).")
    public ResponseEntity<ApiResponse<SimilarListingsResponse>> getSimilarListings(
            @PathVariable UUID id,
            @Parameter(description = "Maximum number of results to return (default: 5, max: 10)") @RequestParam(defaultValue = "5") @Min(1) @Max(10) int limit) {

        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching similar listings - traceId: {}, listingId: {}, limit: {}", traceId, id, limit);

        SimilarListingsResponse similarListings = listingApplicationService.getSimilarListings(id, limit);
        return ResponseEntity.ok(ApiResponse.success("Similar listings retrieved successfully", similarListings));
    }
}
