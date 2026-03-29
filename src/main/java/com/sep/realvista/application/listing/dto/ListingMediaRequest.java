package com.sep.realvista.application.listing.dto;

import com.sep.realvista.domain.property.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shared DTO for media requests in listing operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingMediaRequest {
    private String url;
    private MediaType type;
    private String thumbnailUrl;
    private Boolean isPrimary;
}
