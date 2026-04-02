package com.sep.realvista.application.media.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateProperty3DOperationRequest {
    private String model;
    private String displayName;
    private List<ImageAsset> images;

    @Data
    public static class ImageAsset {
        private String mediaAssetId;
        private Double azimuth;
    }
}
