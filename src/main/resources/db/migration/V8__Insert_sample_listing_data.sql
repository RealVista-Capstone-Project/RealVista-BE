-- V8__Insert_sample_listing_data.sql
-- Insert sample data for listings testing
-- Compatible with both PostgreSQL and H2 databases (using standard SQL date literals)

-- ============================================================================
-- STEP 1: INSERT LOCATIONS (Ho Chi Minh City hierarchy)
-- ============================================================================

-- City: Ho Chi Minh City
INSERT INTO "locations" (location_id, parent_id, type, name, code, north_lat, south_lat, east_lng, west_lng, deleted)
VALUES ('110e8400-e29b-41d4-a716-446655440100', NULL, 'CITY', 'Ho Chi Minh City', 'HCM', 11.000000, 10.000000, 107.000000, 106.000000, FALSE);

-- District 1
INSERT INTO "locations" (location_id, parent_id, type, name, code, north_lat, south_lat, east_lng, west_lng, deleted) VALUES ('110e8400-e29b-41d4-a716-446655440101', '110e8400-e29b-41d4-a716-446655440100', 'DISTRICT', 'District 1', 'D1', 10.780000, 10.760000, 106.700000, 106.680000, FALSE);

-- District 3
INSERT INTO "locations" (location_id, parent_id, type, name, code, north_lat, south_lat, east_lng, west_lng, deleted)
VALUES (
    '110e8400-e29b-41d4-a716-446655440102',
    '110e8400-e29b-41d4-a716-446655440100',
    'DISTRICT',
    'District 3',
    'D3',
    10.790000,
    10.770000,
    106.690000,
    106.670000,
    FALSE
);

-- Ward 1 in District 1
INSERT INTO "locations" (location_id, parent_id, type, name, code, north_lat, south_lat, east_lng, west_lng, deleted) VALUES ('110e8400-e29b-41d4-a716-446655440201', '110e8400-e29b-41d4-a716-446655440101', 'WARD', 'Ward 1', 'W1', 10.775000, 10.765000, 106.695000, 106.685000, FALSE);

INSERT INTO "locations" (location_id, parent_id, type, name, code, north_lat, south_lat, east_lng, west_lng, deleted) VALUES ('110e8400-e29b-41d4-a716-446655440202', '110e8400-e29b-41d4-a716-446655440101', 'WARD', 'Ward 2', 'W2', 10.770000, 10.760000, 106.690000, 106.680000, FALSE);

-- Ward 3 in District 1
INSERT INTO "locations" (location_id, parent_id, type, name, code, north_lat, south_lat, east_lng, west_lng, deleted)
VALUES (
    '110e8400-e29b-41d4-a716-446655440203',
    '110e8400-e29b-41d4-a716-446655440101',
    'WARD',
    'Ward 3',
    'W3',
    10.770000,
    10.760000,
    106.690000,
    106.680000,
    FALSE
);

-- ============================================================================
-- STEP 2: INSERT PROPERTY CATEGORIES
-- ============================================================================

INSERT INTO property_categories (property_category_id, name, code, created_at, updated_at, deleted)
VALUES
    ('210e8400-e29b-41d4-a716-446655440001', 'Residential', 'RES', NOW(), NOW(), FALSE),
    ('210e8400-e29b-41d4-a716-446655440002', 'Commercial', 'COM', NOW(), NOW(), FALSE),
    ('210e8400-e29b-41d4-a716-446655440003', 'Industrial', 'IND', NOW(), NOW(), FALSE);

-- ============================================================================
-- STEP 3: INSERT PROPERTY TYPES
-- ============================================================================

