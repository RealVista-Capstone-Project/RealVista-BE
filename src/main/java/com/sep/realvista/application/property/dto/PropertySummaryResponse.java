package com.sep.realvista.application.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.domain.property.PropertyStatus;
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
public class PropertySummaryResponse {
    @JsonProperty("property_id")
    private UUID propertyId;
    
    @JsonProperty("property_type_id")
    private UUID propertyTypeId;
    
    @JsonProperty("street_address")
    private String streetAddress;
    
    @JsonProperty("land_size_m2")
    private BigDecimal landSizeM2;
    
    @JsonProperty("status")
    private PropertyStatus status;
    
    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    @JsonProperty("owner_name")
    private String ownerName;

    @JsonProperty("owner_phone")
    private String ownerPhone;

    @JsonProperty("has_3d")
    private Boolean has3d;
}
