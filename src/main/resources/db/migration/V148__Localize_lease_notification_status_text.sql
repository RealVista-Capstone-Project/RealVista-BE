-- V146__Localize_lease_notification_status_text.sql
-- Replace raw enum status names in Vietnamese lease notification templates with user-facing Vietnamese labels.

UPDATE notification_templates
SET content_body = 'Hợp đồng thuê tại {{propertyAddress}} đã được chủ nhà và người thuê ký hoàn tất. Trạng thái hiện tại: Đang hoạt động.',
    updated_at = NOW(),
    deleted = false
WHERE template_key = 'LEASE_SIGNED'
  AND language = 'vi';

UPDATE notification_templates
SET content_body = 'Hợp đồng thuê tại {{propertyAddress}} đã được chủ nhà ký. Trạng thái hiện tại: Chờ người thuê ký.',
    updated_at = NOW(),
    deleted = false
WHERE template_key = 'LEASE_LANDLORD_SIGNED'
  AND language = 'vi';
