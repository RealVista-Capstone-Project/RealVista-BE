package com.sep.realvista.application.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.domain.property.DuplicateSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDuplicateCheckResponse {

    private DuplicateSeverity severity;

    /** Human-readable reason code to help the FE pick the correct modal variant. */
    @JsonProperty("reason_code")
    private String reasonCode;

    /** Brief message for display. */
    private String message;

    /** Conflicting properties (summary only, no sensitive owner details for different-owner cases). */
    @JsonProperty("conflicting_properties")
    private List<ConflictingPropertySummary> conflictingProperties;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictingPropertySummary {

        @JsonProperty("property_id")
        private String propertyId;

        @JsonProperty("street_address")
        private String streetAddress;

        private String status;

        @JsonProperty("is_same_owner")
        private Boolean isSameOwner;

        /** Only populated when isSameOwner = true. */
        @JsonProperty("thumbnail_url")
        private String thumbnailUrl;
    }
}
