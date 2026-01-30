-- V9__Insert_property_attributes_data.sql
-- Insert property attribute definitions and values for sample properties
-- Compatible with both PostgreSQL and H2 databases

-- ============================================================================
-- STEP 1: INSERT PROPERTY ATTRIBUTE DEFINITIONS
-- ============================================================================

-- Bedrooms attribute
INSERT INTO property_attributes (
    property_attribute_id,
    name,
    code,
    data_type,
    is_searchable,
    icon,
    unit,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '810e8400-e29b-41d4-a716-446655440001',
    'Bedrooms',
    'bedrooms',
    'NUMBER',
    TRUE,
    'bed',
    'rooms',
    NOW(),
    NOW(),
    FALSE
);

-- Bathrooms attribute
INSERT INTO property_attributes (
    property_attribute_id,
    name,
    code,
    data_type,
    is_searchable,
    icon,
    unit,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '810e8400-e29b-41d4-a716-446655440002',
    'Bathrooms',
    'bathrooms',
    'NUMBER',
    TRUE,
    'bath',
    'rooms',
    NOW(),
    NOW(),
    FALSE
);

-- Parking attribute
INSERT INTO property_attributes (
    property_attribute_id,
    name,
    code,
    data_type,
    is_searchable,
    icon,
    unit,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '810e8400-e29b-41d4-a716-446655440003',
    'Parking',
    'parking',
    'BOOLEAN',
    TRUE,
    'car',
    NULL,
    NOW(),
    NOW(),
    FALSE
);

-- Furnished attribute
INSERT INTO property_attributes (
    property_attribute_id,
    name,
    code,
    data_type,
    is_searchable,
    icon,
    unit,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '810e8400-e29b-41d4-a716-446655440004',
    'Furnished',
    'furnished',
    'BOOLEAN',
    TRUE,
    'sofa',
    NULL,
    NOW(),
    NOW(),
    FALSE
);

-- Floor attribute
INSERT INTO property_attributes (
    property_attribute_id,
    name,
    code,
    data_type,
    is_searchable,
    icon,
    unit,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '810e8400-e29b-41d4-a716-446655440005',
    'Floor',
    'floor',
    'NUMBER',
    TRUE,
    'layers',
    'floor',
    NOW(),
    NOW(),
    FALSE
);

-- Total Floors attribute
INSERT INTO property_attributes (
    property_attribute_id,
    name,
    code,
    data_type,
    is_searchable,
    icon,
    unit,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '810e8400-e29b-41d4-a716-446655440006',
    'Total Floors',
    'total_floors',
    'NUMBER',
    FALSE,
    'building',
    'floors',
    NOW(),
    NOW(),
    FALSE
);

-- Swimming Pool attribute
INSERT INTO property_attributes (
    property_attribute_id,
    name,
    code,
    data_type,
    is_searchable,
    icon,
    unit,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '810e8400-e29b-41d4-a716-446655440007',
    'Swimming Pool',
    'swimming_pool',
    'BOOLEAN',
    TRUE,
    'water',
    NULL,
    NOW(),
    NOW(),
    FALSE
);

-- Gym attribute
INSERT INTO property_attributes (
    property_attribute_id,
    name,
    code,
    data_type,
    is_searchable,
    icon,
    unit,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '810e8400-e29b-41d4-a716-446655440008',
    'Gym',
    'gym',
    'BOOLEAN',
    TRUE,
    'fitness',
    NULL,
    NOW(),
    NOW(),
    FALSE
);

-- Security attribute
INSERT INTO property_attributes (
    property_attribute_id,
    name,
    code,
    data_type,
    is_searchable,
    icon,
    unit,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '810e8400-e29b-41d4-a716-446655440009',
    '24/7 Security',
    'security',
    'BOOLEAN',
    TRUE,
    'shield',
    NULL,
    NOW(),
    NOW(),
    FALSE
);

-- Year Built attribute
INSERT INTO property_attributes (
    property_attribute_id,
    name,
    code,
    data_type,
    is_searchable,
    icon,
    unit,
    created_at,
    updated_at,
    deleted
)
VALUES (
    '810e8400-e29b-41d4-a716-446655440010',
    'Year Built',
    'year_built',
    'NUMBER',
    FALSE,
    'calendar',
    'year',
    NOW(),
    NOW(),
    FALSE
);

-- ============================================================================
-- STEP 2: INSERT PROPERTY ATTRIBUTE VALUES FOR PROPERTY 1 (Apartment)
-- ============================================================================

