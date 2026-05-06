package com.sep.realvista.application.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent performance trend data point")
public class AgentPerformanceTrendPointDTO {

    @Schema(description = "Bucket label shown in chart", example = "Mon")
    private String month;

    @Schema(description = "Views in this bucket", example = "125")
    private Long views;

    @Schema(description = "Inquiries in this bucket", example = "14")
    private Long inquiries;

    @Schema(description = "Closed deals in this bucket", example = "3")
    private Long closedDeals;
}
