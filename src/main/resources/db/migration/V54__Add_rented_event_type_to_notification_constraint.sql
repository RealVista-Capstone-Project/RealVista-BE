-- V54__Add_rented_event_type_to_notification_constraint.sql
-- Update notification event type check constraint to include 'LISTING_RENTED'

ALTER TABLE notifications 
DROP CONSTRAINT IF EXISTS chk_notification_event_type;

ALTER TABLE notifications 
ADD CONSTRAINT chk_notification_event_type
    CHECK (event_type IN ('NEW_LISTING', 'PRICE_CHANGE', 'APPOINTMENT_REMINDER', 
                          'APPOINTMENT_CONFIRMED', 'APPOINTMENT_CANCELLED', 
                          'NEW_TOUR_REQUEST', 'NEW_MESSAGE', 'LISTING_EXPIRED', 
                          'LISTING_SOLD', 'LISTING_RENTED', 'SYSTEM'));