-- Property 1: 410e8400-e29b-41d4-a716-446655440001 (Luxury Apartment)
-- 2 bedrooms, 2 bathrooms, 5th floor, 15 total floors

INSERT INTO property_attribute_values (
    property_attribute_value_id,
    property_id,
    property_attribute_id,
    value_number,
    value_text,
    value_boolean,
    created_at,
    updated_at,
    deleted
)
VALUES
    -- 2 bedrooms
    ('910e8400-e29b-41d4-a716-446655440001', '410e8400-e29b-41d4-a716-446655440001', '810e8400-e29b-41d4-a716-446655440001', 2, NULL, NULL, NOW(), NOW(), FALSE),
    -- 2 bathrooms
    ('910e8400-e29b-41d4-a716-446655440002', '410e8400-e29b-41d4-a716-446655440001', '810e8400-e29b-41d4-a716-446655440002', 2, NULL, NULL, NOW(), NOW(), FALSE),
    -- Has parking
    ('910e8400-e29b-41d4-a716-446655440003', '410e8400-e29b-41d4-a716-446655440001', '810e8400-e29b-41d4-a716-446655440003', NULL, NULL, TRUE, NOW(), NOW(), FALSE),
    -- Furnished
    ('910e8400-e29b-41d4-a716-446655440004', '410e8400-e29b-41d4-a716-446655440001', '810e8400-e29b-41d4-a716-446655440004', NULL, NULL, TRUE, NOW(), NOW(), FALSE),
    -- 5th floor
    ('910e8400-e29b-41d4-a716-446655440005', '410e8400-e29b-41d4-a716-446655440001', '810e8400-e29b-41d4-a716-446655440005', 5, NULL, NULL, NOW(), NOW(), FALSE),
    -- 15 total floors
    ('910e8400-e29b-41d4-a716-446655440006', '410e8400-e29b-41d4-a716-446655440001', '810e8400-e29b-41d4-a716-446655440006', 15, NULL, NULL, NOW(), NOW(), FALSE),
    -- Has swimming pool
    ('910e8400-e29b-41d4-a716-446655440007', '410e8400-e29b-41d4-a716-446655440001', '810e8400-e29b-41d4-a716-446655440007', NULL, NULL, TRUE, NOW(), NOW(), FALSE),
    -- Has gym
    ('910e8400-e29b-41d4-a716-446655440008', '410e8400-e29b-41d4-a716-446655440001', '810e8400-e29b-41d4-a716-446655440008', NULL, NULL, TRUE, NOW(), NOW(), FALSE),
    -- Has 24/7 security
    ('910e8400-e29b-41d4-a716-446655440009', '410e8400-e29b-41d4-a716-446655440001', '810e8400-e29b-41d4-a716-446655440009', NULL, NULL, TRUE, NOW(), NOW(), FALSE),
    -- Built in 2020
    ('910e8400-e29b-41d4-a716-446655440010', '410e8400-e29b-41d4-a716-446655440001', '810e8400-e29b-41d4-a716-446655440010', 2020, NULL, NULL, NOW(), NOW(), FALSE);

-- ============================================================================
-- STEP 3: INSERT PROPERTY ATTRIBUTE VALUES FOR PROPERTY 2 (House)
-- ============================================================================

-- Property 2: 410e8400-e29b-41d4-a716-446655440002 (Modern House)
-- 4 bedrooms, 3 bathrooms, 3 floors

