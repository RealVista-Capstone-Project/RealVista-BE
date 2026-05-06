package com.sep.realvista.presentation.rest.listing;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.listing.dto.CreateListingRequest;
import com.sep.realvista.application.listing.dto.ListingCompareDataResponse;
import com.sep.realvista.application.listing.dto.ListingDetailResponse;
import com.sep.realvista.application.listing.dto.ListingResponse;
import com.sep.realvista.application.listing.dto.ListingSearchCriteria;
import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.application.listing.dto.ManagedListingSearchCriteria;
import com.sep.realvista.application.listing.dto.ManagedListingSummaryDTO;
import com.sep.realvista.application.listing.dto.PriceHistoryResponse;
import com.sep.realvista.application.listing.dto.RelatedListingsResponse;
import com.sep.realvista.application.listing.dto.SimilarListingsResponse;
import com.sep.realvista.application.listing.dto.UpdateListingRequest;
import com.sep.realvista.application.listing.service.ListingApplicationService;
import com.sep.realvista.application.listing.service.ListingSearchService;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

        @Operation(summary = "Search Listings",
                        description = "Search for published listings using various filter criteria.",
                        responses = {
                                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                                        responseCode = "200",
                                                        description = "Successfully retrieved matching listings",
                                                        content = @io.swagger.v3.oas.annotations.media.Content(
                                                                mediaType = "application/json",
                                                                schema = @io.swagger.v3.oas.annotations.media.Schema(
                                                                        implementation = PageResponse.class))),
                                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                                        responseCode = "400",
                                                        description = "Invalid search criteria provided",
                                                        content = @io.swagger.v3.oas.annotations.media.Content)
                        })
        @GetMapping("/search")
        public ResponseEntity<ApiResponse<PageResponse<ListingSearchResponse>>> search(
                        @org.springdoc.core.annotations.ParameterObject ListingSearchCriteria criteria,
                        @org.springframework.data.web.PageableDefault(size = 20)
                        org.springframework.data.domain.Pageable pageable,
                        @AuthenticationPrincipal SecurityUserDetails userDetails,
                        @org.springframework.web.bind.annotation.RequestParam(required = false)
                        java.util.Map<String, String> allParams) {

                // Extract attr_* params as dynamic attributes (e.g. attr_bathrooms=4:)
                if (allParams != null) {
                        java.util.Map<String, String> dynAttrs = new java.util.LinkedHashMap<>();
                        allParams.forEach((key, value) -> {
                                if (key.startsWith("attr_") && value != null && !value.isBlank()) {
                                        dynAttrs.put(key.substring(5).toUpperCase(), value);
                                }
                        });
                        if (!dynAttrs.isEmpty()) {
                                criteria.setDynamicAttributes(dynAttrs);
                        }
                }

                log.info("Searching listings with criteria: {}", criteria);

                UUID userId = userDetails != null ? userDetails.getUserId() : null;
                org.springframework.data.domain.Page<ListingSearchResponse> results = listingSearchService.search(
                                criteria,
                                pageable, userId);

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

        @GetMapping("/{idOrSlug}")
        @Operation(summary = "Get listing detail by ID or slug",
                        description = "Retrieves complete listing information including media, property, "
                        + "location, type, category, and agent/owner. "
                        + "Accepts either UUID or SEO-friendly slug format. "
                        + "Slug format: {slugified-name}-{short-uuid} "
                        + "Example: luxury-2-bedroom-apartment-2qLn4Z8XooP")
        public ResponseEntity<ApiResponse<ListingDetailResponse>> getListingDetail(
                        @PathVariable String idOrSlug,
                        @RequestParam(defaultValue = "false") boolean recordView,
                        @RequestParam(defaultValue = "false") boolean editing,
                        @AuthenticationPrincipal SecurityUserDetails userDetails) {
                String traceId = UUID.randomUUID().toString();
                MDC.put("traceId", traceId);

                try {
                        log.info("Fetching listing detail - traceId: {}, idOrSlug: {}, recordView: {}", traceId,
                                        idOrSlug, recordView);

                        UUID userId = userDetails != null ? userDetails.getUserId() : null;
                        boolean isAdmin = userDetails != null && userDetails.getAuthorities().stream()
                                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                        
                        ListingDetailResponse listing;

                        // Try to parse as UUID first
                        try {
                                UUID id = UUID.fromString(idOrSlug);
                                listing = listingApplicationService.getListingDetail(id, userId, recordView, editing);
                        } catch (IllegalArgumentException e) {
                                // Not a valid UUID, treat as slug
                                log.debug("Input is not a valid UUID, treating as slug: {}", idOrSlug);
                                try {
                                        listing = listingApplicationService.getListingBySlug(idOrSlug, userId,
                                                        recordView, editing);
                                } catch (Exception ex) {
                                        log.error("Failed to get listing by slug: {}", idOrSlug, ex);
                                        throw ex;
                                }
                        }
                        
                        // Enforce access control for BANNED listings
                        if (listing.getStatus() == com.sep.realvista.domain.listing.ListingStatus.BANNED
                                        && !isAdmin) {
                                log.warn("Access denied to BANNED listing {} for user {}", idOrSlug, userId);
                                throw new com.sep.realvista.domain.common.exception.ResourceNotFoundException(
                                                "Listing", idOrSlug);
                        }

                        return ResponseEntity.ok(ApiResponse.success("Listing retrieved successfully", listing));
                } finally {
                        MDC.remove("traceId");
                }
        }

        @GetMapping("/{idOrSlug}/price-history")
        @Operation(summary = "Get listing price history",
                        description = "Retrieves the price history for a listing including all price changes "
                        + "with calculated differences and percentages. Accepts either UUID or slug.")
        public ResponseEntity<ApiResponse<PriceHistoryResponse>> getPriceHistory(@PathVariable String idOrSlug) {
                String traceId = UUID.randomUUID().toString();
                MDC.put("traceId", traceId);

                try {
                        log.info("Fetching price history - traceId: {}, idOrSlug: {}", traceId, idOrSlug);

                        UUID id = parseIdOrSlug(idOrSlug);
                        PriceHistoryResponse priceHistory = listingApplicationService.getPriceHistory(id);
                        return ResponseEntity
                                        .ok(ApiResponse.success("Price history retrieved successfully", priceHistory));
                } finally {
                        MDC.remove("traceId");
                }
        }

        @GetMapping("/{idOrSlug}/similar")
        @Operation(summary = "Get similar listings",
                        description = "Retrieves listings similar to the given listing "
                                        + "based on property type, price range, area, and common attributes. "
                                        + "Results are sorted by similarity score (descending) "
                                        + "and published date (descending). Accepts either UUID or slug.")
        public ResponseEntity<ApiResponse<SimilarListingsResponse>> getSimilarListings(
                        @PathVariable String idOrSlug,
                        @Parameter(description = "Maximum number of results to return (default: 5, max: 10)")
                        @RequestParam(defaultValue = "5") @Min(1) @Max(10) int limit,
                        @AuthenticationPrincipal SecurityUserDetails userDetails) {

                String traceId = UUID.randomUUID().toString();
                MDC.put("traceId", traceId);

                try {
                        log.info("Fetching similar listings - traceId: {}, idOrSlug: {}, limit: {}", traceId, idOrSlug,
                                        limit);

                        UUID id = parseIdOrSlug(idOrSlug);
                        UUID userId = userDetails != null ? userDetails.getUserId() : null;
                        SimilarListingsResponse similarListings = listingApplicationService.getSimilarListings(id,
                                        limit, userId);
                        return ResponseEntity.ok(ApiResponse.success("Similar listings retrieved successfully",
                                        similarListings));
                } finally {
                        MDC.remove("traceId");
                }
        }

        @GetMapping("/property/{propertyId}/related")
        @Operation(summary = "Get related listings by property",
                        description = "Retrieves both RENT and SALE listings for the same property "
                                        + "if they exist and are published. Useful for showing rent vs buy comparison.")
        public ResponseEntity<ApiResponse<RelatedListingsResponse>> getRelatedListingsByProperty(
                        @PathVariable UUID propertyId) {

                String traceId = UUID.randomUUID().toString();
                MDC.put("traceId", traceId);

                try {
                        log.info("Fetching related listings for property - traceId: {}, propertyId: {}",
                                        traceId, propertyId);

                        RelatedListingsResponse relatedListings = listingApplicationService
                                        .getRelatedListingsByProperty(propertyId);
                        return ResponseEntity.ok(ApiResponse.success("Related listings retrieved successfully",
                                        relatedListings));
                } finally {
                        MDC.remove("traceId");
                }
        }

        /**
         * Helper method to parse idOrSlug parameter to UUID.
         * Tries to parse as UUID first, then extracts from slug if needed.
         *
         * @param idOrSlug either a UUID string or a slug
         * @return UUID extracted from the input
         * @throws IllegalArgumentException if neither UUID nor valid slug
         */
        private UUID parseIdOrSlug(String idOrSlug) {
                try {
                        // Try direct UUID parsing first
                        return UUID.fromString(idOrSlug);
                } catch (IllegalArgumentException e) {
                        // Not a UUID, extract from slug
                        return com.sep.realvista.shared.util.ShortIdUtils.extractUuidFromSlug(idOrSlug);
                }
        }

        // ==================== CRUD Operations ====================

        @PostMapping
        @SecurityRequirement(name = "Bearer Authentication")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Create a new listing", description = "Creates a new listing in DRAFT status. "
                        + "The listing will be associated with the authenticated user.")
        public ResponseEntity<ApiResponse<ListingResponse>> createListing(
                        @RequestBody @Valid CreateListingRequest request,
                        @AuthenticationPrincipal SecurityUserDetails userDetails) {

                log.info("Creating listing for user: {}", userDetails.getUserId());

                ListingResponse response = listingApplicationService.createListing(request, userDetails.getUserId());

                return ResponseEntity
                                .status(org.springframework.http.HttpStatus.CREATED)
                                .body(ApiResponse.success("Listing created successfully", response));
        }

        @PutMapping("/{listingId}")
        @SecurityRequirement(name = "Bearer Authentication")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Update a listing",
                        description = "Updates an existing listing. "
                                        + "The listing creator or property owner can update it.")
        public ResponseEntity<ApiResponse<ListingResponse>> updateListing(
                        @PathVariable UUID listingId,
                        @RequestBody @Valid UpdateListingRequest request,
                        @AuthenticationPrincipal SecurityUserDetails userDetails) {

                log.info("Updating listing ID: {} by user: {}", listingId, userDetails.getUserId());

                com.sep.realvista.application.listing.dto.ListingResponse response = listingApplicationService
                                .updateListing(listingId, request, userDetails.getUserId());

                return ResponseEntity.ok(ApiResponse.success("Listing updated successfully", response));
        }

        @DeleteMapping("/{listingId}")
        @SecurityRequirement(name = "Bearer Authentication")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Delete a listing",
                        description = "Soft deletes a listing. The listing creator or property owner can delete it.")
        public ResponseEntity<ApiResponse<Void>> deleteListing(
                        @PathVariable UUID listingId,
                        @AuthenticationPrincipal SecurityUserDetails userDetails) {

                log.info("Deleting listing ID: {} by user: {}", listingId, userDetails.getUserId());

                listingApplicationService.deleteListing(listingId, userDetails.getUserId());

                return ResponseEntity.ok(ApiResponse.success("Listing deleted successfully", null));
        }

        @GetMapping("/managed-listings")
        @SecurityRequirement(name = "Bearer Authentication")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get managed listings",
                        description = "Retrieves listings created by the authenticated user "
                                        + "or where the user owns the property. "
                        + "Supports pagination, search, and sorting.")
        public ResponseEntity<ApiResponse<PageResponse<ListingResponse>>> getManagedListings(
                        @org.springdoc.core.annotations.ParameterObject ManagedListingSearchCriteria criteria,
                        @org.springframework.data.web.PageableDefault()
                        org.springframework.data.domain.Pageable pageable,
                        @AuthenticationPrincipal SecurityUserDetails userDetails) {

                log.info("Fetching managed listings for user: {} with criteria: {}", userDetails.getUserId(), criteria);

                org.springframework.data.domain.Page<ListingResponse> results = listingApplicationService
                                .getManagedListings(userDetails.getUserId(), criteria, pageable);

                PageResponse<ListingResponse> pageResponse = PageResponse.<ListingResponse>builder()
                                .content(results.getContent())
                                .page(results.getNumber())
                                .size(results.getSize())
                                .totalElements(results.getTotalElements())
                                .totalPages(results.getTotalPages())
                                .first(results.isFirst())
                                .last(results.isLast())
                                .build();

                return ResponseEntity.ok(
                                ApiResponse.success("Listings retrieved successfully", pageResponse));
        }

        @GetMapping("/managed-listings/summary")
        @SecurityRequirement(name = "Bearer Authentication")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get managed listings summary",
                        description = "Retrieves counts of ALL, RENT, and SALE listings for the authenticated user.")
        public ResponseEntity<ApiResponse<ManagedListingSummaryDTO>> getManagedListingSummary(
                        @AuthenticationPrincipal SecurityUserDetails userDetails) {

                log.info("Fetching listing summary for user: {}", userDetails.getUserId());

                ManagedListingSummaryDTO summary = listingApplicationService
                                .getManagedListingSummary(userDetails.getUserId());

                return ResponseEntity.ok(
                                ApiResponse.success("Summary retrieved successfully", summary));
        }

        // ==================== Status Management Operations ====================

        @PatchMapping("/{listingId}/publish")
        @SecurityRequirement(name = "Bearer Authentication")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Publish a listing",
                        description = "Changes listing status from DRAFT/PENDING to PUBLISHED. "
                        + "The listing creator or property owner can publish. "
                        + "Requires the associated property to be in AVAILABLE (active) status.")
        public ResponseEntity<ApiResponse<ListingResponse>> publishListing(
                        @PathVariable UUID listingId,
                        @AuthenticationPrincipal SecurityUserDetails userDetails) {

                log.info("Publishing listing ID: {} by user: {}", listingId, userDetails.getUserId());

                ListingResponse response = listingApplicationService.publishListing(listingId, userDetails.getUserId());

                return ResponseEntity.ok(ApiResponse.success("Listing published successfully", response));
        }

        @PatchMapping("/{listingId}/unpublish")
        @SecurityRequirement(name = "Bearer Authentication")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Unpublish a listing", description = "Changes listing status from PUBLISHED to DRAFT. "
                        + "The listing creator or property owner can unpublish.")
        public ResponseEntity<ApiResponse<com.sep.realvista.application.listing.dto.ListingResponse>> unpublishListing(
                        @PathVariable UUID listingId,
                        @AuthenticationPrincipal SecurityUserDetails userDetails) {

                log.info("Unpublishing listing ID: {} by user: {}", listingId, userDetails.getUserId());

                com.sep.realvista.application.listing.dto.ListingResponse response = listingApplicationService
                                .unpublishListing(listingId, userDetails.getUserId());

                return ResponseEntity.ok(ApiResponse.success("Listing unpublished successfully", response));
        }

        @PatchMapping("/{listingId}/mark-as-sold")
        @SecurityRequirement(name = "Bearer Authentication")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Mark listing as sold", description = "Changes SALE listing status to SOLD. "
                        + "Only applicable for SALE listings. The listing creator or property owner can mark as sold.")
        public ResponseEntity<ApiResponse<com.sep.realvista.application.listing.dto.ListingResponse>> markAsSold(
                        @PathVariable UUID listingId,
                        @AuthenticationPrincipal SecurityUserDetails userDetails) {

                log.info("Marking listing ID: {} as sold by user: {}", listingId, userDetails.getUserId());

                ListingResponse response = listingApplicationService.markAsSold(listingId, userDetails.getUserId());

                return ResponseEntity.ok(ApiResponse.success("Listing marked as sold", response));
        }

        @PatchMapping("/{listingId}/mark-as-rented")
        @SecurityRequirement(name = "Bearer Authentication")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Mark listing as rented",
                        description = "Changes RENT listing status to RENTED. "
                        + "Only applicable for RENT listings. "
                        + "The listing creator or property owner can mark as rented.")
        public ResponseEntity<ApiResponse<com.sep.realvista.application.listing.dto.ListingResponse>> markAsRented(
                        @PathVariable UUID listingId,
                        @AuthenticationPrincipal SecurityUserDetails userDetails) {

                log.info("Marking listing ID: {} as rented by user: {}", listingId, userDetails.getUserId());

                com.sep.realvista.application.listing.dto.ListingResponse response = listingApplicationService
                                .markAsRented(listingId, userDetails.getUserId());

                return ResponseEntity.ok(ApiResponse.success("Listing marked as rented", response));
        }

        // ==================== Compare Operations ====================

        @GetMapping("/compare")
        @Operation(summary = "Get compare data for multiple listings",
                        description = "Retrieves detailed data for comparing multiple listings. "
                                        + "Accepts multiple listing UUIDs as query parameters. "
                                        + "Maximum 3 listings can be compared at once. "
                                        + "Returns comprehensive data including media, attributes, amenities, "
                                        + "and boost status (featured/hot) for each listing.")
        public ResponseEntity<ApiResponse<java.util.List<ListingCompareDataResponse>>> getCompareData(
                        @RequestParam(name = "ids")
                        @Parameter(description = "List of listing UUIDs to compare (max 3)")
                        java.util.List<UUID> ids) {

                String traceId = UUID.randomUUID().toString();
                MDC.put("traceId", traceId);

                try {
                        if (ids == null || ids.isEmpty()) {
                                throw new IllegalArgumentException("At least one listing ID is required");
                        }
                        if (ids.size() > 3) {
                                throw new IllegalArgumentException("Maximum 3 listings can be compared at once");
                        }

                        log.info("Fetching compare data - traceId: {}, ids: {}", traceId, ids);

                        java.util.List<ListingCompareDataResponse> compareData = listingApplicationService
                                        .getCompareData(ids);

                        return ResponseEntity.ok(ApiResponse.success("Compare data retrieved successfully",
                                        compareData));
                } finally {
                        MDC.remove("traceId");
                }
        }
}
