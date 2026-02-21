-- V39__Create_tenant_rental_profiles.sql
-- Create tenant rental profiles to act as CV/Templates for renting

CREATE TABLE tenant_rental_profiles
(
    profile_id        UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id           UUID         NOT NULL,
    title             VARCHAR(100) NOT NULL,
    monthly_income    NUMERIC(15, 2),
    move_in_date      DATE,
    lease_term_months INTEGER,
    note              TEXT,
    is_active         BOOLEAN      DEFAULT TRUE,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted           BOOLEAN      DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE INDEX idx_tenant_rental_profile_user ON tenant_rental_profiles (user_id);

ALTER TABLE tenant_applications
ADD COLUMN rental_profile_id UUID;

ALTER TABLE tenant_applications
ADD CONSTRAINT fk_tenant_app_rental_profile FOREIGN KEY (rental_profile_id) REFERENCES tenant_rental_profiles (profile_id) ON DELETE SET NULL;
