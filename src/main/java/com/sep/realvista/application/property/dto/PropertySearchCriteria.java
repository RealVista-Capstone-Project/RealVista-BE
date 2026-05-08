package com.sep.realvista.application.property.dto;

import com.sep.realvista.domain.property.PropertyStatus;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertySearchCriteria {
    @Parameter(description = "Search query for property street address or description")
    private String keyword;

    @Parameter(description = "Filter properties by current status")
    private PropertyStatus status;

    @Parameter(description = "Filter properties by multiple statuses. Takes priority over status when present.")
    private List<PropertyStatus> statuses;

    @Parameter(description = "Filter properties by owner or agent user ID (Admin only)")
    private UUID userId;

    @Parameter(description = "Filter properties by property type ID")
    private UUID propertyTypeId;

    @Parameter(description = "Filter properties by city, district, ward, or exact location ID")
    private UUID locationId;

    @Parameter(description = "Sort order: NEWEST, OLDEST, AREA_ASC, AREA_DESC, ADDRESS_ASC, ADDRESS_DESC")
    private String sortBy;
}
