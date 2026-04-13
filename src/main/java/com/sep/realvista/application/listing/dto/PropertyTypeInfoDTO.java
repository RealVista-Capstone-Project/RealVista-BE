package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Property type and category nested DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyTypeInfoDTO {
    @JsonProperty("property_type_id")
    private UUID propertyTypeId;
    @JsonProperty("property_type_name")
    private String propertyTypeName;
    @JsonProperty("property_type_code")
    private String propertyTypeCode;
    @JsonProperty("property_category_id")
    private UUID propertyCategoryId;
    @JsonProperty("property_category_name")
    private String propertyCategoryName;
    @JsonProperty("property_category_code")
    private String propertyCategoryCode;
}
