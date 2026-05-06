package com.sep.realvista.application.report.dto;

import com.sep.realvista.domain.report.ReportReason;
import com.sep.realvista.domain.report.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new report for a user or listing")
public class CreateReportRequest {

    @NotNull(message = "Target type is required")
    @Schema(description = "Type of target being reported", example = "LISTING",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private ReportTargetType targetType;

    @NotNull(message = "Target ID is required")
    @Schema(description = "UUID of the target (user or listing)",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID targetId;

    @NotNull(message = "Report reason is required")
    @Schema(description = "Reason for the report", example = "SCAM",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private ReportReason reason;

    @Schema(description = "Detailed description of the issue",
            example = "This listing contains false price information")
    private String description;

    @Schema(description = "URL of evidence media (optional)", example = "https://cdn.example.com/evidence.jpg")
    private String evidenceMediaUrl;
}
