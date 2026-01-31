package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.domain.property.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Media information nested DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaDTO {
    @JsonProperty("media_id")
    private UUID mediaId;
    @JsonProperty("media_type")
    private MediaType mediaType;
    @JsonProperty("media_url")
    private String mediaUrl;
    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;
    @JsonProperty("is_primary")
    private Boolean isPrimary;
    @JsonProperty("display_order")
    private Integer displayOrder;

    // Helper methods for UI
    public boolean isImage() {
        return mediaType == MediaType.IMAGE;
    }

    public boolean isVideo() {
        return mediaType == MediaType.VIDEO;
    }

    public boolean is3D() {
        return mediaType == MediaType.THREE_D;
    }
}
