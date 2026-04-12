-- Add AGENT_PROPOSAL_ACCEPTED / AGENT_PROPOSAL_REJECTED to notification event_type constraint
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS chk_notification_event_type;
ALTER TABLE notifications ADD CONSTRAINT chk_notification_event_type
    CHECK (event_type IN ('NEW_LISTING', 'PRICE_CHANGE', 'APPOINTMENT_REMINDER',
                          'APPOINTMENT_CONFIRMED', 'APPOINTMENT_CANCELLED',
                          'NEW_TOUR_REQUEST', 'NEW_MESSAGE', 'LISTING_EXPIRED',
                          'LISTING_SOLD', 'SYSTEM', 'PROPERTY_3D_GENERATED',
                          'PROPERTY_3D_FAILED', 'NEW_AGENT_PROPOSAL',
                          'AGENT_PROPOSAL_ACCEPTED', 'AGENT_PROPOSAL_REJECTED'));
