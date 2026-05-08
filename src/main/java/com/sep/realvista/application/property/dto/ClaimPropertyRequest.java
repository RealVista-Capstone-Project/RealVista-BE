package com.sep.realvista.application.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimPropertyRequest {

    /**
     * Reason for the claim.
     * Allowed: NEW_OWNER | DIFFERENT_UNIT | OTHER
     */
    @NotBlank(message = "Claim reason is required")
    @JsonProperty("claim_reason")
    private String claimReason;

    /** Optional free-text description to help the admin review. */
    private String message;
}
