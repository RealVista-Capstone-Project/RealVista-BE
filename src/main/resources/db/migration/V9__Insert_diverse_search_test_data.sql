-- V9__Insert_diverse_search_test_data.sql
-- Comprehensive sample data for testing Advanced Search API

-- Insert Property Attributes (only if they don't exist)
INSERT INTO property_attributes (property_attribute_id, name, code, data_type, is_searchable, unit)
VALUES 
('cc0e8400-e29b-41d4-a716-446655440001', 'Bedrooms', 'bedrooms', 'NUMBER', true, 'rooms'),
('cc0e8400-e29b-41d4-a716-446655440002', 'Bathrooms', 'bathrooms', 'NUMBER', true, 'rooms'),
('cc0e8400-e29b-41d4-a716-446655440003', 'Floors', 'floors', 'NUMBER', true, 'floors'),
('cc0e8400-e29b-41d4-a716-446655440004', 'Direction', 'direction', 'TEXT', true, null),
('cc0e8400-e29b-41d4-a716-446655440005', 'Balcony Direction', 'balconyDirection', 'TEXT', true, null),
('cc0e8400-e29b-41d4-a716-446655440006', 'Furniture', 'furniture', 'TEXT', true, null),
('cc0e8400-e29b-41d4-a716-446655440007', 'Legal Status', 'legal', 'TEXT', true, null),
('cc0e8400-e29b-41d4-a716-446655440008', 'Elevator', 'elevator', 'BOOLEAN', true, null),
('cc0e8400-e29b-41d4-a716-446655440009', 'Parking', 'parking', 'BOOLEAN', true, null)
ON CONFLICT (property_attribute_id) DO NOTHING;

-- Link Attributes to Property Types (using existing property type IDs from V8)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required)
VALUES 
-- Apartment attributes (property_type_id from V8: 880e8400-e29b-41d4-a716-446655440001)
('dd0e8400-e29b-41d4-a716-446655440001', '880e8400-e29b-41d4-a716-446655440001', 'cc0e8400-e29b-41d4-a716-446655440001', true),
('dd0e8400-e29b-41d4-a716-446655440002', '880e8400-e29b-41d4-a716-446655440001', 'cc0e8400-e29b-41d4-a716-446655440002', true),
('dd0e8400-e29b-41d4-a716-446655440003', '880e8400-e29b-41d4-a716-446655440001', 'cc0e8400-e29b-41d4-a716-446655440003', false),
('dd0e8400-e29b-41d4-a716-446655440004', '880e8400-e29b-41d4-a716-446655440001', 'cc0e8400-e29b-41d4-a716-446655440008', false),
-- Villa attributes (property_type_id from V8: 880e8400-e29b-41d4-a716-446655440002)
('dd0e8400-e29b-41d4-a716-446655440005', '880e8400-e29b-41d4-a716-446655440002', 'cc0e8400-e29b-41d4-a716-446655440001', true),
('dd0e8400-e29b-41d4-a716-446655440006', '880e8400-e29b-41d4-a716-446655440002', 'cc0e8400-e29b-41d4-a716-446655440002', true)
ON CONFLICT (property_type_attribute_id) DO NOTHING;

-- Insert more diverse properties
-- Property 4: Luxury Apartment in District 7 with full furniture
INSERT INTO properties (property_id, owner_id, location_id, property_type_id, street_address, latitude, longitude, 
                        land_size_m2, usable_size_m2, status, extra_attributes)
VALUES ('aa0e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440002', 
        '990e8400-e29b-41d4-a716-446655440002', '880e8400-e29b-41d4-a716-446655440001', 
        '123 Nguyễn Hữu Thọ', 10.7250, 106.7150, 120.0, 110.0, 'AVAILABLE', 
        '{"bedrooms": 3, "bathrooms": 2, "direction": "SOUTH", "balconyDirection": "EAST", "furniture": "FULL", "legal": "RED_BOOK", "floors": 25, "elevator": true, "parking": true}'::jsonb);

-- Property 5: Affordable Apartment for Rent in Binh Thanh
INSERT INTO properties (property_id, owner_id, location_id, property_type_id, street_address, latitude, longitude, 
                        usable_size_m2, status, extra_attributes)
