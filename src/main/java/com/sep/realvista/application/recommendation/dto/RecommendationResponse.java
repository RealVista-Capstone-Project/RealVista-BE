package com.sep.realvista.application.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO returned to the frontend containing AI-recommended listings
 * with full listing details fetched from PostgreSQL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {

    @JsonProperty("user_id")
    private String userId;

    private List<RecommendedListingDTO> recommendations;

    @JsonProperty("generated_at")
    private String generatedAt;

    @JsonProperty("behavior_summary")
    private String behaviorSummary;

    @JsonProperty("from_cache")
    private boolean fromCache;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedListingDTO {

        @JsonProperty("listing_id")
        private String listingId;

        /** AI-generated reason for this recommendation */
        private String reason;

        /** Relevance score 0.0 - 1.0 */
        private double score;

        /** Listing name from PostgreSQL */
        private String name;

        /** Listing slug from PostgreSQL */
        private String slug;

        /** Listing type (SALE / RENT) */
        @JsonProperty("listing_type")
        private String listingType;

        /** Listing price from PostgreSQL */
        private Long price;

        /** Thumbnail URL */
        private String thumbnail;

        /** Location name */
        private String location;
    }
}
