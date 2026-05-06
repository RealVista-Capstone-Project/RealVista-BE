-- V142__Add_cancel_fields_to_lease_agreements.sql
-- Add cancellation audit fields for lease agreements cancelled before becoming ACTIVE.

ALTER TABLE lease_agreements
    ADD COLUMN IF NOT EXISTS cancel_reason TEXT,
    ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS cancelled_by UUID;
