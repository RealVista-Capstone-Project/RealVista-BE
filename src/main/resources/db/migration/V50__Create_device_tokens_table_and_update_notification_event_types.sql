-- ============================================================================
-- V43: Create device_tokens table for FCM push notifications
--      and update notifications CHECK constraint to include NEW_TOUR_REQUEST
-- ============================================================================

-- DEVICE TOKENS TABLE (FCM token storage for push notifications)
CREATE TABLE device_tokens
(
    device_token_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id         UUID          NOT NULL,
    fcm_token       VARCHAR(512)  NOT NULL,
    device_type     VARCHAR(20)   NOT NULL,
    device_name     VARCHAR(255),
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    last_used_at    TIMESTAMP,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN DEFAULT FALSE,

    CONSTRAINT chk_device_type CHECK (device_type IN ('WEB', 'ANDROID', 'IOS')),
    CONSTRAINT uq_user_fcm_token UNIQUE (user_id, fcm_token),
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE INDEX idx_device_token_user ON device_tokens (user_id);
CREATE INDEX idx_device_token_active ON device_tokens (user_id, active);

-- Update notifications CHECK constraint to include NEW_TOUR_REQUEST
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS chk_notification_event_type;
ALTER TABLE notifications ADD CONSTRAINT chk_notification_event_type
    CHECK (event_type IN ('NEW_LISTING', 'PRICE_CHANGE', 'APPOINTMENT_REMINDER',
                          'APPOINTMENT_CONFIRMED', 'APPOINTMENT_CANCELLED',
                          'NEW_TOUR_REQUEST', 'NEW_MESSAGE', 'LISTING_EXPIRED',
                          'LISTING_SOLD', 'SYSTEM'));
