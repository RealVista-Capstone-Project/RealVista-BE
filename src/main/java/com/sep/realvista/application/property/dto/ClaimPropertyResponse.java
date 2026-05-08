package com.sep.realvista.application.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimPropertyResponse {

    @JsonProperty("claim_id")
    private String claimId;

    @JsonProperty("property_id")
    private String propertyId;

    private String status;

    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;

    private String message;
}
