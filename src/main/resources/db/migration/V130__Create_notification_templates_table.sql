CREATE TABLE notification_templates (
    template_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_key VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    language VARCHAR(10) NOT NULL,
    title VARCHAR(255),
    content_body TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    UNIQUE (template_key, language)
);

CREATE INDEX idx_notification_template_key_lang ON notification_templates (template_key, language);
CREATE INDEX idx_notification_template_type ON notification_templates (type);

-- Seed basic templates
INSERT INTO notification_templates (template_key, name, type, language, title, content_body) VALUES
('WELCOME_EMAIL', 'Welcome Email', 'EMAIL', 'en', 'Welcome to RealVista!', 'Hi {{name}}, welcome to our platform.'),
('WELCOME_EMAIL', 'Email Chào mừng', 'EMAIL', 'vi', 'Chào mừng đến với RealVista!', 'Chào {{name}}, chào mừng bạn đến với hệ thống của chúng tôi.'),
('LISTING_APPROVED', 'Listing Approved', 'IN_APP', 'en', 'Your listing has been approved', 'Your listing {{listingName}} is now live.'),
('LISTING_APPROVED', 'Tin đăng đã được duyệt', 'IN_APP', 'vi', 'Tin đăng của bạn đã được duyệt', 'Tin đăng {{listingName}} của bạn đã được hiển thị công khai.');
