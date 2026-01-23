-- V7__Insert_sample_users.sql
-- Insert sample users for testing authentication
-- Compatible with both PostgreSQL and H2 databases
-- Password for all users: 'Password123' (BCrypt encoded)

-- Admin user
INSERT INTO "users" (user_id, first_name, last_name, business_name, password_hash, email, phone, status, created_at, updated_at, deleted)
VALUES (
    '550e8400-e29b-41d4-a716-446655440001',
    'Admin',
    'User',
    'RealVista Admin',
    '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2',
    'admin@realvista.com',
    '+84901000001',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Regular active user
INSERT INTO "users" (user_id, first_name, last_name, business_name, password_hash, email, phone, status, created_at, updated_at, deleted)
VALUES (
    '550e8400-e29b-41d4-a716-446655440002',
    'John',
    'Doe',
    'John Doe Properties',
    '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2',
    'user@realvista.com',
    '+84901000002',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Verified user
INSERT INTO "users" (user_id, first_name, last_name, business_name, password_hash, email, phone, status, created_at, updated_at, deleted)
VALUES (
    '550e8400-e29b-41d4-a716-446655440003',
    'Jane',
    'Smith',
    'Jane Smith Realty',
    '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2',
    'verified@realvista.com',
    '+84901000003',
    'VERIFIED',
    NOW(),
    NOW(),
    FALSE
);

-- Suspended user (for testing suspended account login)
INSERT INTO "users" (user_id, first_name, last_name, business_name, password_hash, email, phone, status, created_at, updated_at, deleted)
VALUES (
    '550e8400-e29b-41d4-a716-446655440004',
    'Bob',
    'Wilson',
    'Wilson Estates',
    '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2',
    'suspended@realvista.com',
    '+84901000004',
    'SUSPENDED',
    NOW(),
    NOW(),
    FALSE
);

-- Banned user (for testing banned account login)
INSERT INTO "users" (user_id, first_name, last_name, business_name, password_hash, email, phone, status, created_at, updated_at, deleted)
VALUES (
    '550e8400-e29b-41d4-a716-446655440005',
    'Charlie',
    'Brown',
    'Brown Housing',
    '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2',
    'banned@realvista.com',
    '+84901000005',
    'BANNED',
    NOW(),
    NOW(),
    FALSE
);

-- Assign roles to users
-- Admin gets ADMIN role
INSERT INTO "user_roles" (user_id, role_id, assigned_at, created_at, updated_at, deleted)
SELECT 
    '550e8400-e29b-41d4-a716-446655440001',
    role_id,
    NOW(),
    NOW(),
    NOW(),
    FALSE
FROM "roles" WHERE role_code = 'ADMIN';

-- Regular user gets BUYER role
INSERT INTO "user_roles" (user_id, role_id, assigned_at, created_at, updated_at, deleted)
SELECT 
    '550e8400-e29b-41d4-a716-446655440002',
    role_id,
    NOW(),
    NOW(),
    NOW(),
    FALSE
FROM "roles" WHERE role_code = 'BUYER';

-- Verified user gets AGENT role
INSERT INTO "user_roles" (user_id, role_id, assigned_at, created_at, updated_at, deleted)
SELECT 
    '550e8400-e29b-41d4-a716-446655440003',
    role_id,
    NOW(),
    NOW(),
    NOW(),
    FALSE
FROM "roles" WHERE role_code = 'AGENT';

-- Suspended user gets OWNER role
INSERT INTO "user_roles" (user_id, role_id, assigned_at, created_at, updated_at, deleted)
SELECT 
    '550e8400-e29b-41d4-a716-446655440004',
    role_id,
    NOW(),
    NOW(),
    NOW(),
    FALSE
FROM "roles" WHERE role_code = 'OWNER';

-- Banned user gets TENANT role
INSERT INTO "user_roles" (user_id, role_id, assigned_at, created_at, updated_at, deleted)
SELECT 
    '550e8400-e29b-41d4-a716-446655440005',
    role_id,
    NOW(),
    NOW(),
    NOW(),
    FALSE
FROM "roles" WHERE role_code = 'TENANT';
