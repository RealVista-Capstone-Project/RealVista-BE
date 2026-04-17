package com.sep.realvista.application.engagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class SendOwnerInvitationRequest {

    @NotNull(message = "agent_id is required")
    @JsonProperty("agent_id")
    private UUID agentId;

    @NotNull(message = "property_id is required")
    @JsonProperty("property_id")
    private UUID propertyId;

    @Size(max = 200, message = "Title must not exceed 200 characters")
    @JsonProperty("title")
    private String title;

    @JsonProperty("offered_commission")
    private Double offeredCommission;

    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    @JsonProperty("message")
    private String message;
}
