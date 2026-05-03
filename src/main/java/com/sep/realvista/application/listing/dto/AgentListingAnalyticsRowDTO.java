package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Per-listing analytics row for agent dashboard "top listings" card.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Listing with aggregated analytics for agent dashboard")
public class AgentListingAnalyticsRowDTO {

    @Schema(description = "Listing identifier")
    @JsonProperty("listing_id")
    private UUID listingId;

    @Schema(description = "Property identifier")
    @JsonProperty("property_id")
    private UUID propertyId;

    @Schema(description = "Listing display name")
    @JsonProperty("name")
    private String name;

    @Schema(description = "SEO slug")
    @JsonProperty("slug")
    private String slug;

    @Schema(description = "Primary thumbnail URL if available")
    @JsonProperty("thumbnail")
    private String thumbnail;

    @Schema(description = "Listing type")
    @JsonProperty("listing_type")
    private ListingType listingType;

    @Schema(description = "Listing status")
    @JsonProperty("status")
    private ListingStatus status;

    @Schema(description = "Listed price")
    @JsonProperty("price")
    private BigDecimal price;

    @Schema(description = "Formatted full address")
    @JsonProperty("full_address")
    private String fullAddress;

    @Schema(description = "Published timestamp")
    @JsonProperty("published_at")
    private LocalDateTime publishedAt;

    @Schema(description = "Total view count (sum of view_count)")
    @JsonProperty("total_views")
    private Integer totalViews;

    @Schema(description = "Distinct viewers")
    @JsonProperty("unique_viewers")
    private Integer uniqueViewers;

    @Schema(description = "Tour appointment count")
    @JsonProperty("tour_bookings")
    private Integer tourBookings;

    @Schema(description = "CRM inquiries (leads) for this listing")
    @JsonProperty("inquiries")
    private Integer inquiries;

    @Schema(description = "Conversion rate: tour bookings / total views * 100")
    @JsonProperty("conversion_rate")
    private BigDecimal conversionRate;
}
