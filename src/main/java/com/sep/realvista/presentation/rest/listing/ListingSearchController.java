package com.sep.realvista.presentation.rest.listing;

import com.sep.realvista.application.listing.dto.ListingSearchCriteria;
import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.application.listing.service.ListingSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springdoc.core.annotations.ParameterObject;

@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
@Tag(name = "Listing Search", description = "Endpoints for searching and filtering property listings")
public class ListingSearchController {

    private final ListingSearchService listingSearchService;

    @Operation(
            summary = "Search Listings",
            description = "Search for published listings using various filter criteria.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved matching listings",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid search criteria provided",
                            content = @Content
                    )
            }
    )
    @GetMapping("/search")
    public ResponseEntity<Page<ListingSearchResponse>> search(
            @ParameterObject ListingSearchCriteria criteria,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ListingSearchResponse> results = listingSearchService.search(criteria, pageable);
        return ResponseEntity.ok(results);
    }
}