VALUES ('aa0e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440002', 
        '990e8400-e29b-41d4-a716-446655440004', '880e8400-e29b-41d4-a716-446655440001', 
        '456 Điện Biên Phủ', 10.7950, 106.6950, 55.0, 'AVAILABLE', 
        '{"bedrooms": 2, "bathrooms": 1, "direction": "NORTH", "furniture": "BASIC", "legal": "SALE_CONTRACT", "elevator": false}'::jsonb);

-- Property 6: Modern Apartment in District 2
INSERT INTO properties (property_id, owner_id, location_id, property_type_id, street_address, latitude, longitude, 
                        usable_size_m2, status, extra_attributes)
VALUES ('aa0e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440002', 
        '990e8400-e29b-41d4-a716-446655440003', '880e8400-e29b-41d4-a716-446655440001', 
        '789 Mai Chí Thọ', 10.7750, 106.7350, 95.0, 'AVAILABLE', 
        '{"bedrooms": 3, "bathrooms": 2, "direction": "SOUTHEAST", "balconyDirection": "SOUTH", "furniture": "FULL", "legal": "RED_BOOK", "floors": 18, "elevator": true, "parking": true}'::jsonb);

-- Property 7: Villa with Garden in District 2
INSERT INTO properties (property_id, owner_id, location_id, property_type_id, street_address, latitude, longitude, 
                        land_size_m2, usable_size_m2, status, extra_attributes)
VALUES ('aa0e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440002', 
        '990e8400-e29b-41d4-a716-446655440003', '880e8400-e29b-41d4-a716-446655440002', 
        '321 Thảo Điền', 10.7780, 106.7380, 500.0, 400.0, 'AVAILABLE', 
        '{"bedrooms": 4, "bathrooms": 3, "direction": "SOUTH", "furniture": "FULL", "legal": "RED_BOOK", "garden": true, "pool": true}'::jsonb);

-- Property 8: Budget Studio for Rent
INSERT INTO properties (property_id, owner_id, location_id, property_type_id, street_address, latitude, longitude, 
                        usable_size_m2, status, extra_attributes)
VALUES ('aa0e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440002', 
        '990e8400-e29b-41d4-a716-446655440004', '880e8400-e29b-41d4-a716-446655440001', 
        '654 Bạch Đằng', 10.7980, 106.6980, 30.0, 'AVAILABLE', 
        '{"bedrooms": 1, "bathrooms": 1, "direction": "WEST", "furniture": "NONE", "legal": "WAITING_FOR_BOOK"}'::jsonb);

-- Property 9: Office Space in District 7
INSERT INTO properties (property_id, owner_id, location_id, property_type_id, street_address, latitude, longitude, 
                        usable_size_m2, status, extra_attributes)
VALUES ('aa0e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440002', 
        '990e8400-e29b-41d4-a716-446655440002', '880e8400-e29b-41d4-a716-446655440004', 
        '999 Nguyễn Văn Linh', 10.7280, 106.7180, 200.0, 'AVAILABLE', 
        '{"floors": 10, "elevator": true, "parking": true, "legal": "RED_BOOK"}'::jsonb);

-- Property 10: Land Plot in District 7
INSERT INTO properties (property_id, owner_id, location_id, property_type_id, street_address, latitude, longitude, 
                        land_size_m2, status, extra_attributes)
VALUES ('aa0e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440002', 
        '990e8400-e29b-41d4-a716-446655440002', '880e8400-e29b-41d4-a716-446655440003', 
        '111 Huỳnh Tấn Phát', 10.7200, 106.7100, 250.0, 'AVAILABLE', 
        '{"legal": "RED_BOOK", "roadWidth": 12, "cornerLot": true}'::jsonb);

-- Insert Listings for new properties
-- L4: Luxury Apartment for SALE
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, is_negotiable, published_at)
VALUES ('bb0e8400-e29b-41d4-a716-446655440004', 'aa0e8400-e29b-41d4-a716-446655440004', 
        '550e8400-e29b-41d4-a716-446655440002', 'SALE', 'PUBLISHED', 12000000000, true, NOW());

-- L5: Apartment for RENT
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, available_from, published_at)
VALUES ('bb0e8400-e29b-41d4-a716-446655440005', 'aa0e8400-e29b-41d4-a716-446655440005', 
        '550e8400-e29b-41d4-a716-446655440002', 'RENT', 'PUBLISHED', 8000000, '2024-03-01', NOW());

