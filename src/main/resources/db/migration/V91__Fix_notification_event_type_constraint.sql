-- V90__Fix_notification_event_type_constraint.sql
-- Fix chk_notification_event_type to include all EventType enum values.
-- Previous migrations (V78, V88, V89) dropped and recreated this constraint
-- without carrying forward LISTING_RENTED, LEASE_TERMINATED and LISTING_EXPIRING_SOON.

ALTER TABLE notifications DROP CONSTRAINT IF EXISTS chk_notification_event_type;

ALTER TABLE notifications ADD CONSTRAINT chk_notification_event_type
    CHECK (event_type IN (
        'NEW_LISTING',
        'PRICE_CHANGE',
        'APPOINTMENT_REMINDER',
        'APPOINTMENT_CONFIRMED',
        'APPOINTMENT_CANCELLED',
        'NEW_TOUR_REQUEST',
        'NEW_MESSAGE',
        'LISTING_EXPIRED',
        'LISTING_EXPIRING_SOON',
        'LISTING_SOLD',
        'LISTING_RENTED',
        'LEASE_TERMINATED',
        'SYSTEM',
        'PROPERTY_3D_GENERATED',
        'PROPERTY_3D_FAILED',
        'NEW_AGENT_PROPOSAL',
        'AGENT_PROPOSAL_ACCEPTED',
        'AGENT_PROPOSAL_REJECTED'
    ));
