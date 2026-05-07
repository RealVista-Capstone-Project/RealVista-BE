-- V152: Add duplicate address detection columns and property_claims table

-- Add duplicate-detection columns to properties table
ALTER TABLE properties
    ADD COLUMN IF NOT EXISTS flagged_for_admin_review BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS duplicate_override_reason VARCHAR(100),
    ADD COLUMN IF NOT EXISTS stale_at TIMESTAMP;

-- Index for admin review queue
CREATE INDEX IF NOT EXISTS idx_property_flagged ON properties (flagged_for_admin_review)
    WHERE flagged_for_admin_review = TRUE AND deleted = FALSE;

-- Index for stale detection scheduler
CREATE INDEX IF NOT EXISTS idx_property_stale_at ON properties (stale_at)
    WHERE stale_at IS NOT NULL AND deleted = FALSE;

-- property_claims table: tracks ownership claim requests
CREATE TABLE IF NOT EXISTS property_claims (
    claim_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id      UUID        NOT NULL REFERENCES properties (property_id) ON DELETE CASCADE,
    claimant_id      UUID        NOT NULL,
    claim_reason     VARCHAR(50) NOT NULL,
    message          TEXT,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at       TIMESTAMP   NOT NULL,
    resolved_at      TIMESTAMP,
    created_at       TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP   NOT NULL DEFAULT now(),
    deleted          BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_claim_status CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'EXPIRED', 'ESCALATED')),
    CONSTRAINT chk_claim_reason CHECK (claim_reason IN ('NEW_OWNER', 'DIFFERENT_UNIT', 'OTHER'))
);

CREATE INDEX IF NOT EXISTS idx_claim_property  ON property_claims (property_id);
CREATE INDEX IF NOT EXISTS idx_claim_claimant  ON property_claims (claimant_id);
CREATE INDEX IF NOT EXISTS idx_claim_status    ON property_claims (status) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_claim_expires   ON property_claims (expires_at) WHERE status = 'PENDING';
