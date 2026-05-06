-- Add final signed DocuSign document tracking to lease agreements.
-- The webhook marks completed envelopes as PENDING; a background job downloads the final PDF,
-- uploads it to storage, and updates these fields for frontend preview.

ALTER TABLE lease_agreements
    ADD COLUMN IF NOT EXISTS signed_document_url TEXT,
    ADD COLUMN IF NOT EXISTS signed_document_status VARCHAR(30) NOT NULL DEFAULT 'NOT_REQUESTED',
    ADD COLUMN IF NOT EXISTS signed_document_error TEXT,
    ADD COLUMN IF NOT EXISTS signed_document_processed_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_lease_signed_document_status
    ON lease_agreements (signed_document_status);