-- L6: Modern Apartment for SALE
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, min_price, max_price, is_negotiable, published_at)
VALUES ('bb0e8400-e29b-41d4-a716-446655440006', 'aa0e8400-e29b-41d4-a716-446655440006', 
        '550e8400-e29b-41d4-a716-446655440002', 'SALE', 'PUBLISHED', 9500000000, 9000000000, 10000000000, true, NOW());

-- L7: Villa for SALE
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, published_at)
VALUES ('bb0e8400-e29b-41d4-a716-446655440007', 'aa0e8400-e29b-41d4-a716-446655440007', 
        '550e8400-e29b-41d4-a716-446655440002', 'SALE', 'PUBLISHED', 75000000000, NOW());

-- L8: Studio for RENT (Available immediately)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, available_from, published_at)
VALUES ('bb0e8400-e29b-41d4-a716-446655440008', 'aa0e8400-e29b-41d4-a716-446655440008', 
        '550e8400-e29b-41d4-a716-446655440002', 'RENT', 'PUBLISHED', 4500000, NOW()::date, NOW());

-- L9: Office for RENT
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, available_from, published_at)
VALUES ('bb0e8400-e29b-41d4-a716-446655440009', 'aa0e8400-e29b-41d4-a716-446655440009', 
        '550e8400-e29b-41d4-a716-446655440002', 'RENT', 'PUBLISHED', 50000000, '2024-02-15', NOW());

-- L10: Land for SALE
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, published_at)
VALUES ('bb0e8400-e29b-41d4-a716-446655440010', 'aa0e8400-e29b-41d4-a716-446655440010', 
        '550e8400-e29b-41d4-a716-446655440002', 'SALE', 'PUBLISHED', 25000000000, NOW());

-- Insert Property Media (for hasVideo and has3D filters)
-- Media for Property 4 (Luxury Apartment) - Has Video and 3D
INSERT INTO property_medias (property_media_id, property_id, upload_by, media_type, media_url, is_primary)
VALUES 
('ee0e8400-e29b-41d4-a716-446655440001', 'aa0e8400-e29b-41d4-a716-446655440004', 
 '550e8400-e29b-41d4-a716-446655440002', 'IMAGE', 'https://example.com/property4/img1.jpg', true),
('ee0e8400-e29b-41d4-a716-446655440002', 'aa0e8400-e29b-41d4-a716-446655440004', 
 '550e8400-e29b-41d4-a716-446655440002', 'VIDEO', 'https://example.com/property4/tour.mp4', false),
('ee0e8400-e29b-41d4-a716-446655440003', 'aa0e8400-e29b-41d4-a716-446655440004', 
 '550e8400-e29b-41d4-a716-446655440002', '3D', 'https://example.com/property4/3d-tour', false);

-- Media for Property 6 (Modern Apartment) - Has Video only
INSERT INTO property_medias (property_media_id, property_id, upload_by, media_type, media_url, is_primary)
VALUES 
('ee0e8400-e29b-41d4-a716-446655440004', 'aa0e8400-e29b-41d4-a716-446655440006', 
 '550e8400-e29b-41d4-a716-446655440002', 'IMAGE', 'https://example.com/property6/img1.jpg', true),
('ee0e8400-e29b-41d4-a716-446655440005', 'aa0e8400-e29b-41d4-a716-446655440006', 
 '550e8400-e29b-41d4-a716-446655440002', 'VIDEO', 'https://example.com/property6/video.mp4', false);

-- Media for Property 7 (Villa) - Has 3D only
INSERT INTO property_medias (property_media_id, property_id, upload_by, media_type, media_url, is_primary)
VALUES 
('ee0e8400-e29b-41d4-a716-446655440006', 'aa0e8400-e29b-41d4-a716-446655440007', 
 '550e8400-e29b-41d4-a716-446655440002', 'IMAGE', 'https://example.com/property7/img1.jpg', true),
('ee0e8400-e29b-41d4-a716-446655440007', 'aa0e8400-e29b-41d4-a716-446655440007', 
 '550e8400-e29b-41d4-a716-446655440002', '3D', 'https://example.com/property7/3d', false);
