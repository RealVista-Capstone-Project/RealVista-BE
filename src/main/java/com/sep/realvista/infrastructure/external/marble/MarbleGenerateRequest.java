package com.sep.realvista.infrastructure.external.marble;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MarbleGenerateRequest {
    @JsonProperty("display_name")
    private String displayName;
    
    private String model;
    
    @JsonProperty("generation_options")
    private GenerationOptions generationOptions;

    @Data
    @Builder
    public static class GenerationOptions {
        private List<ImageParam> images;
    }

    @Data
    @Builder
    public static class ImageParam {
        @JsonProperty("media_asset_id")
        private String mediaAssetId;
        
        @JsonProperty("camera_parameters")
        private CameraParameters cameraParameters;
    }

    @Data
    @Builder
    public static class CameraParameters {
        private Double azimuth;
    }
}
