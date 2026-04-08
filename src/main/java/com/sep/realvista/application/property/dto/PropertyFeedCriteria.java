package com.sep.realvista.application.property.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyFeedCriteria {

    @Parameter(description = "Search keyword for property address or description")
    private String keyword;

    @Parameter(description = "Filter by property type ID")
    private UUID propertyTypeId;

    @Parameter(description = "Filter by location ID (ward/district/city)")
    private UUID locationId;
}
