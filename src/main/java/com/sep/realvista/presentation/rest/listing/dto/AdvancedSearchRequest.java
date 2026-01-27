package com.sep.realvista.presentation.rest.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Advanced search criteria for property listings")
public class AdvancedSearchRequest {

    @Schema(description = "Type of property (e.g., apartment, villa, land)", example = "apartment")
    private String propertyType;

    @Schema(description = "Location name or district", example = "Quận 7")
    private String location;

    @Schema(description = "Price range [min, max]", example = "[5000000000, 15000000000]")
    private List<BigDecimal> price;

    @Schema(description = "Area range in m2 [min, max]", example = "[60, 120]")
    private List<BigDecimal> area;

    @Schema(description = "Dynamic attributes based on property type (e.g., bedrooms, direction)", 
            example = "{\"bedrooms\": 2, \"direction\": \"Đông Nam\"}")
    private Map<String, Object> dynamic;
}
