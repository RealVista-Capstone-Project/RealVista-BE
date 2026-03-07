package com.sep.realvista.application.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Internal DTO that mirrors the AI microservice's recommendation response.
 * Used for deserializing the JSON returned by POST /recommendation/generate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendationResult {

    @JsonProperty("userId")
    private String userId;

    private List<AiRecommendedListing> recommendations;

    @JsonProperty("generatedAt")
    private String generatedAt;

    @JsonProperty("behaviorSummary")
    private String behaviorSummary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiRecommendedListing {

        @JsonProperty("listingId")
        private String listingId;

        private String reason;

        private double score;
    }
}
