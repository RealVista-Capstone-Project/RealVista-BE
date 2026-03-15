package com.sep.realvista.application.media.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadResponse {

    @JsonProperty("media_url")
    private String mediaUrl;

    @JsonProperty("media_type")
    private String mediaType;

    @JsonProperty("file_size")
    private Long fileSize;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("uploaded_at")
    private String uploadedAt;
}
