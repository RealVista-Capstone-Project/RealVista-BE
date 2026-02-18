package com.sep.realvista.application.listing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for similar listings endpoint.
 * Contains a list of similar listings with their similarity scores.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarListingsResponse {

    /**
     * List of similar listings sorted by similarity score (descending).
     */
    private List<SimilarListingDTO> listings;

    /**
     * Total number of similar listings found.
     */
    private Integer total;

    /**
     * The limit that was applied to the results.
     */
    private Integer limit;
}
