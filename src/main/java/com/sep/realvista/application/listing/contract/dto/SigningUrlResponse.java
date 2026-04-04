package com.sep.realvista.application.listing.contract.dto;

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
    private String signingUrl;

    /** DocuSign envelope ID associated with this signing session. */
    private String envelopeId;

    /** Who this signing URL is for (e.g. "renter" or "landlord"). */
    private String signerRole;
}
