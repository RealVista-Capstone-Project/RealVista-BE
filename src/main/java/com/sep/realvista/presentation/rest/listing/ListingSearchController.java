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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import com.sep.realvista.application.listing.dto.ListingFilterDTO;
import com.sep.realvista.application.listing.dto.ListingSearchCriteria;
import org.springframework.web.bind.annotation.ModelAttribute;

@RestController
@RequestMapping({"/api/v1/listings", "/listings"})
@RequiredArgsConstructor
@Tag(name = "Listing Search", description = "Endpoints for searching property listings")
public class ListingSearchController {

    private final ListingSearchService listingSearchService;

    @Operation(
            summary = "Search Listings",
            description = "Search listings with filters. All parameters are optional. "
                    + "Use 'attr_' prefix for dynamic attributes (e.g., attr_direction=EAST)"
    )
    @GetMapping("/search")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<ListingSearchResponse>> search(
            @ModelAttribute ListingFilterDTO filter,
            @RequestParam Map<String, String> allParams,
            @PageableDefault(size = 12) Pageable pageable) {
        
        // Extract dynamic attributes (params starting with "attr_")
        Map<String, String> dynamicAttributes = allParams.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("attr_"))
            .collect(java.util.stream.Collectors.toMap(
                entry -> entry.getKey().substring(5), // Remove "attr_" prefix
                Map.Entry::getValue
            ));
        
        ListingSearchCriteria criteria = ListingSearchCriteria.builder()
            .listingType(filter.getListingType())
            .propertyType(filter.getPropertyType())
            .propertyCategory(filter.getPropertyCategory())
            .location(filter.getLocation())
            .minPrice(filter.getMinPrice())
            .maxPrice(filter.getMaxPrice())
            .minArea(filter.getMinArea())
            .maxArea(filter.getMaxArea())
            .bedrooms(filter.getBedrooms())
            .bathrooms(filter.getBathrooms())
            .sortBy(filter.getSortBy())
            .dynamicAttributes(dynamicAttributes)
            .build();

        return ResponseEntity.ok(listingSearchService.search(criteria, pageable));
    }
}
