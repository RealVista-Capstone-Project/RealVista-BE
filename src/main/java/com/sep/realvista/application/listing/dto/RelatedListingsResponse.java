package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for related listings by property.
 * Returns both RENT and SALE listings if they exist and are active.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Related listings for the same property")
public class RelatedListingsResponse {

    @JsonProperty("rent_listing")
    @Schema(description = "RENT listing for the property, if available and active")
    private ListingResponse rentListing;

    @JsonProperty("sale_listing")
    @Schema(description = "SALE listing for the property, if available and active")
    private ListingResponse saleListing;
}
