package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PropertyTypeInfoDTO {
    private UUID propertyTypeId;
    private String propertyTypeName;
    private String propertyTypeCode;
    private UUID propertyCategoryId;
    private String propertyCategoryName;
    private String propertyCategoryCode;
}
