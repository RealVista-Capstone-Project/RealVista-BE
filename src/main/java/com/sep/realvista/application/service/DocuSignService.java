package com.sep.realvista.application.service;

import com.sep.realvista.application.listing.contract.dto.LeaseTemplateData;

/**
 * Application-layer service interface for DocuSign eSignature operations.
 * <p>
 * Abstracts the DocuSign SDK from the application layer, following the same
 * pattern as {@link EmailService} and {@link FirebaseNotificationService}.
 * <p>
 * All operations are no-ops (returning empty/null) when DocuSign is not configured.
 * Check {@link #isAvailable()} before calling signing operations.
 */
public interface DocuSignService {

    /**
     * Returns true if DocuSign is configured and available.
     */
    boolean isAvailable();

    /**
     * Creates a DocuSign envelope for a lease agreement document and sends it to the
     * first signer (renter) for embedded signing.
     *
     * @param documentBytes    PDF bytes of the lease document
     * @param documentName     Display name for the document (e.g. "Lease Agreement")
     * @param signerEmail      Email address of the signer
     * @param signerName       Full name of the signer
     * @param signerClientUserId Unique client-side user ID for embedded signing (e.g. user UUID)
     * @return DocuSign envelope ID
     */
    String createEnvelopeForSigning(
            byte[] documentBytes,
            String documentName,
            String signerEmail,
            String signerName,
            String signerClientUserId
    );

    /**
     * Generates an embedded signing URL for a recipient who already has an envelope.
     *
     * @param envelopeId       DocuSign envelope ID
     * @param signerEmail      Email of the recipient
     * @param signerName       Full name of the recipient
     * @param signerClientUserId Client-side user ID used when the envelope was created
     * @param returnUrl        URL to redirect to after signing is complete
     * @return Embedded signing URL (redirect user to this URL)
     */
    String getEmbeddedSigningUrl(
            String envelopeId,
            String signerEmail,
            String signerName,
            String signerClientUserId,
            String returnUrl
    );

    /**
     * Retrieves the current status of a DocuSign envelope.
     *
     * @param envelopeId DocuSign envelope ID
     * @return Envelope status string (e.g. "sent", "delivered", "completed", "voided")
     */
    String getEnvelopeStatus(String envelopeId);

    /**
     * Adds a second signer (landlord) to an existing envelope as a sequential recipient.
     * Creates a new envelope if the existing one is already completed.
     *
     * @param envelopeId       Existing envelope ID (after renter signed)
     * @param documentBytes    PDF bytes (used if a new envelope must be created)
     * @param documentName     Display name for the document
     * @param signerEmail      Landlord email
     * @param signerName       Landlord full name
     * @param signerClientUserId Landlord client-side user ID
     * @return Envelope ID (same or new)
     */
    String addLandlordSigner(
            String envelopeId,
            byte[] documentBytes,
            String documentName,
            String signerEmail,
            String signerName,
            String signerClientUserId
    );

    /**
     * Verifies the HMAC signature on a DocuSign Connect webhook payload.
     *
     * @param payload   Raw request body bytes
     * @param hmacHeader Value of the X-DocuSign-Signature-1 header
     * @return true if signature is valid (or if HMAC key is not configured)
     */
    boolean verifyWebhookSignature(byte[] payload, String hmacHeader);

    /**
     * Creates a DocuSign envelope from a pre-configured template with dynamic field values.
     * Both renter and landlord are added as recipients with sequential signing order.
     *
     * @param templateId Template ID configured in DocuSign
     * @param data       Dynamic field values to populate in the template tabs
     * @return DocuSign envelope ID
     */
    String createEnvelopeFromTemplate(String templateId, LeaseTemplateData data);
}
