-- Initial schema: Create users table
-- Version: V1__Create_users_table.sql
-- Note: Compatible with both PostgreSQL and H2 (test database)

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,  -- Renamed from 'value' (reserved keyword in H2)
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    avatar_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);

-- Create index on status for filtering
CREATE INDEX IF NOT EXISTS idx_user_status ON users(status);

-- Create index on deleted for soft delete queries
CREATE INDEX IF NOT EXISTS idx_user_deleted ON users(deleted);

-- Add constraints
ALTER TABLE users ADD CONSTRAINT chk_user_status 
    CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'INACTIVE'));

ALTER TABLE users ADD CONSTRAINT chk_user_role 
    CHECK (role IN ('USER', 'ADMIN', 'MODERATOR'));

