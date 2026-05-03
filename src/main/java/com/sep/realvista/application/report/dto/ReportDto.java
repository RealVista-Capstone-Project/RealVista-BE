package com.sep.realvista.application.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.domain.report.ReportStatus;
import com.sep.realvista.domain.report.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    @JsonProperty("report_id")
    private UUID reportId;

    @JsonProperty("reporter_id")
    private UUID reporterId;

    @JsonProperty("reporter_name")
    private String reporterName;

    @JsonProperty("reporter_email")
    private String reporterEmail;

    @JsonProperty("report_target_id")
    private UUID reportTargetId;

    @JsonProperty("report_target_type")
    private ReportTargetType reportTargetType;

    @JsonProperty("reported_listing_name")
    private String reportedListingName;

    @JsonProperty("reported_user_name")
    private String reportedUserName;

    @JsonProperty("report_reason")
    private String reportReason;

    private String description;
    
    private ReportStatus status;

    @JsonProperty("admin_note")
    private String adminNote;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
