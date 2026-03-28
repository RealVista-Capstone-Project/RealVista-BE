package com.sep.realvista.application.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

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
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class RecommendedListingDTO extends ListingSearchResponse {
        /** AI-generated reason for this recommendation */
        private String reason;

        /** Relevance score 0.0 - 1.0 */
        private Double score;
    }
}
