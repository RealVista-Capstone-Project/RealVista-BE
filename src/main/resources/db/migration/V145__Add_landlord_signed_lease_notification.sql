-- V145__Add_landlord_signed_lease_notification.sql
-- Add notification event/template for landlord signing completion.

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
    (gen_random_uuid(), 'LEASE_LANDLORD_SIGNED', 'Lease Landlord Signed Notification', 'IN_APP', 'vi',
     'Chủ nhà đã ký hợp đồng thuê',
     'Hợp đồng thuê tại {{propertyAddress}} đã được chủ nhà ký. Trạng thái hiện tại: Chờ người thuê ký.',
     NOW(), NOW(), false),
    (gen_random_uuid(), 'LEASE_LANDLORD_SIGNED', 'Lease Landlord Signed Notification', 'IN_APP', 'en',
     'Landlord signed lease agreement',
     'The lease agreement for {{propertyAddress}} has been signed by the landlord. Current status: PENDING_RENTER, waiting for renter signature.',
     NOW(), NOW(), false)
ON CONFLICT (template_key, language) DO UPDATE
SET name = EXCLUDED.name,
    title = EXCLUDED.title,
    content_body = EXCLUDED.content_body,
    updated_at = NOW(),
    deleted = false;
