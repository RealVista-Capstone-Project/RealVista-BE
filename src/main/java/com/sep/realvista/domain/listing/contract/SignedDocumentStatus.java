package com.sep.realvista.domain.listing.contract;

/**
 * Processing status for the final signed lease document downloaded from DocuSign.
 */
public enum SignedDocumentStatus {
    /** No signed document has been requested yet. */
    NOT_REQUESTED,

    /** DocuSign completed and the document is waiting for background processing. */
    PENDING,

    /** Background worker is downloading/uploading the signed document. */
    PROCESSING,

    /** Signed document was uploaded and its URL is available. */
    COMPLETED,

    /** Processing failed and can be retried by the background worker. */
    FAILED
}