-- Apartment (Residential)
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '310e8400-e29b-41d4-a716-446655440001',
    '210e8400-e29b-41d4-a716-446655440001',
    'Apartment',
    'APT',
    'Modern apartment in high-rise building',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- House (Residential)
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '310e8400-e29b-41d4-a716-446655440002',
    '210e8400-e29b-41d4-a716-446655440001',
    'House',
    'HOUSE',
    'Detached or semi-detached house',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Villa (Residential)
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '310e8400-e29b-41d4-a716-446655440003',
    '210e8400-e29b-41d4-a716-446655440001',
    'Villa',
    'VILLA',
    'Luxury villa with private garden',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Office (Commercial)
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '310e8400-e29b-41d4-a716-446655440004',
    '210e8400-e29b-41d4-a716-446655440002',
    'Office',
    'OFFICE',
    'Commercial office space',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- ============================================================================
-- STEP 4: INSERT PROPERTIES
-- ============================================================================

-- Property 1: Luxury Apartment in District 1
INSERT INTO "properties" (
    property_id,
    owner_id,
    location_id,
    property_type_id,
    street_address,
    latitude,
    longitude,
    land_size_m2,
    usable_size_m2,
    width_m,
    length_m,
    status,
    descriptions,
    slug,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '410e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440003', -- Jane Smith (AGENT)
    '110e8400-e29b-41d4-a716-446655440201', -- Ward 1, District 1
    '310e8400-e29b-41d4-a716-446655440001', -- Apartment
    '123 Nguyen Hue Street, Ben Nghe Ward',
    10.776389,
    106.701944,
    100.50,
    85.00,
    10.00,
    8.50,
    'AVAILABLE',
    'Luxury 2-bedroom apartment in the heart of Ho Chi Minh City. Modern design with full amenities including swimming pool, gym, and 24/7 security. Close to shopping centers, restaurants, and public transportation.',
    'luxury-apartment-nguyen-hue',
    NOW(),
    NOW(),
    FALSE
);

-- Property 2: Modern House in District 3
INSERT INTO "properties" (
    property_id,
    owner_id,
    location_id,
    property_type_id,
    street_address,
    latitude,
    longitude,
    land_size_m2,
    usable_size_m2,
    width_m,
    length_m,
    status,
    descriptions,
    slug,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '410e8400-e29b-41d4-a716-446655440002',
    '550e8400-e29b-41d4-a716-446655440004', -- Bob Wilson (OWNER)
    '110e8400-e29b-41d4-a716-446655440102', -- District 3
    '310e8400-e29b-41d4-a716-446655440002', -- House
    '456 Vo Van Tan Street, Ward 6',
    10.780500,
    106.685000,
    120.00,
    150.00,
    12.00,
    10.00,
    'AVAILABLE',
    'Beautiful 3-story house with spacious living areas, 4 bedrooms, and a rooftop garden. Recently renovated with modern kitchen and bathrooms. Located in a quiet neighborhood with easy access to city center.',
    'modern-house-vo-van-tan',
    NOW(),
    NOW(),
    FALSE
);

-- Property 3: Villa in District 1
INSERT INTO "properties" (
    property_id,
    owner_id,
    location_id,
    property_type_id,
    street_address,
    latitude,
    longitude,
    land_size_m2,
    usable_size_m2,
    width_m,
    length_m,
    status,
    descriptions,
    slug,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '410e8400-e29b-41d4-a716-446655440003',
    '550e8400-e29b-41d4-a716-446655440003', -- Jane Smith (AGENT)
    '110e8400-e29b-41d4-a716-446655440202', -- Ward 2, District 1
    '310e8400-e29b-41d4-a716-446655440003', -- Villa
    '789 Hai Ba Trung Street, Da Kao Ward',
    10.770000,
    106.692000,
    300.00,
    450.00,
    20.00,
    15.00,
    'AVAILABLE',
    'Exclusive luxury villa featuring 5 bedrooms, private swimming pool, landscaped garden, and smart home automation system. Perfect for families seeking premium living space in prime location.',
    'luxury-villa-hai-ba-trung',
    NOW(),
    NOW(),
    FALSE
);

-- ============================================================================
-- STEP 5: INSERT PROPERTY MEDIAS
-- ============================================================================

