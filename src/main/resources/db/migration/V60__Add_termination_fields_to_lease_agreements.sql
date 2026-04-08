-- V60__Add_termination_fields_to_lease_agreements.sql
-- Add termination_reason and terminated_at to lease_agreements
-- to support the improved lease termination business logic.

ALTER TABLE lease_agreements
    ADD COLUMN IF NOT EXISTS termination_reason TEXT,
    ADD COLUMN IF NOT EXISTS terminated_at TIMESTAMP;

-- Update notification event type check constraint to include 'LEASE_TERMINATED'
ALTER TABLE notifications
DROP CONSTRAINT IF EXISTS chk_notification_event_type;

ALTER TABLE notifications
ADD CONSTRAINT chk_notification_event_type
    CHECK (event_type IN ('NEW_LISTING', 'PRICE_CHANGE', 'APPOINTMENT_REMINDER',
                          'APPOINTMENT_CONFIRMED', 'APPOINTMENT_CANCELLED',
                          'NEW_TOUR_REQUEST', 'NEW_MESSAGE', 'LISTING_EXPIRED',
                          'LISTING_SOLD', 'LISTING_RENTED', 'LEASE_TERMINATED', 'SYSTEM'));
