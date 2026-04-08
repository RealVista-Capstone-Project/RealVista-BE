package com.sep.realvista.application.listing.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request body for terminating a lease agreement.
 * All fields are optional — the reason is stored for record-keeping.
 */
@Getter
@NoArgsConstructor
public class TerminateLeaseRequest {

    @JsonProperty("reason")
    private String reason;
}
