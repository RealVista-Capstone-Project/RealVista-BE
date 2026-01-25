-- V1__Create_user_role_property_listing_tables.sql
-- Complete database schema for RealVista
-- Compatible with both PostgreSQL and H2 databases

-- ============================================================================
-- USER MANAGEMENT TABLES
-- ============================================================================

CREATE TABLE "roles"
(
    role_id         UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    role_code       VARCHAR(50) NOT NULL UNIQUE,
    description     TEXT,
    is_system_role  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN DEFAULT FALSE,

    CONSTRAINT chk_role_code CHECK (role_code IN ('ADMIN', 'VERIFIER', 'AGENT', 'OWNER', 'BUYER', 'TENANT'))
);

CREATE TABLE "users"
(
    user_id         UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    first_name        VARCHAR(100),
    last_name         VARCHAR(100),
    business_name     VARCHAR(255) NOT NULL,
    password_hash     VARCHAR(255) NOT NULL,
    email             VARCHAR(255) UNIQUE,
    phone             VARCHAR(20) UNIQUE,
    email_verified_at TIMESTAMP,
    phone_verified_at TIMESTAMP,
    avatar_url        TEXT,
    status            VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted           BOOLEAN DEFAULT FALSE,

    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'VERIFIED', 'SUSPENDED', 'BANNED'))
);

CREATE INDEX idx_user_status ON "users"(status);

CREATE TABLE "user_roles"
(
    user_id     UUID NOT NULL,
    role_id     UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted     BOOLEAN DEFAULT FALSE,

    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES "users" (user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES "roles" (role_id) ON DELETE RESTRICT
);

CREATE TABLE "user_sessions"
(
    user_session_id         UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id            UUID NOT NULL,
    session_token_hash VARCHAR(255) NOT NULL UNIQUE,
    device_type        VARCHAR(20) NOT NULL 
        CHECK (device_type IN ('WEB', 'IOS', 'ANDROID')),
    ip_address         VARCHAR(45),
    user_agent         TEXT,
    expires_at         TIMESTAMP NOT NULL,
    revoked_at         TIMESTAMP,
    last_activity_at   TIMESTAMP,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted            BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES "users" (user_id) ON DELETE CASCADE
);

CREATE INDEX idx_user_session_user_id ON "user_sessions"(user_id);

-- ============================================================================
-- STEP 3: CREATE NEW SCHEMA - PROPERTY & LISTING TABLES
-- ============================================================================

CREATE TABLE property_categories
(
    property_category_id         UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name                 VARCHAR(100) NOT NULL,
    code                 VARCHAR(50) UNIQUE NOT NULL,
    created_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted              BOOLEAN DEFAULT FALSE
);

CREATE TABLE property_types
(
    property_type_id         UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    property_category_id UUID NOT NULL,
    name                 VARCHAR(100) NOT NULL,
    code                 VARCHAR(50) UNIQUE,
    description          TEXT,
    status               VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted              BOOLEAN DEFAULT FALSE,

    CONSTRAINT chk_property_type_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    FOREIGN KEY (property_category_id) REFERENCES property_categories (property_category_id)
);

CREATE INDEX idx_property_type_category ON property_types(property_category_id);
CREATE INDEX idx_property_type_status ON property_types(status);

CREATE TABLE "locations"
(
    location_id         UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    parent_id   UUID,
    type        VARCHAR(20) NOT NULL 
        CHECK (type IN ('CITY', 'DISTRICT', 'WARD')),
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(50),
    north_lat   NUMERIC(9,6) NOT NULL,
    south_lat   NUMERIC(9,6) NOT NULL,
    east_lng    NUMERIC(9,6) NOT NULL,
    west_lng    NUMERIC(9,6) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted     BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (parent_id) REFERENCES "locations" (location_id)
);

CREATE INDEX idx_location_parent ON "locations"(parent_id);
CREATE INDEX idx_location_type ON "locations"(type);
CREATE INDEX idx_location_code ON "locations"(code);

CREATE TABLE "properties"
(
    property_id         UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    owner_id         UUID NOT NULL,
    location_id      UUID NOT NULL,
    property_type_id UUID NOT NULL,
    street_address   VARCHAR(255) NOT NULL,
    latitude         NUMERIC(9,6) NOT NULL,
    longitude        NUMERIC(9,6) NOT NULL,
    land_size_m2     NUMERIC(10,2),
    usable_size_m2   NUMERIC(10,2),
    width_m          NUMERIC(6,2),
    length_m         NUMERIC(6,2),
    status           VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    descriptions     TEXT,
    slug             VARCHAR(255) UNIQUE,
    extra_attributes JSONB,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted          BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (location_id)     REFERENCES "locations" (location_id),
    FOREIGN KEY (property_type_id) REFERENCES property_types (property_type_id),

    CONSTRAINT chk_property_status 
        CHECK (status IN ('DRAFT', 'AVAILABLE', 'RESERVED', 'SOLD'))
);

CREATE INDEX idx_property_owner ON "properties"(owner_id);
CREATE INDEX idx_property_location ON "properties"(location_id);
CREATE INDEX idx_property_type ON "properties"(property_type_id);
CREATE INDEX idx_property_status ON "properties"(status);

CREATE TABLE "listings"
(
    listing_id         UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    property_id    UUID NOT NULL,
    user_id        UUID NOT NULL,
    listing_type   VARCHAR(20) NOT NULL CHECK (listing_type IN ('SALE', 'RENT')),
    status         VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    price          NUMERIC(14,2) NOT NULL,
    min_price      NUMERIC(14,2),
    max_price      NUMERIC(14,2),
    is_negotiable  BOOLEAN NOT NULL DEFAULT FALSE,
    available_from DATE,
    published_at   TIMESTAMP,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted        BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (property_id) REFERENCES "properties" (property_id),

    CONSTRAINT chk_listing_status 
        CHECK (status IN ('DRAFT', 'PENDING', 'PUBLISHED', 'SOLD', 'RENTED', 'EXPIRED'))
);

CREATE INDEX idx_listing_property ON "listings"(property_id);
CREATE INDEX idx_listing_user ON "listings"(user_id);
CREATE INDEX idx_listing_type ON "listings"(listing_type);
CREATE INDEX idx_listing_status ON "listings"(status);
CREATE INDEX idx_listing_published ON "listings"(published_at);
