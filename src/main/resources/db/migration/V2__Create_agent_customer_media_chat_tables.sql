-- V2__Create_agent_customer_media_chat_tables.sql
-- Creates agent profiles, customer profiles, media, amenities, appointments and chat tables
-- Compatible with both PostgreSQL and H2 databases

-- ============================================================================
-- AGENT MANAGEMENT TABLES
-- ============================================================================

CREATE TABLE agent_profiles
(
    agent_profile_id    UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id             UUID NOT NULL UNIQUE,
    bio                 TEXT,
    specialties         TEXT,
    service_areas       TEXT,
    rating              NUMERIC(2, 1) DEFAULT 0.0,
    years_of_experience INTEGER,
    properties_sold     INTEGER      DEFAULT 0,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN      DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE INDEX idx_agent_profile_rating ON agent_profiles (rating);

CREATE TABLE agent_licenses
(
    agent_license_id     UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    agent_profile_id     UUID         NOT NULL,
    license_number       VARCHAR(100) NOT NULL,
    issuing_authority    VARCHAR(255) NOT NULL,
    license_type         VARCHAR(100) NOT NULL,
    issued_date          DATE         NOT NULL,
    expiry_date          DATE         NOT NULL,
    status               VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    verified_at          TIMESTAMP,
    verified_by          UUID,
    verification_note    TEXT,
    license_document_url TEXT,
    is_active            BOOLEAN      DEFAULT TRUE,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted              BOOLEAN      DEFAULT FALSE,

    FOREIGN KEY (agent_profile_id) REFERENCES agent_profiles (agent_profile_id) ON DELETE CASCADE,
    CONSTRAINT chk_license_status CHECK (status IN ('PENDING', 'VERIFIED', 'REJECTED', 'EXPIRED'))
);

CREATE INDEX idx_agent_license_profile ON agent_licenses (agent_profile_id);
CREATE INDEX idx_agent_license_status ON agent_licenses (status);
CREATE INDEX idx_agent_license_expiry ON agent_licenses (expiry_date);

CREATE TABLE property_agents
(
    property_agent_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    property_id       UUID      NOT NULL,
    agent_id          UUID      NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted           BOOLEAN DEFAULT FALSE,

    UNIQUE (property_id, agent_id),
    FOREIGN KEY (property_id) REFERENCES properties (property_id) ON DELETE CASCADE
);

CREATE INDEX idx_property_agent_property ON property_agents (property_id);
CREATE INDEX idx_property_agent_agent ON property_agents (agent_id);

-- ============================================================================
-- CUSTOMER MANAGEMENT TABLES
-- ============================================================================

CREATE TABLE customer_profiles
(
    customer_profile_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id             UUID         NOT NULL,
    profile_name        VARCHAR(255) NOT NULL,
    is_active           BOOLEAN   DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN   DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE INDEX idx_customer_profile_user ON customer_profiles (user_id);
CREATE INDEX idx_customer_profile_active ON customer_profiles (is_active);

-- ============================================================================
-- AMENITIES TABLES
-- ============================================================================

CREATE TABLE amenities
(
    amenity_id   UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    amenity_name VARCHAR(100) NOT NULL,
    amenity_type VARCHAR(20)  NOT NULL,
    description  TEXT,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted      BOOLEAN DEFAULT FALSE,

    CONSTRAINT chk_amenity_type CHECK (amenity_type IN ('ONSITE', 'OFFSITE'))
);

CREATE INDEX idx_amenity_type ON amenities (amenity_type);

CREATE TABLE property_amenities
(
    property_amenity_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    property_id         UUID      NOT NULL,
    amenity_id          UUID      NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN DEFAULT FALSE,

    UNIQUE (property_id, amenity_id),
    FOREIGN KEY (property_id) REFERENCES properties (property_id) ON DELETE CASCADE,
    FOREIGN KEY (amenity_id) REFERENCES amenities (amenity_id) ON DELETE RESTRICT
);

CREATE INDEX idx_property_amenity_property ON property_amenities (property_id);
CREATE INDEX idx_property_amenity_amenity ON property_amenities (amenity_id);

-- ============================================================================
-- MEDIA TABLES
-- ============================================================================

CREATE TABLE property_medias
(
    property_media_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    property_id       UUID        NOT NULL,
    upload_by         UUID        NOT NULL,
    media_type        VARCHAR(20) NOT NULL,
    media_url         TEXT        NOT NULL,
    thumbnail_url     TEXT,
    is_primary        BOOLEAN   DEFAULT FALSE,
    created_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted           BOOLEAN   DEFAULT FALSE,

    FOREIGN KEY (property_id) REFERENCES properties (property_id) ON DELETE CASCADE,
    CONSTRAINT chk_media_type CHECK (media_type IN ('VIDEO', 'IMAGE', '3D'))
);

CREATE INDEX idx_property_media_property ON property_medias (property_id);
CREATE INDEX idx_property_media_type ON property_medias (media_type);

CREATE TABLE listing_medias
(
    listing_media_id  UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    listing_id        UUID      NOT NULL,
    property_media_id UUID      NOT NULL,
    display_order     INTEGER DEFAULT 0,
    is_primary        BOOLEAN DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted           BOOLEAN DEFAULT FALSE,

    UNIQUE (listing_id, property_media_id),
    FOREIGN KEY (listing_id) REFERENCES listings (listing_id) ON DELETE CASCADE,
    FOREIGN KEY (property_media_id) REFERENCES property_medias (property_media_id)
);

CREATE INDEX idx_listing_media_listing ON listing_medias (listing_id);

-- ============================================================================
-- APPOINTMENT TABLES
-- ============================================================================

CREATE TABLE appointments
(
    appointment_id      UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    listing_id          UUID        NOT NULL,
    sender_id           UUID        NOT NULL,
    receiver_id         UUID        NOT NULL,
    start_time          TIMESTAMP   NOT NULL,
    end_time            TIMESTAMP   NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    appointment_type    VARCHAR(20) NOT NULL,
    sender_notes        TEXT,
    rejection_reason    TEXT,
    cancellation_reason TEXT,
    canceled_by_user_id UUID,
    reminder_before     INTEGER   DEFAULT 60,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN   DEFAULT FALSE,

    FOREIGN KEY (listing_id) REFERENCES listings (listing_id) ON DELETE CASCADE,
    CONSTRAINT chk_appointment_type CHECK (appointment_type IN ('TOUR', 'BLOCK')),
    CONSTRAINT chk_appointment_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELED', 'COMPLETED'))
);

CREATE INDEX idx_appointment_listing ON appointments (listing_id);
CREATE INDEX idx_appointment_sender ON appointments (sender_id);
CREATE INDEX idx_appointment_receiver ON appointments (receiver_id);
CREATE INDEX idx_appointment_status ON appointments (status);
CREATE INDEX idx_appointment_start ON appointments (start_time);

-- ============================================================================
-- CHAT/CONVERSATION TABLES
-- ============================================================================

CREATE TABLE conversations
(
    conversation_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN DEFAULT FALSE
);

CREATE TABLE user_conversations
(
    user_conversation_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    conversation_id      UUID      NOT NULL,
    user_id              UUID      NOT NULL,
    last_read_message_id UUID,
    unread_count         INTEGER DEFAULT 0,
    is_archived          BOOLEAN DEFAULT FALSE,
    is_muted             BOOLEAN DEFAULT FALSE,
    created_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted              BOOLEAN DEFAULT FALSE,

    UNIQUE (conversation_id, user_id),
    FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id) ON DELETE CASCADE
);

CREATE INDEX idx_user_conversation_conv ON user_conversations (conversation_id);
CREATE INDEX idx_user_conversation_user ON user_conversations (user_id);

CREATE TABLE messages
(
    message_id          UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    conversation_id     UUID        NOT NULL,
    reply_to_message_id UUID,
    sender_id           UUID,
    message_type        VARCHAR(20) NOT NULL,
    content             TEXT,
    metadata            JSON,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id) ON DELETE CASCADE,
    FOREIGN KEY (reply_to_message_id) REFERENCES messages (message_id),
    CONSTRAINT chk_message_type CHECK (message_type IN ('TEXT', 'LISTING_CARD', 'CONTRACT_CARD', 'SYSTEM'))
);

CREATE INDEX idx_message_conversation ON messages (conversation_id);
CREATE INDEX idx_message_sender ON messages (sender_id);
CREATE INDEX idx_message_type ON messages (message_type);
CREATE INDEX idx_message_created ON messages (created_at);
