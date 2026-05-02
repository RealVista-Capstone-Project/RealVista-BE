-- Seed EMAIL_OTP template
INSERT INTO notification_templates (template_key, name, type, language, title, content_body) VALUES
('EMAIL_OTP', 'Email Verification OTP', 'EMAIL', 'en', 'RealVista Email Verification Code', 'Hi {{userName}}, your verification code is {{otp}}. It will expire in {{expiryMinutes}} minutes.'),
('EMAIL_OTP', 'Mã xác minh Email', 'EMAIL', 'vi', 'Mã xác minh email RealVista', 'Chào {{userName}}, mã xác minh của bạn là {{otp}}. Mã này sẽ hết hạn sau {{expiryMinutes}} phút.');

-- Seed AGENT_PROPOSAL templates
INSERT INTO notification_templates (template_key, name, type, language, title, content_body) VALUES
('AGENT_PROPOSAL_NOTIFICATION', 'Agent Proposal Received', 'EMAIL', 'en', 'New Agent Proposal on RealVista', 'Hi {{ownerName}}, agent {{agentName}} has sent a new proposal "{{proposalTitle}}" for your property at {{propertyAddress}}. View it here: {{viewEngagementsUrl}}'),
('AGENT_PROPOSAL_NOTIFICATION', 'Nhận đề xuất môi giới', 'EMAIL', 'vi', 'Đề xuất môi giới mới trên RealVista', 'Chào {{ownerName}}, môi giới {{agentName}} vừa gửi một đề xuất mới "{{proposalTitle}}" cho bất động sản của bạn tại {{propertyAddress}}. Xem chi tiết tại đây: {{viewEngagementsUrl}}'),
('AGENT_PROPOSAL_DECISION', 'Agent Proposal Decision', 'EMAIL', 'en', 'Decision on your Agent Proposal', 'Hi {{agentName}}, {{ownerName}} has {{#if accepted}}accepted{{else}}rejected{{/if}} your proposal "{{proposalTitle}}" for property at {{propertyAddress}}. View details: {{viewEngagementsUrl}}'),
('AGENT_PROPOSAL_DECISION', 'Kết quả đề xuất môi giới', 'EMAIL', 'vi', 'Kết quả đề xuất môi giới của bạn', 'Chào {{agentName}}, {{ownerName}} đã {{#if accepted}}chấp nhận{{else}}từ chối{{/if}} đề xuất "{{proposalTitle}}" của bạn cho bất động sản tại {{propertyAddress}}. Xem chi tiết: {{viewEngagementsUrl}}');

-- Seed TOUR_BOOKING templates
INSERT INTO notification_templates (template_key, name, type, language, title, content_body) VALUES
('TOUR_BOOKING_CONFIRMATION', 'Tour Booking Confirmation', 'EMAIL', 'en', 'Tour Booking Confirmation: {{listingName}}', 'Hi {{senderName}}, your tour request for {{listingName}} at {{propertyAddress}} on {{tourDate}} ({{tourTime}}) has been submitted. Status: {{status}}. Notes: {{notes}}'),
('TOUR_BOOKING_CONFIRMATION', 'Xác nhận đặt lịch tham quan', 'EMAIL', 'vi', 'Xác nhận đặt lịch tham quan: {{listingName}}', 'Chào {{senderName}}, yêu cầu tham quan của bạn cho {{listingName}} tại {{propertyAddress}} vào ngày {{tourDate}} ({{tourTime}}) đã được gửi thành công. Trạng thái: {{status}}. Ghi chú: {{notes}}'),
('TOUR_BOOKING_NOTIFICATION', 'New Tour Booking Request', 'EMAIL', 'en', 'New Tour Request: {{listingName}}', 'Hi {{ownerName}}, you have a new tour request from {{senderName}} for {{listingName}} at {{propertyAddress}} on {{tourDate}} ({{tourTime}}). Notes: {{notes}}'),
('TOUR_BOOKING_NOTIFICATION', 'Yêu cầu tham quan mới', 'EMAIL', 'vi', 'Yêu cầu tham quan mới: {{listingName}}', 'Chào {{ownerName}}, bạn có một yêu cầu tham quan mới từ {{senderName}} cho {{listingName}} tại {{propertyAddress}} vào ngày {{tourDate}} ({{tourTime}}). Ghi chú: {{notes}}'),
('TOUR_BOOKING_STATUS_CHANGE', 'Tour Booking Status Change', 'EMAIL', 'en', 'Tour Booking Update: {{listingName}}', 'Hi {{recipientName}}, your tour booking for {{listingName}} has been updated to: {{status}}. {{#if reason}}Reason: {{reason}}{{/if}}'),
('TOUR_BOOKING_STATUS_CHANGE', 'Cập nhật trạng thái lịch tham quan', 'EMAIL', 'vi', 'Cập nhật lịch tham quan: {{listingName}}', 'Chào {{recipientName}}, lịch tham quan của bạn cho {{listingName}} đã được cập nhật trạng thái thành: {{status}}. {{#if reason}}Lý do: {{reason}}{{/if}}');
