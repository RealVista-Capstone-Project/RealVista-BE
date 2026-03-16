package com.sep.realvista.application.media.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkMediaUploadResponse {

    @JsonProperty("uploaded_files")
    private List<MediaUploadResponse> uploadedFiles;

    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("success_count")
    private Integer successCount;

    @JsonProperty("failed_count")
    private Integer failedCount;

    @JsonProperty("failed_files")
    private List<FailedUpload> failedFiles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedUpload {

        @JsonProperty("file_name")
        private String fileName;

        @JsonProperty("error_message")
        private String errorMessage;
    }
}
