package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Flexible amenity DTO for listing detail response.
 * Supports dynamic amenities based on property type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmenityDTO {

    @JsonProperty("amenity_id")
    private UUID amenityId;

    @JsonProperty("amenity_name")
    private String amenityName;

    @JsonProperty("amenity_type")
    private String amenityType;

    private String description;

    @JsonProperty("is_onsite")
    public boolean isOnsite() {
        return "ONSITE".equals(amenityType);
    }

    @JsonProperty("is_offsite")
    public boolean isOffsite() {
        return "OFFSITE".equals(amenityType);
    }
}
