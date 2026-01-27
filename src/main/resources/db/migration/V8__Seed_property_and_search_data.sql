-- V8__Seed_property_and_search_data.sql
-- Seed data for testing the Advanced Search API

-- 1. Insert Property Category
INSERT INTO property_categories (property_category_id, name, code)
VALUES ('770e8400-e29b-41d4-a716-446655440001', 'Residential', 'RESIDENTIAL');

-- 2. Insert Property Type
INSERT INTO property_types (property_type_id, property_category_id, name, code, status)
VALUES ('880e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440001', 'Chung cư', 'apartment', 'ACTIVE');

-- 3. Insert Location (HCM City -> Quận 7)
INSERT INTO "locations" (location_id, name, type, north_lat, south_lat, east_lng, west_lng)
VALUES ('990e8400-e29b-41d4-a716-446655440001', 'TP. Hồ Chí Minh', 'CITY', 10.8231, 10.7231, 106.6297, 106.5297);

INSERT INTO "locations" (location_id, parent_id, name, type, north_lat, south_lat, east_lng, west_lng)
VALUES ('990e8400-e29b-41d4-a716-446655440002', '990e8400-e29b-41d4-a716-446655440001', 'Quận 7', 'DISTRICT', 10.7323, 10.7123, 106.7218, 106.7018);

-- 4. Insert Property
INSERT INTO "properties" (property_id, owner_id, location_id, property_type_id, street_address, latitude, longitude, usable_size_m2, status, extra_attributes)
VALUES (
    'aa0e8400-e29b-41d4-a716-446655440001', 
    '550e8400-e29b-41d4-a716-446655440002', -- user@realvista.com
    '990e8400-e29b-41d4-a716-446655440002', -- Quận 7
    '880e8400-e29b-41d4-a716-446655440001', -- apartment
    '15 Nguyễn Lương Bằng, Tân Phú', 
    10.7223, 
    106.7118, 
    85.5, 
    'AVAILABLE',
    '{"bedrooms": 2, "direction": "Đông Nam"}'::jsonb
);

-- 5. Insert Listing
INSERT INTO "listings" (listing_id, property_id, user_id, listing_type, status, price, published_at)
VALUES (
    'bb0e8400-e29b-41d4-a716-446655440001',
    'aa0e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440002',
    'SALE',
    'PUBLISHED',
    7500000000,
    NOW()
);
