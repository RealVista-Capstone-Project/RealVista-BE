package com.sep.realvista.application.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Advanced search criteria for property listings")
public class AdvancedSearchRequest {

    @Schema(description = "Unique identifier of the property type", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID propertyType;

    @Schema(description = "Location name or district", example = "Quận 7")
    private String location;

    @Schema(description = "Price range [min, max]", example = "[5000000000, 15000000000]")
    private List<BigDecimal> price;

    @Schema(description = "Area range in m2 [min, max]", example = "[60, 120]")
    private List<BigDecimal> area;

    @Schema(description = "Listing Type (SALE or RENT)", example = "RENT")
    private String listingType;

    @Schema(description = "Property Category (RESIDENTIAL, COMMERCIAL, LAND)", example = "RESIDENTIAL")
    private String propertyCategory;

    @Schema(description = "Number of bedrooms", example = "2")
    private Integer bedrooms;

    @Schema(description = "Number of bathrooms", example = "2")
    private Integer bathrooms;

    @Schema(description = "Property has video media", example = "true")
    private Boolean hasVideo;

    @Schema(description = "Property has 3D tour", example = "true")
    private Boolean has3D;

    @Schema(description = "Legal status (e.g., RED_BOOK, SALE_CONTRACT, WAITING_FOR_BOOK)", example = "RED_BOOK")
    private String legal;

    @Schema(description = "Furniture status (e.g., FULL, BASIC, NONE)", example = "FULL")
    private String furniture;

    @Schema(description = "House direction (e.g., NORTH, SOUTH, EAST, WEST)", example = "SOUTH")
    private String direction;

    @Schema(description = "Balcony direction (e.g., NORTH, SOUTH, EAST, WEST)", example = "EAST")
    private String balconyDirection;

    @Schema(description = "Available from date (for RENT listings)", example = "2024-02-01")
    private LocalDate availableFrom;

    @Schema(description = "Dynamic attributes based on property type (e.g., floors, parking)")
    private Map<String, Object> dynamic;
}
