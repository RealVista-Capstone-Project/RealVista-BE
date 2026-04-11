package com.sep.realvista.application.engagement.dto;

import com.sep.realvista.domain.engagement.proposal.AgentProposalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyAgentProposalRequest {

    private String title;

    private BigDecimal commissionRate;

    private Integer experienceYears;

    private String pitchContent;

    private String specialty;

    private Map<String, Object> priceRange;

    private AgentProposalStatus status;
}
