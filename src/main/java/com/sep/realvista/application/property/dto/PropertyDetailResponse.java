package com.sep.realvista.application.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.application.listing.dto.AmenityDTO;
import com.sep.realvista.application.listing.dto.MediaDTO;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.domain.property.PropertyStatus;
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
public class PropertyDetailResponse {
    @JsonProperty("property_id")
    private UUID propertyId;
    
    @JsonProperty("owner_id")
    private UUID ownerId;
    
    @JsonProperty("location_id")
    private UUID locationId;
    
    @JsonProperty("district_id")
    private UUID districtId;

    @JsonProperty("city_id")
    private UUID cityId;
    
    @JsonProperty("property_type_id")
    private UUID propertyTypeId;

    @JsonProperty("property_type_code")
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
    
    private PropertyStatus status;
    private String descriptions;
    private String slug;
    
    @JsonProperty("extra_attributes")
    private Map<String, Object> extraAttributes;
    
    @JsonProperty("attributes")
    private List<PropertyAttributeDTO> attributes;
    
    @JsonProperty("amenities")
    private List<AmenityDTO> amenities;
    
    @JsonProperty("media")
    private List<MediaDTO> media;
}
