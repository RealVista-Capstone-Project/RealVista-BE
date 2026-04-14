package com.sep.realvista.infrastructure.external.ai;

import com.sep.realvista.application.recommendation.dto.AiRecommendationResult;
import com.sep.realvista.application.recommendation.dto.UserBehaviorRequest;
import com.sep.realvista.domain.listing.ListingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP client that communicates with the RealVista AI microservice (NestJS).
 * Uses the internal x-api-key authentication scheme.
 * Endpoints called:
 * POST /recommendation/ingest   — store user behavior in Qdrant
 * POST /recommendation/generate — get AI-powered listing recommendations
 */
@Slf4j
@Service
public class AiServiceClient {

    private final RestTemplate restTemplate;
    private final String aiServiceBaseUrl;
    private final String serviceApiKey;

    public AiServiceClient(
            RestTemplate restTemplate,
            @Value("${realvista.ai.service-url:http://localhost:3001}") String aiServiceBaseUrl,
            @Value("${realvista.ai.api-key:}") String serviceApiKey) {
        this.restTemplate = restTemplate;
        this.aiServiceBaseUrl = aiServiceBaseUrl;
        this.serviceApiKey = serviceApiKey;
    }

    /**
     * Forward user behavior events to the AI microservice for vector storage.
     *
     * @param request   the behavior events from PostHog
     * @param userId    authenticated user ID (forwarded as header)
     * @param userName  authenticated user name
     * @param userRoles comma-separated roles
     * @return true if ingestion was successful
     */
    public boolean ingestBehavior(UserBehaviorRequest request,
                                  String userId,
                                  String userName,
                                  String userRoles) {
        String url = aiServiceBaseUrl + "/recommendation/ingest";

        try {
            HttpHeaders headers = buildHeaders(userId, userName, userRoles);
            HttpEntity<UserBehaviorRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully ingested {} behavior events for user {}",
                        request.getEvents().size(), userId);
                return true;
            }

            log.warn("AI service returned non-2xx for ingest: {}", response.getStatusCode());
            return false;

        } catch (RestClientException e) {
            log.error("Failed to call AI service ingest endpoint: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Request AI-powered recommendations from the AI microservice.
     *
     * @param userId    the user to get recommendations for
     * @param limit     max number of recommendations
     * @param userName  authenticated user name
     * @param userRoles comma-separated roles
     * @return the AI recommendation result, or null on failure
     */
    public AiRecommendationResult getRecommendations(String userId,
                                                     int limit,
                                                     String userName,
                                                     String userRoles,
                                                     ListingType listingType,
                                                     java.util.List<?> preferences,
                                                     String profileName) {
        String url = aiServiceBaseUrl + "/recommendation/generate";

        try {
            HttpHeaders headers = buildHeaders(userId, userName, userRoles);

            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId);
            body.put("limit", limit);
            if (listingType != null) {
                body.put("listingType", listingType.name());
            }
            if (preferences != null) {
                body.put("preferences", preferences);
            }
            if (profileName != null) {
                body.put("profileName", profileName);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<AiRecommendationResult> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, AiRecommendationResult.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Received {} recommendations from AI service for user {}",
                        response.getBody().getRecommendations().size(), userId);
                return response.getBody();
            }

            log.warn("AI service returned non-2xx for recommendations: {}",
                    response.getStatusCode());
            return null;

        } catch (RestClientException e) {
            log.error("Failed to call AI service recommendation endpoint: {}", e.getMessage());
            return null;
        }
    }

    // ─── Internal ────────────────────────────────────────────────

    private HttpHeaders buildHeaders(String userId, String userName, String userRoles) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", serviceApiKey);
        headers.set("x-user-id", userId);
        headers.set("x-user-name", userName != null ? userName : "unknown");
        headers.set("x-user-roles", userRoles != null ? userRoles : "USER");
        return headers;
    }
}
