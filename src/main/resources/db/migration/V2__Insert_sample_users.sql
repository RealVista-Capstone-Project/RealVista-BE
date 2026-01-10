-- Insert sample users for testing
-- Version: V2__Insert_sample_users.sql
-- Note: Compatible with both PostgreSQL and H2 (test database)

-- Note: Password is 'Password123' hashed with BCrypt
-- You can generate new password hashes using: https://bcrypt-generator.com/

-- Admin user
INSERT INTO users (email, password_hash, first_name, last_name, status, role, created_at, updated_at, deleted)
VALUES ('admin@realvista.com', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'Admin', 'User', 'ACTIVE', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- Regular user
INSERT INTO users (email, password_hash, first_name, last_name, status, role, created_at, updated_at, deleted)
VALUES ('user@realvista.com', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'John', 'Doe', 'ACTIVE', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- Pending user
INSERT INTO users (email, password_hash, first_name, last_name, status, role, created_at, updated_at, deleted)
VALUES ('pending@realvista.com', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'Jane', 'Smith', 'PENDING', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

