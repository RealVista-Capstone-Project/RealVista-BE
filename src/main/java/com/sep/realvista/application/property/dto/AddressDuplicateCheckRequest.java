package com.sep.realvista.application.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDuplicateCheckRequest {

    @NotNull(message = "Location ID is required")
    @JsonProperty("location_id")
    private UUID locationId;

    @NotBlank(message = "Street address is required")
    @JsonProperty("street_address")
    private String streetAddress;

    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;

    /** When editing an existing property, pass its ID to exclude it from the duplicate search. */
    @JsonProperty("exclude_property_id")
    private UUID excludePropertyId;
}
