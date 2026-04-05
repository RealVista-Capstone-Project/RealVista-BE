package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.domain.listing.ListingType;
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
public class ListingSummaryDTO {
    @JsonProperty("listing_id")
    private UUID listingId;
    
    private String name;
    private String slug;
    private BigDecimal price;
    
    @JsonProperty("listing_type")
    private ListingType listingType;
    
    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;
    
    @JsonProperty("agent_name")
    private String agentName;
}
