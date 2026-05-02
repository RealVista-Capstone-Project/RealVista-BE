-- V140__Seed_admin_nttri.sql
-- Seed account nttri.10a1cl2@gmail.com as Admin
-- Password: 'Password123'

-- Insert user if not exists
INSERT INTO users (user_id, first_name, last_name, business_name, password_hash, email, phone, status, created_at, updated_at, deleted)
SELECT 
    '550e8400-e29b-41d4-a716-446655440999',
    'Tri',
    'Nguyen',
    'Tri Nguyen Realty',
    '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2',
    'nttri.10a1cl2@gmail.com',
    '+84900000000',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'nttri.10a1cl2@gmail.com');

-- Update password for existing user
UPDATE users 
SET password_hash = '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2',
    status = 'ACTIVE',
    deleted = FALSE
WHERE email = 'nttri.10a1cl2@gmail.com';

-- Clear existing roles for this user
DELETE FROM user_roles 
WHERE user_id = (SELECT user_id FROM users WHERE email = 'nttri.10a1cl2@gmail.com');

-- Assign ADMIN role
INSERT INTO user_roles (user_id, role_id, assigned_at, created_at, updated_at, deleted)
SELECT 
    (SELECT user_id FROM users WHERE email = 'nttri.10a1cl2@gmail.com'),
    role_id,
    NOW(),
    NOW(),
    NOW(),
    FALSE
FROM roles WHERE role_code = 'ADMIN';
