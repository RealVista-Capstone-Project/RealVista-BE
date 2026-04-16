package com.sep.realvista.presentation.rest.recommendation;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.recommendation.dto.RecommendationResponse;
import com.sep.realvista.application.recommendation.dto.UserBehaviorRequest;
import com.sep.realvista.application.recommendation.service.RecommendationApplicationService;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.presentation.common.util.ControllerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for AI-powered listing recommendations.
 * Flow:
 * FE (PostHog events) → POST /api/v1/recommendations/behavior → BE → AI service → Qdrant
 * FE → GET /api/v1/recommendations → BE (cache check / threshold) → AI service → PostgreSQL → FE
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "AI-powered personalized listing recommendations")
public class RecommendationController {

    private final RecommendationApplicationService recommendationService;
    private final ControllerUtils controllerUtils;

    /**
     * Ingest user behavior events from PostHog.
     * Called by the frontend after batching user interactions.
     * Requires authentication so we know which user the events belong to.
     */
    @PostMapping("/behavior")
    @Operation(
            summary = "Ingest user behavior events",
            description = "Receives user behavior data (views, clicks, bookmarks) from PostHog "
                    + "and forwards them to the AI microservice for vector storage in Qdrant."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            value = """
                                    {
                                      "user_id": "will_be_overridden",
                                      "events": [
                                        {
                                          "event_type": "view",
                                          "listing_id": "LST-1001",
                                          "duration_seconds": 120,
                                          "metadata": {
                                            "source": "homepage",
                                            "scroll_depth_percent": 80
                                          }
                                        },
                                        {
                                          "event_type": "click",
                                          "listing_id": "LST-1002",
                                          "duration_seconds": 10,
                                          "metadata": {
                                            "source": "search_results",
                                            "position": 3
                                          }
                                        },
                                        {
                                          "event_type": "bookmark",
                                          "listing_id": "LST-1003",
                                          "duration_seconds": null,
                                          "metadata": null
                                        }
                                      ]
                                    }"""
                    )
            )
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> ingestBehavior(
            @Valid @RequestBody UserBehaviorRequest request,
            Authentication authentication) {

        String userId = extractUserId(authentication);
        String userName = extractUserName(authentication);
        String userRoles = extractUserRoles(authentication);

        // Override the userId in the request with the authenticated user's ID
        request.setUserId(userId);

        boolean success = recommendationService.ingestBehavior(
                request, userId, userName, userRoles);

        if (success) {
            int eventCount = recommendationService.getEventCount(userId);
            boolean thresholdMet = recommendationService.isThresholdMet(userId);

            Map<String, Object> data = Map.of(
                    "ingested", true,
                    "event_count", eventCount,
                    "threshold_met", thresholdMet
            );

            return ResponseEntity.ok(ApiResponse.success(
                    "Behavior events ingested successfully", data));
        }

        return ResponseEntity.internalServerError().body(
                ApiResponse.error("Failed to ingest behavior events"));
    }

    /**
     * Get personalized recommendations for the authenticated user.
     * If the metrics threshold has been reached since the last recommendation
     * refresh, fresh recommendations are generated from the AI service.
     * Otherwise, cached recommendations are returned for fast response.
     */
    @GetMapping
    @Operation(
            summary = "Get personalized recommendations",
            description = "Returns AI-powered listing recommendations. "
                    + "Uses cached results when below the metrics threshold, "
                    + "or generates fresh recommendations when the threshold is met."
    )
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendations(
            @RequestParam(required = false) Integer limit,
            @RequestParam(value = "listingType", required = false) String listingTypeParam,
            @RequestParam(value = "listing_type", required = false) String listingTypeSnake,
            Authentication authentication) {

        String userId = extractUserId(authentication);
        String userName = extractUserName(authentication);
        String userRoles = extractUserRoles(authentication);
        ListingType listingType = parseListingTypeQuery(listingTypeParam, listingTypeSnake);
        log.debug("GET /recommendations userId={} listingType={}", userId, listingType);

        RecommendationResponse response;

        // Check if we should refresh (threshold met) or use cache
        if (recommendationService.isThresholdMet(userId)) {
            log.info("Threshold met for user {} — refreshing recommendations", userId);
            response = recommendationService.refreshRecommendations(
                    userId, limit, userName, userRoles, listingType);
        } else {
            response = recommendationService.getRecommendations(
                    userId, limit, userName, userRoles, listingType);
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Recommendations retrieved successfully", response));
    }

    /**
     * Force-refresh recommendations regardless of threshold.
     * Useful for debugging or when the user explicitly requests new recommendations.
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Force-refresh recommendations",
            description = "Bypasses the threshold check and generates fresh recommendations "
                    + "from the AI service. Evicts the cache for this user."
    )
    public ResponseEntity<ApiResponse<RecommendationResponse>> refreshRecommendations(
            @RequestParam(required = false) Integer limit,
            @RequestParam(value = "listingType", required = false) String listingTypeParam,
            @RequestParam(value = "listing_type", required = false) String listingTypeSnake,
            Authentication authentication) {

        String userId = extractUserId(authentication);
        String userName = extractUserName(authentication);
        String userRoles = extractUserRoles(authentication);
        ListingType listingType = parseListingTypeQuery(listingTypeParam, listingTypeSnake);

        RecommendationResponse response = recommendationService.refreshRecommendations(
                userId, limit, userName, userRoles, listingType);

        return ResponseEntity.ok(ApiResponse.success(
                "Recommendations refreshed successfully", response));
    }

    /**
     * Get the current threshold status for the authenticated user.
     * Useful for the FE to decide whether to show a "refresh" button.
     */
    @GetMapping("/status")
    @Operation(
            summary = "Get recommendation status",
            description = "Returns whether the metrics threshold has been reached "
                    + "and the current event count for the authenticated user."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus(
            Authentication authentication) {

        String userId = extractUserId(authentication);
        int eventCount = recommendationService.getEventCount(userId);
        boolean thresholdMet = recommendationService.isThresholdMet(userId);

        Map<String, Object> data = Map.of(
                "user_id", userId,
                "event_count", eventCount,
                "threshold_met", thresholdMet
        );

        return ResponseEntity.ok(ApiResponse.success("Status retrieved", data));
    }

    // ─── Helpers ─────────────────────────────────────────────────

    /**
     * Accept {@code listingType} or {@code listing_type} (snake_case clients).
     */
    private static ListingType parseListingTypeQuery(String listingTypeParam, String listingTypeSnake) {
        String raw = listingTypeParam != null && !listingTypeParam.isBlank()
                ? listingTypeParam.trim()
                : (listingTypeSnake != null && !listingTypeSnake.isBlank() ? listingTypeSnake.trim() : null);
        if (raw == null) {
            return null;
        }
        try {
            return ListingType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String extractUserId(Authentication authentication) {
        try {
            return controllerUtils.getCurrentUser(authentication).getUserId().toString();
        } catch (Exception e) {
            return authentication != null ? authentication.getName() : "anonymous";
        }
    }

    private String extractUserName(Authentication authentication) {
        try {
            return controllerUtils.getCurrentUser(authentication).getFullName();
        } catch (Exception e) {
            return authentication != null ? authentication.getName() : "unknown";
        }
    }

    private String extractUserRoles(Authentication authentication) {
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
        }
        return "USER";
    }
}
