package com.sep.realvista.application.property.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertySearchCriteria {
    @Parameter(description = "Search query for property street address or description")
    private String keyword;
}
