-- V4__Create_saved_search_notification_preference_attribute_tables.sql
-- Creates saved searches, notifications, user preferences, property attributes tables
-- Compatible with both PostgreSQL and H2 databases

-- ============================================================================
-- SAVED SEARCHES TABLE
-- ============================================================================

CREATE TABLE saved_searches
(
    saved_search_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    profile_id      UUID        NOT NULL,
    search_type     VARCHAR(20) NOT NULL,
    criteria        JSON        NOT NULL,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (profile_id) REFERENCES customer_profiles (customer_profile_id) ON DELETE CASCADE,
    CONSTRAINT chk_search_type CHECK (search_type IN ('BUY', 'RENT', 'SELL'))
);

CREATE INDEX idx_saved_search_profile ON saved_searches (profile_id);
CREATE INDEX idx_saved_search_type ON saved_searches (search_type);

-- ============================================================================
-- NOTIFICATIONS TABLE
-- ============================================================================

CREATE TABLE notifications
(
    notification_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id         UUID         NOT NULL,
    channel         VARCHAR(20)  NOT NULL,
    title           VARCHAR(255) NOT NULL,
    message         TEXT,
    delivery_status VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    is_read         BOOLEAN      NOT NULL DEFAULT FALSE,
    event_type      VARCHAR(50)  NOT NULL,
    metadata        JSON,
    entity_type     VARCHAR(50),
    entity_id       UUID,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN DEFAULT FALSE,

    CONSTRAINT chk_notification_channel CHECK (channel IN ('IN_APP', 'EMAIL', 'BOTH')),
    CONSTRAINT chk_notification_status CHECK (delivery_status IN ('PENDING', 'SENT', 'FAILED', 'PARTIAL')),
    CONSTRAINT chk_notification_event_type CHECK (event_type IN ('NEW_LISTING', 'PRICE_CHANGE', 'APPOINTMENT_REMINDER', 'APPOINTMENT_CONFIRMED', 'APPOINTMENT_CANCELLED', 'NEW_MESSAGE', 'LISTING_EXPIRED', 'LISTING_SOLD', 'SYSTEM')),
    CONSTRAINT chk_notification_entity_type CHECK (entity_type IN ('LISTING', 'APPOINTMENT', 'MESSAGE', 'USER', 'PROPERTY'))
);

CREATE INDEX idx_notification_user ON notifications (user_id);
CREATE INDEX idx_notification_channel ON notifications (channel);
CREATE INDEX idx_notification_status ON notifications (delivery_status);
CREATE INDEX idx_notification_event_type ON notifications (event_type);
CREATE INDEX idx_notification_entity ON notifications (entity_type, entity_id);

-- ============================================================================
-- USER SETTINGS / PREFERENCES TABLE
-- ============================================================================

CREATE TABLE setting_preferences
(
    setting_preference_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id               UUID      NOT NULL UNIQUE,
    in_app_enabled        BOOLEAN DEFAULT TRUE,
    email_enabled         BOOLEAN DEFAULT TRUE,
    push_enabled          BOOLEAN DEFAULT TRUE,
    event_preference      JSON,
    contact_via_email     BOOLEAN DEFAULT TRUE,
    contact_via_phone     BOOLEAN DEFAULT FALSE,
    hide_phone_number     BOOLEAN DEFAULT TRUE,
    hide_email            BOOLEAN DEFAULT TRUE,
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted               BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- ============================================================================
-- PROPERTY ATTRIBUTES TABLE (Dynamic Attributes)
-- ============================================================================

CREATE TABLE property_attributes
(
    property_attribute_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name                  VARCHAR(100) NOT NULL,
    code                  VARCHAR(50)  NOT NULL UNIQUE,
    data_type             VARCHAR(20)  NOT NULL,
    is_searchable         BOOLEAN DEFAULT TRUE,
    icon                  VARCHAR(100),
    unit                  VARCHAR(20),
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted               BOOLEAN DEFAULT FALSE,

    CONSTRAINT chk_attribute_data_type CHECK (data_type IN ('NUMBER', 'BOOLEAN', 'TEXT'))
);

CREATE INDEX idx_property_attribute_data_type ON property_attributes (data_type);

-- ============================================================================
-- PROPERTY TYPE ATTRIBUTES TABLE (Link attributes to property types)
-- ============================================================================

CREATE TABLE property_type_attributes
(
    property_type_attribute_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    property_type_id           UUID      NOT NULL,
    property_attribute_id      UUID      NOT NULL,
    is_required                BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at                 TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at                 TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted                    BOOLEAN DEFAULT FALSE,

    UNIQUE (property_type_id, property_attribute_id),
    FOREIGN KEY (property_type_id) REFERENCES property_types (property_type_id) ON DELETE CASCADE,
    FOREIGN KEY (property_attribute_id) REFERENCES property_attributes (property_attribute_id) ON DELETE RESTRICT
);

CREATE INDEX idx_property_type_attr_type ON property_type_attributes (property_type_id);
CREATE INDEX idx_property_type_attr_attr ON property_type_attributes (property_attribute_id);

-- ============================================================================
-- PROPERTY ATTRIBUTE VALUES TABLE (Actual values for each property)
-- ============================================================================

CREATE TABLE property_attribute_values
(
    property_attribute_value_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    property_id                 UUID      NOT NULL,
    property_attribute_id       UUID      NOT NULL,
    value_number                NUMERIC(12, 2),
    value_text                  VARCHAR(255),
    value_boolean               BOOLEAN,
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted                     BOOLEAN DEFAULT FALSE,

    UNIQUE (property_id, property_attribute_id),
    FOREIGN KEY (property_id) REFERENCES properties (property_id) ON DELETE CASCADE,
    FOREIGN KEY (property_attribute_id) REFERENCES property_attributes (property_attribute_id) ON DELETE RESTRICT
);

CREATE INDEX idx_property_attr_value_property ON property_attribute_values (property_id);
CREATE INDEX idx_property_attr_value_attr ON property_attribute_values (property_attribute_id);

-- ============================================================================
-- PROPERTY ATTRIBUTE RANGES TABLE (For filtering/searching)
-- ============================================================================

CREATE TABLE property_attribute_ranges
(
    property_attribute_range_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    property_attribute_id       UUID        NOT NULL,
    label                       VARCHAR(50) NOT NULL,
    min_value                   NUMERIC(12, 2),
    max_value                   NUMERIC(12, 2),
    display_order               INTEGER DEFAULT 0,
    created_at                  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted                     BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (property_attribute_id) REFERENCES property_attributes (property_attribute_id) ON DELETE CASCADE
);

CREATE INDEX idx_property_attr_range_attr ON property_attribute_ranges (property_attribute_id);
