package com.sep.realvista.application.property.dto;

import com.sep.realvista.domain.property.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyMediaRequest {
    @NotBlank(message = "Media URL is required")
    private String url;
    
    private String thumbnailUrl;
    
    @NotNull(message = "Media type is required")
    private MediaType type;
    
    @Builder.Default
    private Boolean isThumbnail = false;
}
