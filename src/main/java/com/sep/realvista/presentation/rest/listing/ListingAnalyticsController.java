package com.sep.realvista.presentation.rest.listing;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.listing.dto.ListingAnalyticsDTO;
import com.sep.realvista.application.listing.service.ListingAnalyticsService;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST Controller for listing analytics operations.
 */
@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
@Tag(name = "Listing Analytics", description = "Endpoints for listing performance metrics")
@Slf4j
public class ListingAnalyticsController {

    private final ListingAnalyticsService listingAnalyticsService;
    private final ListingRepository listingRepository;
    private final PropertyRepository propertyRepository;

    @GetMapping("/{listingId}/analytics")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get listing analytics", description = "Retrieves aggregated analytics metrics for a listing including views, unique viewers, "
            + "tour bookings, and conversion rate. The listing creator or the property owner may access.")
    public ResponseEntity<ApiResponse<ListingAnalyticsDTO>> getListingAnalytics(
            @PathVariable UUID listingId,
            @AuthenticationPrincipal SecurityUserDetails userDetails) {

        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        try {
            log.info("Fetching analytics for listing ID: {} - traceId: {}", listingId, traceId);

            // Verify listing exists
            Listing listing = listingRepository.findById(listingId)
                    .orElseThrow(() -> {
                        log.error("Listing not found with ID: {} - traceId: {}", listingId, traceId);
                        return new ResourceNotFoundException("Listing", listingId);
                    });

            if (!canViewListingAnalytics(listing, userDetails.getUserId())) {
                log.warn("Unauthorized access attempt to analytics for listing ID: {} by user: {} - traceId: {}",
                        listingId, userDetails.getUserId(), traceId);
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You do not have permission to view these analytics"));
            }

            // Get analytics
            ListingAnalyticsDTO analytics = listingAnalyticsService.getListingAnalytics(listingId);

            log.info("Successfully retrieved analytics for listing ID: {} - traceId: {}", listingId, traceId);
            return ResponseEntity.ok(ApiResponse.success("Analytics retrieved successfully", analytics));

        } finally {
            MDC.remove("traceId");
        }
    }

    /**
     * Listing creator or property owner may view analytics (same rule as listing
     * modification elsewhere).
     */
    private boolean canViewListingAnalytics(Listing listing, UUID userId) {
        if (listing.getUserId().equals(userId)) {
            return true;
        }
        return propertyRepository.findById(listing.getPropertyId())
                .map(Property::getOwnerId)
                .filter(ownerId -> ownerId.equals(userId))
                .isPresent();
    }
}
