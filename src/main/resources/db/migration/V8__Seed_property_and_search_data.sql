-- V8__Seed_property_and_search_data.sql
-- Seed data for testing the Advanced Search API

-- 1. Insert Property Categories
INSERT INTO property_categories (property_category_id, name, code)
VALUES 
('770e8400-e29b-41d4-a716-446655440001', 'Residential', 'RESIDENTIAL'),
('770e8400-e29b-41d4-a716-446655440002', 'Commercial', 'COMMERCIAL'),
('770e8400-e29b-41d4-a716-446655440003', 'Land', 'LAND');

-- 2. Insert Property Types
INSERT INTO property_types (property_type_id, property_category_id, name, code, status)
VALUES 
('880e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440001', 'Chung cư', 'apartment', 'ACTIVE'),
('880e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440001', 'Biệt thự', 'villa', 'ACTIVE'),
('880e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440003', 'Đất nền', 'land_plot', 'ACTIVE'),
('880e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440002', 'Văn phòng', 'office', 'ACTIVE');

-- 3. Insert Locations (HCM City Districts)
INSERT INTO "locations" (location_id, name, type, north_lat, south_lat, east_lng, west_lng)
VALUES ('990e8400-e29b-41d4-a716-446655440001', 'TP. Hồ Chí Minh', 'CITY', 10.8231, 10.7231, 106.6297, 106.5297);

INSERT INTO "locations" (location_id, parent_id, name, type, north_lat, south_lat, east_lng, west_lng)
VALUES 
('990e8400-e29b-41d4-a716-446655440002', '990e8400-e29b-41d4-a716-446655440001', 'Quận 7', 'DISTRICT', 10.7323, 10.7123, 106.7218, 106.7018),
('990e8400-e29b-41d4-a716-446655440003', '990e8400-e29b-41d4-a716-446655440001', 'Quận 2', 'DISTRICT', 10.7823, 10.7623, 106.7418, 106.7218),
('990e8400-e29b-41d4-a716-446655440004', '990e8400-e29b-41d4-a716-446655440001', 'Bình Thạnh', 'DISTRICT', 10.8023, 10.7823, 106.7018, 106.6818);

-- 4. Insert Properties
-- P1: Apartment in District 7
INSERT INTO "properties" (property_id, owner_id, location_id, property_type_id, street_address, latitude, longitude, usable_size_m2, status, extra_attributes)
VALUES ('aa0e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '990e8400-e29b-41d4-a716-446655440002', '880e8400-e29b-41d4-a716-446655440001', '15 Nguyễn Lương Bằng', 10.7223, 106.7118, 85.5, 'AVAILABLE', '{"bedrooms": 2, "direction": "Đông Nam"}'::jsonb);

-- P2: Villa in District 2
INSERT INTO "properties" (property_id, owner_id, location_id, property_type_id, street_address, latitude, longitude, usable_size_m2, status, extra_attributes)
VALUES ('aa0e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', '990e8400-e29b-41d4-a716-446655440003', '880e8400-e29b-41d4-a716-446655440002', 'Thảo Điền', 10.7723, 106.7318, 350.0, 'AVAILABLE', '{"bedrooms": 5, "bathrooms": 4, "has_pool": true, "direction": "Nam"}'::jsonb);

-- P3: Affordable Studio in Binh Thanh
INSERT INTO "properties" (property_id, owner_id, location_id, property_type_id, street_address, latitude, longitude, usable_size_m2, status, extra_attributes)
VALUES ('aa0e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440002', '990e8400-e29b-41d4-a716-446655440004', '880e8400-e29b-41d4-a716-446655440001', 'Xô Viết Nghệ Tĩnh', 10.7923, 106.6918, 35.0, 'AVAILABLE', '{"bedrooms": 1}'::jsonb);

-- 5. Insert Listings
-- L1: Published Apartment
INSERT INTO "listings" (listing_id, property_id, user_id, listing_type, status, price, published_at)
VALUES ('bb0e8400-e29b-41d4-a716-446655440001', 'aa0e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', 'SALE', 'PUBLISHED', 7500000000, NOW());

-- L2: Published Villa
INSERT INTO "listings" (listing_id, property_id, user_id, listing_type, status, price, published_at)
VALUES ('bb0e8400-e29b-41d4-a716-446655440002', 'aa0e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', 'SALE', 'PUBLISHED', 55000000000, NOW());

-- L3: Draft Listing (Should not appear in search)
INSERT INTO "listings" (listing_id, property_id, user_id, listing_type, status, price, published_at)
VALUES ('bb0e8400-e29b-41d4-a716-446655440003', 'aa0e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440002', 'SALE', 'DRAFT', 2500000000, NULL);