-- Media for Property 1 (Apartment)
-- Primary Image
INSERT INTO property_medias (
    property_media_id,
    property_id,
    upload_by,
    media_type,
    media_url,
    thumbnail_url,
    is_primary,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '510e8400-e29b-41d4-a716-446655440001',
    '410e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440003',
    'IMAGE',
    'https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=800',
    'https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=200',
    TRUE,
    NOW(),
    NOW(),
    FALSE
);

-- Second Image
INSERT INTO property_medias (
    property_media_id,
    property_id,
    upload_by,
    media_type,
    media_url,
    thumbnail_url,
    is_primary,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '510e8400-e29b-41d4-a716-446655440002',
    '410e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440003',
    'IMAGE',
    'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800',
    'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=200',
    FALSE,
    NOW(),
    NOW(),
    FALSE
);

-- Video Tour
INSERT INTO property_medias (
    property_media_id,
    property_id,
    upload_by,
    media_type,
    media_url,
    thumbnail_url,
    is_primary,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '510e8400-e29b-41d4-a716-446655440003',
    '410e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440003',
    'VIDEO',
    'https://sample-videos.com/video123/mp4-720p.mp4',
    'https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=200',
    FALSE,
    NOW(),
    NOW(),
    FALSE
);

-- 3D Tour
INSERT INTO property_medias (
    property_media_id,
    property_id,
    upload_by,
    media_type,
    media_url,
    thumbnail_url,
    is_primary,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '510e8400-e29b-41d4-a716-446655440004',
    '410e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440003',
    '3D',
    'https://my.matterport.com/show/?m=sample3dtour1',
    'https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=200',
    FALSE,
    NOW(),
    NOW(),
    FALSE
);

-- Media for Property 2 (House)
INSERT INTO property_medias (
    property_media_id,
    property_id,
    upload_by,
    media_type,
    media_url,
    thumbnail_url,
    is_primary,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '510e8400-e29b-41d4-a716-446655440005',
    '410e8400-e29b-41d4-a716-446655440002',
    '550e8400-e29b-41d4-a716-446655440004',
    'IMAGE',
    'https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=800',
    'https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=200',
    TRUE,
    NOW(),
    NOW(),
    FALSE
);

-- Media for Property 3 (Villa)
INSERT INTO property_medias (
    property_media_id,
    property_id,
    upload_by,
    media_type,
    media_url,
    thumbnail_url,
    is_primary,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '510e8400-e29b-41d4-a716-446655440006',
    '410e8400-e29b-41d4-a716-446655440003',
    '550e8400-e29b-41d4-a716-446655440003',
    'IMAGE',
    'https://images.unsplash.com/photo-1613490493576-7fde63acd811?w=800',
    'https://images.unsplash.com/photo-1613490493576-7fde63acd811?w=200',
    TRUE,
    NOW(),
    NOW(),
    FALSE
);

INSERT INTO property_medias (
    property_media_id,
    property_id,
    upload_by,
    media_type,
    media_url,
    thumbnail_url,
    is_primary,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '510e8400-e29b-41d4-a716-446655440007',
    '410e8400-e29b-41d4-a716-446655440003',
    '550e8400-e29b-41d4-a716-446655440003',
    'IMAGE',
    'https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=800',
    'https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=200',
    FALSE,
    NOW(),
    NOW(),
    FALSE
);

-- ============================================================================
-- STEP 6: INSERT LISTINGS
-- ============================================================================

-- Listing 1: Apartment for rent
INSERT INTO "listings" (
    listing_id,
    property_id,
    user_id,
    listing_type,
    status,
    price,
    min_price,
    max_price,
    is_negotiable,
    available_from,
    published_at,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '610e8400-e29b-41d4-a716-446655440001',
    '410e8400-e29b-41d4-a716-446655440001', -- Luxury Apartment
    '550e8400-e29b-41d4-a716-446655440003', -- Jane Smith (AGENT)
    'RENT',
    'PUBLISHED',
    2700.00,
    2500.00,
    3000.00,
    TRUE,
    CAST('2026-02-03' AS DATE),
    NOW(),
    NOW(),
    NOW(),
    FALSE
);

