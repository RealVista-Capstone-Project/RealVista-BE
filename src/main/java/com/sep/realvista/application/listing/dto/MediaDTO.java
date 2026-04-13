package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sep.realvista.domain.property.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Media information nested DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MediaDTO {
    private UUID mediaId;
    private MediaType mediaType;
    private String mediaUrl;
    private String thumbnailUrl;
    private Boolean isPrimary;
    private Boolean isPropertyStandard;
    private Integer displayOrder;
    private Map<String, Object> metadata;

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
