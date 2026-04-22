package com.sep.realvista.application.listing.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ListingSearchCriteria {
    private String listingType;
    private String propertyType;
    private String propertyCategory;
    private String location;
    private UUID locationId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minArea;
    private Double maxArea;
    private String sortBy;
    private Map<String, String> dynamicAttributes;
    private Boolean hasVideo;
    private Boolean has3D;
}
