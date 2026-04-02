package com.sep.realvista.application.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePropertyRequest {
    
    @JsonProperty("owner_id")
    private UUID ownerId;

    @NotNull(message = "Location ID is required")
    @JsonProperty("location_id")
    private UUID locationId;
    
    @NotBlank(message = "Property type code is required")
    @JsonProperty("property_type_id")
    private String propertyTypeCode;
    
    @NotBlank(message = "Street address is required")
    @JsonProperty("street_address")
    private String streetAddress;
    
    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;
    
    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;
    
    @JsonProperty("land_size_m2")
    private BigDecimal landSizeM2;
    
    @JsonProperty("usable_size_m2")
    private BigDecimal usableSizeM2;
    
    @JsonProperty("width_m")
    private BigDecimal widthM;
    
    @JsonProperty("length_m")
    private BigDecimal lengthM;
    
    private String descriptions;
    
    @JsonProperty("extra_attributes")
    private Map<String, Object> extraAttributes;
    
    @JsonProperty("amenity_ids")
    private List<UUID> amenityIds;

    @JsonProperty("attributes")
    private List<PropertyAttributeRequest> attributes;
    
    @Valid
    private List<PropertyMediaRequest> media;
}
