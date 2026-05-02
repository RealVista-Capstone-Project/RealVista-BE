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
@Schema(description = "Agent lead channel performance item")
public class AgentPerformanceChannelDTO {

    @Schema(description = "Lead source channel", example = "chat")
    private String channel;

    @Schema(description = "Lead count for the channel", example = "22")
    private Long leads;

    @Schema(description = "Channel conversion rate percentage", example = "31")
    private Integer conversionRate;
}
