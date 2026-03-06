package com.sep.realvista.application.listing.bookmark.dto;

import com.sep.realvista.domain.listing.ListingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for getting bookmarked listings with filters and pagination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetBookmarksRequest {

    @Schema(description = "Property type codes to filter by", example = "[\"APARTMENT\", \"VILLA\"]")
    private List<String> propertyTypes;

    @Schema(description = "Listing type filter: SALE or RENT", example = "RENT")
    private ListingType listingType;

    @Schema(
            description = "Sort direction based on bookmark creation date",
            example = "NEWEST",
            allowableValues = {"NEWEST", "OLDEST"}
    )
    @Builder.Default
    private SortDirection sortDirection = SortDirection.NEWEST;

    @Schema(description = "Page number (0-indexed)", example = "0")
    @Min(0)
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "Page size", example = "10")
    @Min(1)
    @Max(100)
    @Builder.Default
    private Integer size = 10;

    /**
     * Sort direction enum for bookmark listing.
     */
    public enum SortDirection {
        NEWEST,  // Most recently bookmarked first
        OLDEST   // Oldest bookmarks first
    }
}
