package com.sep.realvista.application.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent performance analytics metrics")
public class AgentPerformanceAnalyticsDTO {

    @Schema(description = "Selected period granularity", example = "W")
    private String period;

    @Schema(description = "Trend points for selected period")
    private List<AgentPerformanceTrendPointDTO> trend;

    @Schema(description = "Lead source distribution and conversion")
    private List<AgentPerformanceChannelDTO> channels;
}
