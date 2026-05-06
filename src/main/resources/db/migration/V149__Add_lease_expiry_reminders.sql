-- Add lease expiry reminder tracking and notification template.

ALTER TABLE lease_agreements
    ADD COLUMN IF NOT EXISTS expiry_reminder_30_sent_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS expiry_reminder_7_sent_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS expiry_reminder_due_sent_at TIMESTAMP;

ALTER TABLE notifications DROP CONSTRAINT IF EXISTS chk_notification_event_type;

ALTER TABLE notifications ADD CONSTRAINT chk_notification_event_type
    CHECK (event_type IN (
        'NEW_LISTING',
        'PRICE_CHANGE',
        'APPOINTMENT_REMINDER',
        'APPOINTMENT_CONFIRMED',
        'APPOINTMENT_CANCELLED',
        'APPOINTMENT_REJECTED',
        'NEW_TOUR_REQUEST',
        'NEW_MESSAGE',
        'LISTING_EXPIRED',
        'LISTING_EXPIRING_SOON',
        'LISTING_SOLD',
        'SYSTEM',
        'PROPERTY_3D_GENERATED',
        'PROPERTY_3D_FAILED',
        'LISTING_RENTED',
        'LEASE_TERMINATED',
        'LEASE_SIGNED',
        'LEASE_LANDLORD_SIGNED',
        'LEASE_EXPIRY_REMINDER',
        'NEW_AGENT_PROPOSAL',
        'AGENT_PROPOSAL_ACCEPTED',
        'AGENT_PROPOSAL_REJECTED',
        'OWNER_ENGAGEMENT_REVIEW_REMINDER',
        'LISTING_UNPUBLISHED',
        'PROPERTY_UPDATED_BY_ADMIN',
        'PROPERTY_DELETED_BY_ADMIN'
    ));

INSERT INTO notification_templates (template_id, template_key, name, type, language, title, content_body, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), 'LEASE_EXPIRY_REMINDER', 'Lease Expiry Reminder', 'IN_APP', 'vi',
     'Hợp đồng thuê sắp hết hạn',
     'Hợp đồng thuê tại {{propertyAddress}} sẽ hết hạn vào {{leaseEndDate}}. Còn {{daysBeforeExpiry}} ngày đến ngày hết hạn.',
     NOW(), NOW(), false),
    (gen_random_uuid(), 'LEASE_EXPIRY_REMINDER', 'Lease Expiry Reminder', 'IN_APP', 'en',
     'Lease agreement expiring soon',
     'The lease agreement for {{propertyAddress}} expires on {{leaseEndDate}}. Days until expiry: {{daysBeforeExpiry}}.',
     NOW(), NOW(), false)
ON CONFLICT (template_key, language) DO UPDATE
SET name = EXCLUDED.name,
    title = EXCLUDED.title,
    content_body = EXCLUDED.content_body,
    updated_at = NOW(),
    deleted = false;
