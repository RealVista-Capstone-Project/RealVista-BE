-- V8__Insert_bookmark_test_data.sql
-- Insert simple test data for bookmark functionality testing
-- Only creates test users with roles and uses existing sample data

-- Insert test users for bookmark testing (simple version)
INSERT INTO users (user_id, first_name, last_name, business_name, password_hash, email, phone, status, created_at, updated_at, deleted)
VALUES 
-- Buyer user 1
('550e8400-e29b-41d4-a716-446655440010',
 'Alice',
 'Johnson', 
 'Alice Real Estate',
 '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2',
 'buyer1@realvista.com',
 '+84901000010', 
 'ACTIVE',
 NOW(),
 NOW(),
 FALSE),

-- Tenant user 1  
('550e8400-e29b-41d4-a716-446655440011',
 'Carol',
 'Davis',
 'Carol Rentals', 
 '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2',
 'tenant1@realvista.com',
 '+84901000011',
 'ACTIVE', 
 NOW(),
 NOW(),
 FALSE),

-- Owner user for properties
('550e8400-e29b-41d4-a716-446655440012',
 'Owner',
 'Test',
 'Test Owner Corp',
 '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2',
 'owner1@realvista.com',
 '+84901000012',
 'ACTIVE',
 NOW(),
 NOW(),
 FALSE);

-- Assign roles to test users
INSERT INTO user_roles (user_id, role_id, created_at, updated_at)
SELECT 
    u.user_id,
    r.role_id,
    NOW(),
    NOW()
FROM users u
CROSS JOIN roles r
WHERE 
    (u.email = 'buyer1@realvista.com' AND r.role_code = 'BUYER') OR
    (u.email = 'tenant1@realvista.com' AND r.role_code = 'TENANT') OR
    (u.email = 'owner1@realvista.com' AND r.role_code = 'OWNER');

-- Insert test property category first
INSERT INTO property_categories (property_category_id, name, code, created_at, updated_at, deleted)
VALUES 
('220e8400-e29b-41d4-a716-446655440001', 'Residential', 'RESIDENTIAL', NOW(), NOW(), FALSE);

-- Insert test property types
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES 
('440e8400-e29b-41d4-a716-446655440001', '220e8400-e29b-41d4-a716-446655440001', 'Apartment', 'APARTMENT', 'Apartment/Condo unit', 'ACTIVE', NOW(), NOW(), FALSE),
('440e8400-e29b-41d4-a716-446655440002', '220e8400-e29b-41d4-a716-446655440001', 'House', 'HOUSE', 'Single family house', 'ACTIVE', NOW(), NOW(), FALSE);

-- Insert test location (Ho Chi Minh City)
INSERT INTO locations (location_id, parent_id, type, name, code, north_lat, south_lat, east_lng, west_lng, created_at, updated_at, deleted)
VALUES 
('330e8400-e29b-41d4-a716-446655440001', NULL, 'CITY', 'Ho Chi Minh City', 'HCMC', 10.9540, 10.5400, 106.8500, 106.6000, NOW(), NOW(), FALSE);

-- Insert simple test properties
INSERT INTO properties (property_id, owner_id, location_id, property_type_id, street_address, latitude, longitude, land_size_m2, usable_size_m2, status, descriptions, created_at, updated_at, deleted)
VALUES
-- Use the test location and property type we just created
('660e8400-e29b-41d4-a716-446655440001',
 '550e8400-e29b-41d4-a716-446655440012',
 '330e8400-e29b-41d4-a716-446655440001',
 '440e8400-e29b-41d4-a716-446655440001',
 '123 Test Street, Ho Chi Minh City',
 10.7729,
 106.7009,
 120.5,
 95.0,
 'AVAILABLE',
 'Beautiful apartment for testing bookmark feature',
 NOW(),
 NOW(),
 FALSE),

('660e8400-e29b-41d4-a716-446655440002',
 '550e8400-e29b-41d4-a716-446655440012',
 '330e8400-e29b-41d4-a716-446655440001',
 '440e8400-e29b-41d4-a716-446655440002',
 '456 Test Avenue, Ho Chi Minh City',
 10.8012,
 106.7103,
 200.0,
 180.0,
 'AVAILABLE',
 'Spacious house for testing bookmark feature',
 NOW(),
 NOW(),
 FALSE);

-- Insert test listings
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, is_negotiable, available_from, published_at, created_at, updated_at, deleted)
VALUES
-- Sale listing
('770e8400-e29b-41d4-a716-446655440001',
 '660e8400-e29b-41d4-a716-446655440001',
 '550e8400-e29b-41d4-a716-446655440012',
 'SALE',
 'PUBLISHED',
 5000000000,
 TRUE,
 NULL,
 NOW(),
 NOW(),
 NOW(),
 FALSE),

-- Rent listing
('770e8400-e29b-41d4-a716-446655440002',
 '660e8400-e29b-41d4-a716-446655440002',
 '550e8400-e29b-41d4-a716-446655440012',
 'RENT',
 'PUBLISHED',
 20000000,
 FALSE,
 NOW(),
 NOW(),
 NOW(),
 NOW(),
 FALSE);