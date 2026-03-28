package com.sep.realvista.application.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for ingesting user behavior events from PostHog.
 * Sent by the frontend via the backend to the AI microservice.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorRequest {

    @NotBlank(message = "userId is required")
    @JsonProperty("user_id")
    private String userId;

    @NotEmpty(message = "At least one behavior event is required")
    @Valid
    private List<BehaviorEvent> events;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BehaviorEvent {

        @NotBlank(message = "eventType is required")
        @JsonProperty("event_type")
        private String eventType;

        @NotBlank(message = "listingId is required")
        @JsonProperty("listing_id")
        private String listingId;

        @JsonProperty("duration_seconds")
        private Integer durationSeconds;

        private Map<String, Object> metadata;
    }
}
