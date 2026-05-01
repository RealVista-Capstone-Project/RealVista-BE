-- Seed In-app Notification Templates for real project usages
INSERT INTO notification_templates (template_id, template_key, name, type, language, title, content_body, created_at, updated_at, deleted)
VALUES 
    -- Lease Terminated
    (gen_random_uuid(), 'LEASE_TERMINATED', 'Lease Termination Notification', 'IN_APP', 'vi', 'Hợp đồng thuê nhà đã bị chấm dứt', 'Chủ nhà đã chấm dứt hợp đồng thuê nhà của bạn. Lý do: {{reason}}', NOW(), NOW(), false),
    (gen_random_uuid(), 'LEASE_TERMINATED', 'Lease Termination Notification', 'IN_APP', 'en', 'Lease Agreement Terminated', 'The landlord has terminated your lease agreement. Reason: {{reason}}', NOW(), NOW(), false),
    
    -- New Tour Request (For Owners/Agents)
    (gen_random_uuid(), 'NEW_TOUR_REQUEST', 'New Tour Request', 'IN_APP', 'vi', 'Yêu cầu xem nhà mới', '{{senderName}} muốn xem nhà tại {{listingName}} vào {{tourDate}} ({{tourTime}})', NOW(), NOW(), false),
    (gen_random_uuid(), 'NEW_TOUR_REQUEST', 'New Tour Request', 'IN_APP', 'en', 'New Tour Request', '{{senderName}} wants to view {{listingName}} on {{tourDate}} ({{tourTime}})', NOW(), NOW(), false),
    
    -- Tour Booking Success (For Renters)
    (gen_random_uuid(), 'TOUR_BOOKING_SUCCESS', 'Tour Booking Successful', 'IN_APP', 'vi', 'Đặt lịch xem nhà thành công', 'Yêu cầu xem nhà {{listingName}} vào {{tourDate}} ({{tourTime}}) đã được gửi thành công.', NOW(), NOW(), false),
    (gen_random_uuid(), 'TOUR_BOOKING_SUCCESS', 'Tour Booking Successful', 'IN_APP', 'en', 'Tour Booking Confirmed', 'Your tour request for {{listingName}} on {{tourDate}} ({{tourTime}}) has been submitted successfully.', NOW(), NOW(), false),
    
    -- System Notification
    (gen_random_uuid(), 'SYSTEM', 'System Notification', 'IN_APP', 'vi', 'Thông báo hệ thống', '{{message}}', NOW(), NOW(), false),
    (gen_random_uuid(), 'SYSTEM', 'System Notification', 'IN_APP', 'en', 'System Notification', '{{message}}', NOW(), NOW(), false)
ON CONFLICT (template_key, language) DO UPDATE 
SET name = EXCLUDED.name,
    title = EXCLUDED.title,
    content_body = EXCLUDED.content_body,
    updated_at = NOW();
