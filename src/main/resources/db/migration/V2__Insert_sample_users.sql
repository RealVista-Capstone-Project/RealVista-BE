-- Insert sample users for testing
-- Version: V2__Insert_sample_users.sql
-- Note: Compatible with both PostgreSQL and H2 (test database)

-- Note: Password is 'Password123' hashed with BCrypt
-- You can generate new password hashes using: https://bcrypt-generator.com/

-- Admin user
INSERT INTO users (email, password_hash, first_name, last_name, status, role, created_at, updated_at, deleted)
VALUES ('admin@realvista.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', 'ACTIVE', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- Regular user
INSERT INTO users (email, password_hash, first_name, last_name, status, role, created_at, updated_at, deleted)
VALUES ('user@realvista.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John', 'Doe', 'ACTIVE', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- Pending user
INSERT INTO users (email, password_hash, first_name, last_name, status, role, created_at, updated_at, deleted)
VALUES ('pending@realvista.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane', 'Smith', 'PENDING', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

