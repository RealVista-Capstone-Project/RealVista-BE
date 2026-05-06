package com.sep.realvista.application.listing.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request body for cancelling a lease agreement before it becomes active.
 * The reason is optional and stored for record-keeping.
 */
@Getter
@NoArgsConstructor
public class CancelLeaseRequest {

    @JsonProperty("reason")
    private String reason;
}
