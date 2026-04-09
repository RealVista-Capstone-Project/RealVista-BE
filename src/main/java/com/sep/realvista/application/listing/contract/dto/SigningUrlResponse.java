package com.sep.realvista.application.listing.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO containing an embedded signing URL for DocuSign.
 * The frontend should redirect the user to {@code signingUrl} to complete signing.
 */
@Data
@Builder
public class SigningUrlResponse {

    /** DocuSign embedded signing URL. Redirect the user to this URL to sign. */
    @JsonProperty("signing_url")
    private String signingUrl;

    /** DocuSign envelope ID associated with this signing session. */
    @JsonProperty("envelope_id")
    private String envelopeId;

    /** Who this signing URL is for (e.g. "renter" or "landlord"). */
    @JsonProperty("signer_role")
    private String signerRole;
}
