package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for listing analytics metrics.
 * <p>
 * Contains aggregated statistics about a listing's performance including
 * views, unique viewers, tour bookings, and conversion rate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Listing analytics metrics")
public class ListingAnalyticsDTO {

    @Schema(description = "Total number of views (sum of all view counts)", example = "245")
    @JsonProperty("total_views")
    private Integer totalViews;

    @Schema(description = "Number of unique viewers (distinct users)", example = "132")
    @JsonProperty("unique_viewers")
    private Integer uniqueViewers;

    @Schema(description = "Number of tour bookings/appointments", example = "8")
    @JsonProperty("tour_bookings")
    private Integer tourBookings;

    @Schema(description = "Conversion rate (tour bookings / total views * 100)", example = "3.27")
    @JsonProperty("conversion_rate")
    private BigDecimal conversionRate;
}
