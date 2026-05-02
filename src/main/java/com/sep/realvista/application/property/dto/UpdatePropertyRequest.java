package com.sep.realvista.application.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.domain.common.value.PriceRangeVO;
import jakarta.validation.Valid;
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
public class UpdatePropertyRequest {
    
    @JsonProperty("location_id")
    private UUID locationId;
    
    @JsonProperty("property_type_id")
    private String propertyTypeCode;
    
    @JsonProperty("street_address")
    private String streetAddress;
    
    private BigDecimal latitude;
    
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

    @JsonProperty("status")
    private String status;

    @JsonProperty("price_range")
    private PriceRangeVO priceRange;

    @JsonProperty("allow_rent_listing_when_rented")
    private Boolean allowRentListingWhenRented;
    
    /**
     * Admin-only: reassign the property to a different owner.
     * Ignored for non-admin update calls.
     */
    @JsonProperty("new_owner_id")
    private UUID newOwnerId;
}
