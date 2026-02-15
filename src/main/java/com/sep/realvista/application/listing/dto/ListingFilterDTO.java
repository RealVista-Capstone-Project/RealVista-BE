package com.sep.realvista.application.listing.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ListingFilterDTO {
    private String listingType;
    private String propertyType;
    private String propertyCategory;
    private String location;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minArea;
    private Double maxArea;
    private Integer bedrooms;
    private Integer bathrooms;
    private String sortBy;
}
