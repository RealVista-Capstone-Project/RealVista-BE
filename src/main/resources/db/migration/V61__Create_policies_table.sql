-- V60__Create_policies_table.sql
-- Creates the policies table for managing system policies like Terms of Service, Privacy Policy, etc.

CREATE TABLE policies
(
    policy_id  UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    slug       VARCHAR(255) NOT NULL UNIQUE,
    content    TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted    BOOLEAN               DEFAULT FALSE
);

CREATE INDEX idx_policy_slug ON policies (slug);
