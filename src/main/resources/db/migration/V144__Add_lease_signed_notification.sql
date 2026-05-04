-- V144__Add_lease_signed_notification.sql
-- Add notification event/template for completed lease signing.

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
    (gen_random_uuid(), 'LEASE_SIGNED', 'Lease Signed Notification', 'IN_APP', 'vi',
     'Hợp đồng thuê đã hoàn tất',
     'Hợp đồng thuê tại {{propertyAddress}} đã được chủ nhà và người thuê ký hoàn tất. Trạng thái hiện tại: Đang hoạt động.',
     NOW(), NOW(), false),
    (gen_random_uuid(), 'LEASE_SIGNED', 'Lease Signed Notification', 'IN_APP', 'en',
     'Lease agreement completed',
     'The lease agreement for {{propertyAddress}} has been signed by both landlord and renter. Current status: ACTIVE.',
     NOW(), NOW(), false)
ON CONFLICT (template_key, language) DO UPDATE
SET name = EXCLUDED.name,
    title = EXCLUDED.title,
    content_body = EXCLUDED.content_body,
    updated_at = NOW(),
    deleted = false;
