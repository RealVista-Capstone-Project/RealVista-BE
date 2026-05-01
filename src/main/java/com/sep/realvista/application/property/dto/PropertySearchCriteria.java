package com.sep.realvista.application.property.dto;

import com.sep.realvista.domain.property.PropertyStatus;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
}
