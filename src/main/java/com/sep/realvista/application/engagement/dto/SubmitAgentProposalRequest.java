package com.sep.realvista.application.engagement.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for an agent to submit a proposal for a property.
 * 
 * Contains the proposal template ID and the targeting property ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmitAgentProposalRequest {

    @NotNull(message = "Agent proposal template ID is required")
    @JsonProperty("agent_proposal_id")
    @JsonAlias({"agent_proposal_id", "agentProposalId"})
    private UUID agentProposalId;

    @NotNull(message = "Property ID is required")
    @JsonProperty("property_id")
    @JsonAlias({"property_id", "propertyId"})
    private UUID propertyId;

    @JsonProperty("message")
    private String message;
}
