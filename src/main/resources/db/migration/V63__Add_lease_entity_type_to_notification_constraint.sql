-- V61__Add_lease_entity_type_to_notification_constraint.sql
-- Update notification entity type check constraint to include 'LEASE'

ALTER TABLE notifications
DROP CONSTRAINT IF EXISTS chk_notification_entity_type;

ALTER TABLE notifications
ADD CONSTRAINT chk_notification_entity_type
    CHECK (entity_type IN ('LISTING', 'APPOINTMENT', 'MESSAGE', 'USER', 'PROPERTY', 'LEASE'));
