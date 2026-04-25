-- V106: Extend listing_leads and lead_notes for CRM pipeline feature
-- Adds free-text contact info, new status values, source column,
-- and agent_id to lead_notes for note authorship tracking.

-- 1. Drop old status CHECK constraint (6 values → 7 CRM values)
ALTER TABLE listing_leads
    DROP CONSTRAINT IF EXISTS chk_lead_status;

-- 2. Add free-text contact columns (nullable — existing rows have buyer_id)
ALTER TABLE listing_leads
    ADD COLUMN IF NOT EXISTS full_name  VARCHAR(255),
    ADD COLUMN IF NOT EXISTS email      VARCHAR(255),
    ADD COLUMN IF NOT EXISTS phone      VARCHAR(50),
    ADD COLUMN IF NOT EXISTS source     VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    ADD COLUMN IF NOT EXISTS budget     NUMERIC(20, 2);

-- 3. Make buyer_id nullable (CRM manual leads may not have a registered buyer)
ALTER TABLE listing_leads
    ALTER COLUMN buyer_id DROP NOT NULL;

-- 4. Re-add CHECK constraint with CRM statuses
ALTER TABLE listing_leads
    ADD CONSTRAINT chk_lead_status
        CHECK (status IN ('NEW','CONSULTING','TOUR_SCHEDULED','TOURED','NEGOTIATING','CLOSED','NOT_POTENTIAL'));

-- 5. Migrate existing rows: map old statuses to closest CRM equivalent
UPDATE listing_leads SET status = 'NEW'         WHERE status = 'NEW';
UPDATE listing_leads SET status = 'CONSULTING'  WHERE status = 'CONTACTED';
UPDATE listing_leads SET status = 'NEGOTIATING' WHERE status = 'QUALIFIED';
UPDATE listing_leads SET status = 'NEGOTIATING' WHERE status = 'NEGOTIATING';
UPDATE listing_leads SET status = 'CLOSED'      WHERE status IN ('CLOSED_WON', 'CLOSED_LOST');

-- 6. Add agent_id to lead_notes (who wrote the note)
ALTER TABLE lead_notes
    ADD COLUMN IF NOT EXISTS agent_id UUID REFERENCES users(user_id);

-- 7. Add status_at_time snapshot to lead_notes
ALTER TABLE lead_notes
    ADD COLUMN IF NOT EXISTS status_at_time VARCHAR(20);
