-- Additional Appointment Status Notification Templates
INSERT INTO notification_templates (template_id, template_key, name, type, language, title, content_body, created_at, updated_at, deleted)
VALUES 
    -- Appointment Accepted
    (gen_random_uuid(), 'APPOINTMENT_ACCEPTED', 'Appointment Accepted', 'IN_APP', 'vi', 'Lịch xem nhà được chấp nhận', 'Yêu cầu xem nhà {{listingName}} vào {{tourDate}} ({{tourTime}}) đã được chấp nhận.', NOW(), NOW(), false),
    (gen_random_uuid(), 'APPOINTMENT_ACCEPTED', 'Appointment Accepted', 'IN_APP', 'en', 'Tour Request Accepted', 'Your tour request for {{listingName}} on {{tourDate}} ({{tourTime}}) has been accepted.', NOW(), NOW(), false),

    -- Appointment Rejected
    (gen_random_uuid(), 'APPOINTMENT_REJECTED', 'Appointment Rejected', 'IN_APP', 'vi', 'Lịch xem nhà bị từ chối', 'Yêu cầu xem nhà {{listingName}} vào {{tourDate}} ({{tourTime}}) đã bị từ chối. Lý do: {{reason}}', NOW(), NOW(), false),
    (gen_random_uuid(), 'APPOINTMENT_REJECTED', 'Appointment Rejected', 'IN_APP', 'en', 'Tour Request Rejected', 'Your tour request for {{listingName}} on {{tourDate}} ({{tourTime}}) was rejected. Reason: {{reason}}', NOW(), NOW(), false),

    -- Appointment Cancelled
    (gen_random_uuid(), 'APPOINTMENT_CANCELLED', 'Appointment Cancelled', 'IN_APP', 'vi', 'Lịch xem nhà bị hủy', '{{actorName}} đã hủy lịch xem nhà {{listingName}} vào {{tourDate}} ({{tourTime}}). Lý do: {{reason}}', NOW(), NOW(), false),
    (gen_random_uuid(), 'APPOINTMENT_CANCELLED', 'Appointment Cancelled', 'IN_APP', 'en', 'Tour Request Cancelled', '{{actorName}} has cancelled the tour for {{listingName}} on {{tourDate}} ({{tourTime}}). Reason: {{reason}}', NOW(), NOW(), false),

    -- Appointment Completed
    (gen_random_uuid(), 'APPOINTMENT_COMPLETED', 'Appointment Completed', 'IN_APP', 'vi', 'Lịch xem nhà hoàn thành', 'Cảm ơn bạn đã tham quan {{listingName}}. Vui lòng để lại đánh giá nếu bạn hài lòng.', NOW(), NOW(), false),
    (gen_random_uuid(), 'APPOINTMENT_COMPLETED', 'Appointment Completed', 'IN_APP', 'en', 'Tour Completed', 'Thank you for visiting {{listingName}}. Please leave a review if you are satisfied.', NOW(), NOW(), false),

    -- Listing Unpublished
    (gen_random_uuid(), 'LISTING_UNPUBLISHED', 'Listing Unpublished', 'IN_APP', 'vi', 'Tin đăng đã bị gỡ', 'Tin đăng {{listingName}} không còn khả dụng. Lịch xem nhà của bạn đã bị hủy tự động.', NOW(), NOW(), false),
    (gen_random_uuid(), 'LISTING_UNPUBLISHED', 'Listing Unpublished', 'IN_APP', 'en', 'Listing Unpublished', 'The listing {{listingName}} is no longer available. Your tour request has been automatically cancelled.', NOW(), NOW(), false)
ON CONFLICT (template_key, language) DO UPDATE 
SET name = EXCLUDED.name,
    title = EXCLUDED.title,
    content_body = EXCLUDED.content_body,
    updated_at = NOW();