INSERT INTO property_attribute_values (
    property_attribute_value_id,
    property_id,
    property_attribute_id,
    value_number,
    value_text,
    value_boolean,
    created_at,
    updated_at,
    deleted
)
VALUES
    -- 4 bedrooms
    ('910e8400-e29b-41d4-a716-446655440011', '410e8400-e29b-41d4-a716-446655440002', '810e8400-e29b-41d4-a716-446655440001', 4, NULL, NULL, NOW(), NOW(), FALSE),
    -- 3 bathrooms
    ('910e8400-e29b-41d4-a716-446655440012', '410e8400-e29b-41d4-a716-446655440002', '810e8400-e29b-41d4-a716-446655440002', 3, NULL, NULL, NOW(), NOW(), FALSE),
    -- Has parking
    ('910e8400-e29b-41d4-a716-446655440013', '410e8400-e29b-41d4-a716-446655440002', '810e8400-e29b-41d4-a716-446655440003', NULL, NULL, TRUE, NOW(), NOW(), FALSE),
    -- Not furnished
    ('910e8400-e29b-41d4-a716-446655440014', '410e8400-e29b-41d4-a716-446655440002', '810e8400-e29b-41d4-a716-446655440004', NULL, NULL, FALSE, NOW(), NOW(), FALSE),
    -- 3 floors
    ('910e8400-e29b-41d4-a716-446655440016', '410e8400-e29b-41d4-a716-446655440002', '810e8400-e29b-41d4-a716-446655440006', 3, NULL, NULL, NOW(), NOW(), FALSE),
    -- No swimming pool
    ('910e8400-e29b-41d4-a716-446655440017', '410e8400-e29b-41d4-a716-446655440002', '810e8400-e29b-41d4-a716-446655440007', NULL, NULL, FALSE, NOW(), NOW(), FALSE),
    -- No gym
    ('910e8400-e29b-41d4-a716-446655440018', '410e8400-e29b-41d4-a716-446655440002', '810e8400-e29b-41d4-a716-446655440008', NULL, NULL, FALSE, NOW(), NOW(), FALSE),
    -- Has security
    ('910e8400-e29b-41d4-a716-446655440019', '410e8400-e29b-41d4-a716-446655440002', '810e8400-e29b-41d4-a716-446655440009', NULL, NULL, TRUE, NOW(), NOW(), FALSE),
    -- Built in 2018
    ('910e8400-e29b-41d4-a716-446655440020', '410e8400-e29b-41d4-a716-446655440002', '810e8400-e29b-41d4-a716-446655440010', 2018, NULL, NULL, NOW(), NOW(), FALSE);

-- ============================================================================
-- STEP 4: INSERT PROPERTY ATTRIBUTE VALUES FOR PROPERTY 3 (Villa)
-- ============================================================================

-- Property 3: 410e8400-e29b-41d4-a716-446655440003 (Luxury Villa)
-- 5 bedrooms, 4 bathrooms, 2 floors

INSERT INTO property_attribute_values (
    property_attribute_value_id,
    property_id,
    property_attribute_id,
    value_number,
    value_text,
    value_boolean,
    created_at,
    updated_at,
    deleted
)
VALUES
    -- 5 bedrooms
    ('910e8400-e29b-41d4-a716-446655440021', '410e8400-e29b-41d4-a716-446655440003', '810e8400-e29b-41d4-a716-446655440001', 5, NULL, NULL, NOW(), NOW(), FALSE),
    -- 4 bathrooms
    ('910e8400-e29b-41d4-a716-446655440022', '410e8400-e29b-41d4-a716-446655440003', '810e8400-e29b-41d4-a716-446655440002', 4, NULL, NULL, NOW(), NOW(), FALSE),
    -- Has parking
    ('910e8400-e29b-41d4-a716-446655440023', '410e8400-e29b-41d4-a716-446655440003', '810e8400-e29b-41d4-a716-446655440003', NULL, NULL, TRUE, NOW(), NOW(), FALSE),
    -- Fully furnished
    ('910e8400-e29b-41d4-a716-446655440024', '410e8400-e29b-41d4-a716-446655440003', '810e8400-e29b-41d4-a716-446655440004', NULL, NULL, TRUE, NOW(), NOW(), FALSE),
    -- 2 floors
    ('910e8400-e29b-41d4-a716-446655440026', '410e8400-e29b-41d4-a716-446655440003', '810e8400-e29b-41d4-a716-446655440006', 2, NULL, NULL, NOW(), NOW(), FALSE),
    -- Has swimming pool
    ('910e8400-e29b-41d4-a716-446655440027', '410e8400-e29b-41d4-a716-446655440003', '810e8400-e29b-41d4-a716-446655440007', NULL, NULL, TRUE, NOW(), NOW(), FALSE),
    -- Has private gym
    ('910e8400-e29b-41d4-a716-446655440028', '410e8400-e29b-41d4-a716-446655440003', '810e8400-e29b-41d4-a716-446655440008', NULL, NULL, TRUE, NOW(), NOW(), FALSE),
    -- Has 24/7 security
    ('910e8400-e29b-41d4-a716-446655440029', '410e8400-e29b-41d4-a716-446655440003', '810e8400-e29b-41d4-a716-446655440009', NULL, NULL, TRUE, NOW(), NOW(), FALSE),
    -- Built in 2022
    ('910e8400-e29b-41d4-a716-446655440030', '410e8400-e29b-41d4-a716-446655440003', '810e8400-e29b-41d4-a716-446655440010', 2022, NULL, NULL, NOW(), NOW(), FALSE);
