-- Add missing columns to saved_searches that were added in remote migrations (V90-V100)
-- but not present in local migration files after a clean rebuild.

ALTER TABLE saved_searches
    ADD COLUMN IF NOT EXISTS board_id VARCHAR(100);
