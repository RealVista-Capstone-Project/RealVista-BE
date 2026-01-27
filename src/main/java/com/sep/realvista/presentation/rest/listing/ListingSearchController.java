package com.sep.realvista.presentation.rest.listing;

import com.sep.realvista.application.listing.ListingSearchService;
import com.sep.realvista.presentation.rest.listing.dto.AdvancedSearchRequest;
import com.sep.realvista.presentation.rest.listing.dto.ListingSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
@Tag(name = "Listing Search", description = "Endpoints for searching and filtering property listings")
public class ListingSearchController {

    private final ListingSearchService listingSearchService;

    @Operation(
            summary = "Advanced Search",
            description = "Search for published listings using both fixed and dynamic property attributes.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved matching listings",
                            content = @Content(mediaType = "application/json", 
                                    schema = @Schema(implementation = ListingSearchResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid search criteria provided",
                            content = @Content
                    )
            }
    )
    @PostMapping("/search")
    public ResponseEntity<List<ListingSearchResponse>> search(@RequestBody AdvancedSearchRequest request) {
        return ResponseEntity.ok(listingSearchService.search(request));
    }
}
