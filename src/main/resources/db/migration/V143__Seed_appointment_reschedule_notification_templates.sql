-- V142__Seed_appointment_reschedule_notification_templates.sql

-- 1. In-app/Push notification templates
INSERT INTO notification_templates (template_id, template_key, name, type, language, title, content_body)
VALUES 
    (gen_random_uuid(), 'APPOINTMENT_RESCHEDULE_PROPOSED', 'Appointment Reschedule Proposed (VI)', 'IN_APP', 'vi', 'Yêu cầu đổi lịch hẹn', 'Người dùng {{actorName}} muốn đổi lịch hẹn xem {{listingName}} sang {{tourTime}} ngày {{tourDate}}. Lý do: {{reason}}'),
    (gen_random_uuid(), 'APPOINTMENT_RESCHEDULE_PROPOSED', 'Appointment Reschedule Proposed (EN)', 'IN_APP', 'en', 'Appointment Reschedule Request', '{{actorName}} wants to reschedule the viewing of {{listingName}} to {{tourTime}} on {{tourDate}}. Reason: {{reason}}'),
    
    (gen_random_uuid(), 'APPOINTMENT_RESCHEDULE_ACCEPTED', 'Appointment Reschedule Accepted (VI)', 'IN_APP', 'vi', 'Lịch hẹn đã được đổi thành công', 'Yêu cầu đổi lịch xem {{listingName}} của bạn đã được chấp nhận. Thời gian mới: {{tourTime}} ngày {{tourDate}}'),
    (gen_random_uuid(), 'APPOINTMENT_RESCHEDULE_ACCEPTED', 'Appointment Reschedule Accepted (EN)', 'IN_APP', 'en', 'Appointment Reschedule Confirmed', 'Your reschedule request for {{listingName}} has been accepted. New time: {{tourTime}} on {{tourDate}}'),

    (gen_random_uuid(), 'APPOINTMENT_RESCHEDULE_REJECTED', 'Appointment Reschedule Rejected (VI)', 'IN_APP', 'vi', 'Yêu cầu đổi lịch bị từ chối', 'Yêu cầu đổi lịch xem {{listingName}} của bạn đã bị từ chối. Lý do: {{reason}}'),
    (gen_random_uuid(), 'APPOINTMENT_RESCHEDULE_REJECTED', 'Appointment Reschedule Rejected (EN)', 'IN_APP', 'en', 'Appointment Reschedule Rejected', 'Your reschedule request for {{listingName}} has been rejected. Reason: {{reason}}');
