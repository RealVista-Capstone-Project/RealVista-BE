-- V51: Add DocuSign eSignature fields to lease_agreements table
-- Adds envelope tracking columns for DocuSign integration

ALTER TABLE lease_agreements
    ADD COLUMN IF NOT EXISTS docusign_envelope_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS docusign_status      VARCHAR(50);

-- Index for webhook lookups by envelope ID
CREATE INDEX IF NOT EXISTS idx_lease_docusign_envelope
    ON lease_agreements (docusign_envelope_id)
    WHERE docusign_envelope_id IS NOT NULL;