-- Listing 2: House for sale
INSERT INTO "listings" (
    listing_id,
    property_id,
    user_id,
    listing_type,
    status,
    price,
    min_price,
    max_price,
    is_negotiable,
    available_from,
    published_at,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '610e8400-e29b-41d4-a716-446655440002',
    '410e8400-e29b-41d4-a716-446655440002', -- Modern House
    '550e8400-e29b-41d4-a716-446655440004', -- Bob Wilson (OWNER)
    'SALE',
    'PUBLISHED',
    5500000.00,
    5000000.00,
    6000000.00,
    FALSE,
    CAST('2026-02-26' AS DATE),
    NOW(),
    NOW(),
    NOW(),
    FALSE
);

-- Listing 3: Villa for rent
INSERT INTO "listings" (
    listing_id,
    property_id,
    user_id,
    listing_type,
    status,
    price,
    min_price,
    max_price,
    is_negotiable,
    available_from,
    published_at,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '610e8400-e29b-41d4-a716-446655440003',
    '410e8400-e29b-41d4-a716-446655440003', -- Luxury Villa
    '550e8400-e29b-41d4-a716-446655440003', -- Jane Smith (AGENT)
    'RENT',
    'PUBLISHED',
    8500.00,
    8000.00,
    9000.00,
    TRUE,
    CAST('2026-02-10' AS DATE),
    NOW(),
    NOW(),
    NOW(),
    FALSE
);

-- ============================================================================
-- STEP 7: INSERT LISTING MEDIAS (associations)
-- ============================================================================

-- Listing 1 Medias
INSERT INTO listing_medias (
    listing_media_id,
    listing_id,
    property_media_id,
    display_order,
    is_primary,
    created_at,
    updated_at,
    deleted
)
VALUES
    ('710e8400-e29b-41d4-a716-446655440001', '610e8400-e29b-41d4-a716-446655440001', '510e8400-e29b-41d4-a716-446655440001', 1, TRUE, NOW(), NOW(), FALSE),
    ('710e8400-e29b-41d4-a716-446655440002', '610e8400-e29b-41d4-a716-446655440001', '510e8400-e29b-41d4-a716-446655440002', 2, FALSE, NOW(), NOW(), FALSE),
    ('710e8400-e29b-41d4-a716-446655440003', '610e8400-e29b-41d4-a716-446655440001', '510e8400-e29b-41d4-a716-446655440003', 3, FALSE, NOW(), NOW(), FALSE),
    ('710e8400-e29b-41d4-a716-446655440004', '610e8400-e29b-41d4-a716-446655440001', '510e8400-e29b-41d4-a716-446655440004', 4, FALSE, NOW(), NOW(), FALSE);

-- Listing 2 Medias
INSERT INTO listing_medias (
    listing_media_id,
    listing_id,
    property_media_id,
    display_order,
    is_primary,
    created_at,
    updated_at,
    deleted
)
VALUES
    ('710e8400-e29b-41d4-a716-446655440005', '610e8400-e29b-41d4-a716-446655440002', '510e8400-e29b-41d4-a716-446655440005', 1, TRUE, NOW(), NOW(), FALSE);

-- Listing 3 Medias
INSERT INTO listing_medias (
    listing_media_id,
    listing_id,
    property_media_id,
    display_order,
    is_primary,
    created_at,
    updated_at,
    deleted
)
VALUES
    ('710e8400-e29b-41d4-a716-446655440006', '610e8400-e29b-41d4-a716-446655440003', '510e8400-e29b-41d4-a716-446655440006', 1, TRUE, NOW(), NOW(), FALSE),
    ('710e8400-e29b-41d4-a716-446655440007', '610e8400-e29b-41d4-a716-446655440003', '510e8400-e29b-41d4-a716-446655440007', 2, FALSE, NOW(), NOW(), FALSE);
