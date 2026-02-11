package com.sep.realvista.presentation.rest.listing;

import com.sep.realvista.application.listing.service.ListingSearchService;
import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/listings", "/listings"})
@RequiredArgsConstructor
@Tag(name = "Listing Search", description = "Endpoints for searching property listings")
public class ListingSearchController {

    private final ListingSearchService listingSearchService;

    @Operation(summary = "Search Listings", description = "Search listings with filters. All parameters are optional. Use 'attr_' prefix for dynamic attributes (e.g., attr_direction=EAST)")
    @GetMapping("/search")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<ListingSearchResponse>> search(
            @RequestParam(required = false) String listingType,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false) String propertyCategory,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minArea,
            @RequestParam(required = false) Double maxArea,
            @RequestParam(required = false) Integer bedrooms,
            @RequestParam(required = false) Integer bathrooms,
            @RequestParam(required = false) String sortBy,
            @RequestParam Map<String, String> allParams,
            @PageableDefault(size = 12) Pageable pageable) {
        
        // Extract dynamic attributes (params starting with "attr_")
        Map<String, String> dynamicAttributes = allParams.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("attr_"))
            .collect(java.util.stream.Collectors.toMap(
                entry -> entry.getKey().substring(5), // Remove "attr_" prefix
                Map.Entry::getValue
            ));
        
        return ResponseEntity.ok(listingSearchService.search(
            listingType, propertyType, propertyCategory, location,
            minPrice, maxPrice, minArea, maxArea,
            bedrooms, bathrooms, dynamicAttributes, sortBy, pageable
        ));
    }
}
