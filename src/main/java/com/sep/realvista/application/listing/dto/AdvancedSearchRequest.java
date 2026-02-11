package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdvancedSearchRequest {

    private String query; // General text search (optional)
    
    private String listingType; // SALE, RENT
    private String propertyType; // Property type CODE (e.g., "HOTEL", "APARTMENT")
    private String propertyCategory; // RESIDENTIAL, COMMERCIAL, etc.
    
    private String location; // District or City name
    
    // Ranges [min, max]
    private List<BigDecimal> price;
    private List<Double> area; // usableSizeM2
    
    private Integer bedrooms; // At least
    private Integer bathrooms; // At least
    
    // Specific attributes
    private String direction;
    private String balconyDirection;
    private String legal;
    private String furniture;
    
    private LocalDate availableFrom; // For RENT
    
    // Dynamic attributes (JSONB)
    // Key: attribute code, Value: exact match or range List
    private Map<String, Object> dynamic;
    
    private Boolean hasVideo;
    private Boolean has3D;
    
    // Sorting
    private String sortBy; // PRIORITY, DATE_DESC, PRICE_ASC, PRICE_DESC
}
