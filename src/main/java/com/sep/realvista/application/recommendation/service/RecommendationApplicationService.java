package com.sep.realvista.application.recommendation.service;

import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.application.recommendation.dto.AiRecommendationResult;
import com.sep.realvista.application.recommendation.dto.RecommendationResponse;
import com.sep.realvista.application.recommendation.dto.UserBehaviorRequest;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.infrastructure.external.ai.AiServiceClient;
import com.sep.realvista.infrastructure.persistence.property.attribute.PropertyAttributeValueJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationApplicationService {

    private final AiServiceClient aiServiceClient;
    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;
    private final PropertyAttributeValueJpaRepository propertyAttributeValueRepository;

    /**
     * In-memory event counters per user since last recommendation refresh.
     * In production, replace with Redis or a persistent counter.
     */
    private final ConcurrentHashMap<String, AtomicInteger> userEventCounters =
            new ConcurrentHashMap<>();

    /**
     * Minimum number of new behavior events required before calling the AI
     * service for fresh recommendations. Below this threshold, cached results
     * are returned instead.
     */
    @Value("${realvista.recommendation.threshold:5}")
    private int metricsThreshold;

    @Value("${realvista.recommendation.default-limit:10}")
    private int defaultLimit;

    // ─── Public API ──────────────────────────────────────────────

    /**
     * Ingest user behavior events from PostHog (forwarded by the FE).
     * Events are always forwarded to the AI service for vector storage.
     * The local counter tracks how many events have accumulated since
     * the last recommendation refresh.
     *
     * @return true if ingestion was successful
     */
    public boolean ingestBehavior(UserBehaviorRequest request,
                                  String userId,
                                  String userName,
                                  String userRoles) {
        log.info("Ingesting {} behavior events for user {}",
                request.getEvents().size(), userId);

        // Always forward to AI service for vector storage
        boolean success = aiServiceClient.ingestBehavior(
                request, userId, userName, userRoles);

        if (success) {
            // Increment the local counter
            userEventCounters
                    .computeIfAbsent(userId, k -> new AtomicInteger(0))
                    .addAndGet(request.getEvents().size());

            int currentCount = userEventCounters.get(userId).get();
            log.debug("User {} event counter: {}/{} (threshold)",
                    userId, currentCount, metricsThreshold);
        }

        return success;
    }

    /**
     * Get personalized recommendations for a user.
     * Decision logic:
     * - If the user's accumulated events >= threshold → call AI service
     * for fresh recommendations, reset counter, evict cache, cache new results.
     * - If below threshold → return cached recommendations (fast path).
     * - If no cache exists and below threshold → still call AI service
     * (cold-start scenario).
     * After receiving listing IDs from the AI service, enrich them with
     * full listing data from PostgreSQL.
     */
    @Cacheable(value = "recommendations", key = "#userId")
    public RecommendationResponse getRecommendations(String userId,
                                                     Integer limit,
                                                     String userName,
                                                     String userRoles) {
        int effectiveLimit = (limit != null && limit > 0) ? limit : defaultLimit;
        log.info("Generating recommendations for user {} (limit={})", userId, effectiveLimit);

        // This method body only runs on cache MISS.
        // On cache HIT, Spring returns the cached value directly.
        return fetchAndEnrichRecommendations(userId, effectiveLimit, userName, userRoles);
    }

    /**
     * Force-refresh recommendations regardless of threshold.
     * Called when the threshold is met or when explicitly requested.
     */
    @CacheEvict(value = "recommendations", key = "#userId")
    public RecommendationResponse refreshRecommendations(String userId,
                                                         Integer limit,
                                                         String userName,
                                                         String userRoles) {
        int effectiveLimit = (limit != null && limit > 0) ? limit : defaultLimit;
        log.info("Force-refreshing recommendations for user {}", userId);

        // Reset the event counter
        userEventCounters.remove(userId);

        return fetchAndEnrichRecommendations(userId, effectiveLimit, userName, userRoles);
    }

    /**
     * Check if the user has accumulated enough events to warrant
     * a fresh AI recommendation call.
     */
    public boolean isThresholdMet(String userId) {
        AtomicInteger counter = userEventCounters.get(userId);
        return counter != null && counter.get() >= metricsThreshold;
    }

    /**
     * Get the current event count for a user (for debugging / FE display).
     */
    public int getEventCount(String userId) {
        AtomicInteger counter = userEventCounters.get(userId);
        return counter != null ? counter.get() : 0;
    }

    // ─── Internal ────────────────────────────────────────────────

    private RecommendationResponse fetchAndEnrichRecommendations(
            String userId, int limit, String userName, String userRoles) {

        // 1. Call AI service
        AiRecommendationResult aiResult = aiServiceClient.getRecommendations(
                userId, limit, userName, userRoles);

        if (aiResult == null || aiResult.getRecommendations() == null
                || aiResult.getRecommendations().isEmpty()) {
            log.warn("AI service returned no recommendations for user {}", userId);
            return RecommendationResponse.builder()
                    .userId(userId)
                    .recommendations(Collections.emptyList())
                    .generatedAt(java.time.Instant.now().toString())
                    .behaviorSummary("No behavior data available")
                    .fromCache(false)
                    .build();
        }

        // 2. Extract listing IDs from AI results
        List<UUID> listingIds = aiResult.getRecommendations().stream()
                .map(r -> {
                    try {
                        return UUID.fromString(r.getListingId());
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid listing ID from AI service: {}", r.getListingId());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        // 3. Batch-fetch listings from PostgreSQL
        Map<UUID, Listing> listingMap = new HashMap<>();
        for (UUID id : listingIds) {
            listingRepository.findById(id).ifPresent(listing -> listingMap.put(id, listing));
        }

        // 4. Build enriched response
        List<RecommendationResponse.RecommendedListingDTO> enrichedListings =
                aiResult.getRecommendations().stream()
                        .map(aiRec -> {
                            UUID lid;
                            try {
                                lid = UUID.fromString(aiRec.getListingId());
                            } catch (IllegalArgumentException e) {
                                return null;
                            }

                            Listing listing = listingMap.get(lid);
                            if (listing == null) {
                                return null;
                            }

                            // Start with base search response mapping
                            com.sep.realvista.application.listing.dto.ListingSearchResponse searchRes =
                                    listingMapper.toSearchResponse(listing);

                            // Build the final recommended DTO
                            RecommendationResponse.RecommendedListingDTO.RecommendedListingDTOBuilder<?, ?> builder =
                                    RecommendationResponse.RecommendedListingDTO.builder()
                                            .listingId(searchRes.getListingId())
                                            .name(searchRes.getName())
                                            .slug(searchRes.getSlug())
                                            .listingType(searchRes.getListingType())
                                            .status(searchRes.getStatus())
                                            .price(searchRes.getPrice())
                                            .area(searchRes.getArea())
                                            .publishedAt(searchRes.getPublishedAt())
                                            .userType(searchRes.getUserType())
                                            .reason(aiRec.getReason())
                                            .score(aiRec.getScore());

                            // Populate address fields
                            if (listing.getProperty() != null) {
                                builder.streetAddress(listing.getProperty().getStreetAddress());
                                Location loc = listing.getProperty().getLocation();
                                while (loc != null) {
                                    switch (loc.getType()) {
                                        case CITY -> builder.cityName(loc.getName());
                                        case DISTRICT -> builder.districtName(loc.getName());
                                        case WARD -> builder.wardName(loc.getName());
                                        default -> {
                                        }
                                    }
                                    loc = loc.getParent();
                                }
                            }

                            // Populate thumbnail
                            var thumbnail = listingRepository.findThumbnailByListingId(lid);
                            builder.thumbnail(thumbnail.orElse(null));

                            // Populate attributes
                            List<PropertyAttributeValue> attrs = propertyAttributeValueRepository
                                    .findByPropertyIdWithAttribute(listing.getPropertyId());
                            builder.attributes(listingMapper.toAttributeList(attrs));

                            return (RecommendationResponse.RecommendedListingDTO) builder.build();
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        return RecommendationResponse.builder()
                .userId(userId)
                .recommendations(enrichedListings)
                .generatedAt(aiResult.getGeneratedAt())
                .behaviorSummary(aiResult.getBehaviorSummary())
                .fromCache(false)
                .build();
    }
}
