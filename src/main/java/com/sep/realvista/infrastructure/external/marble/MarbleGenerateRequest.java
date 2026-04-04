package com.sep.realvista.infrastructure.external.marble;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
public class MarbleGenerateRequest {
    @JsonProperty("display_name")
    private String displayName;
    
    private String model;
    
    @JsonProperty("world_prompt")
    private WorldPrompt worldPrompt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorldPrompt {
        private String type; // e.g., "multi-image"
        
        @JsonProperty("multi_image_prompt")
        private List<MultiImagePrompt> multiImagePrompt;

        @JsonProperty("reconstruct_images")
        private Boolean reconstructImages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MultiImagePrompt {
        private Double azimuth;
        private Content content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private String source; // e.g., "media_asset"
        
        @JsonProperty("media_asset_id")
        private String mediaAssetId;
        
        private String uri; // Optional, for "uri" source
    }
}
