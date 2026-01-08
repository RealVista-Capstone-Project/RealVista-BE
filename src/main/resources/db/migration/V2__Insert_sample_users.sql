-- Insert sample users for testing
-- Version: V2__Insert_sample_users.sql
-- Note: Compatible with both PostgreSQL and H2 (test database)

-- Note: Password is 'Password123' hashed with BCrypt
-- You can generate new password hashes using: https://bcrypt-generator.com/

-- Admin user
INSERT INTO users (email, phone, password_hash, first_name, last_name, status, role, email_verified, phone_verified,
                   created_at, updated_at, deleted)
VALUES ('admin@realvista.com', '+1234567890', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin',
        'User', 'ACTIVE', 'ADMIN', TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- Regular user
INSERT INTO users (email, phone, password_hash, first_name, last_name, status, role, email_verified, phone_verified,
                   created_at, updated_at, deleted)
VALUES ('user@realvista.com', '+1234567891', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John',
        'Doe', 'ACTIVE', 'USER', TRUE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- Pending user
INSERT INTO users (email, password_hash, first_name, last_name, status, role, email_verified, phone_verified,
                   created_at, updated_at, deleted)
VALUES ('pending@realvista.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane', 'Smith',
        'PENDING', 'USER', FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

