package com.sep.realvista.application.engagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for checking whether an agent can submit a proposal
 * to a property owner based on the latest engagement state.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentProposalApplyStateResponse {

    @JsonProperty("can_apply_proposal")
    private boolean canApplyProposal;

    @JsonProperty("engagement_status")
    private String engagementStatus;
}
