-- V141__Add_is_active_to_policies.sql
-- Adds is_active column to policies table

ALTER TABLE policies ADD COLUMN is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE policies ADD COLUMN version INTEGER DEFAULT 1;

-- Update existing records to be active by default if not specified
UPDATE policies SET is_active = TRUE WHERE is_active IS NULL;
UPDATE policies SET version = 1 WHERE version IS NULL;
