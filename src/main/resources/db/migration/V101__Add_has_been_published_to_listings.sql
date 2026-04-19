-- V97__Add_has_been_published_to_listings.sql
-- Tracks whether a listing has ever been published (for quota deduction on first publish only).

ALTER TABLE listings ADD COLUMN has_been_published BOOLEAN DEFAULT FALSE;

-- Backfill: any listing that has been published before
UPDATE listings SET has_been_published = TRUE
WHERE published_at IS NOT NULL
   OR status IN ('PUBLISHED', 'SOLD', 'RENTED', 'EXPIRED');

-- Ensure no NULLs remain (e.g. rows inserted between ADD COLUMN and this UPDATE)
UPDATE listings SET has_been_published = FALSE WHERE has_been_published IS NULL;

-- Set NOT NULL constraint now that all rows have a value
ALTER TABLE listings ALTER COLUMN has_been_published SET NOT NULL;
