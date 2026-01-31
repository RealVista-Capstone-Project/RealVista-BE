package com.sep.realvista.application.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Brief information of a property listing in search results")
public class ListingSearchResponse {

    @Schema(description = "Unique identifier of the listing", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID listingId;

    @Schema(description = "Display title of the listing", example = "Căn hộ cao cấp Quận 7")
    private String title;

    @Schema(description = "Type of property", example = "Chung cư")
    private String propertyType;

    @Schema(description = "Location description", example = "Quận 7, TP. HCM")
    private String location;

    @Schema(description = "Price of the listing", example = "7500000000")
    private BigDecimal price;

    @Schema(description = "Usable area in m2", example = "85.5")
    private BigDecimal area;

    @Schema(description = "URL to the thumbnail image", example = "https://example.com/image.jpg")
    private String thumbnailUrl;
}
