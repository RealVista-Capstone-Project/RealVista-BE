-- V31__Insert_property_attribute_values.sql
-- Migration V31: Insert logical property attribute values for different property types
-- Creates realistic attributes based on property type (e.g., apartments have floors and bedrooms, land doesn't have floors)

-- ============================================================================
-- RESIDENTIAL PROPERTIES - APARTMENT
-- ============================================================================
-- Apartments: bedrooms, bathrooms, floor, total_floors, balcony, direction, area, AC
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT 
    p.property_id,
    pa.property_attribute_id,
    CASE 
        WHEN pa.code = 'BEDROOMS' THEN (1 + RANDOM() * 4)::integer -- 1-5 bedrooms
        WHEN pa.code = 'BATHROOMS' THEN (1 + RANDOM() * 2)::integer -- 1-3 bathrooms  
        WHEN pa.code = 'FLOOR' THEN (1 + RANDOM() * 39)::integer -- Floor 1-40
        WHEN pa.code = 'TOTAL_FLOORS' THEN (20 + RANDOM() * 20)::integer -- 20-40 total floors
        ELSE NULL
    END as value_number,
    CASE 
        WHEN pa.code = 'DIRECTION' THEN 
            CASE (RANDOM() * 8)::integer
                WHEN 0 THEN 'Đông'
                WHEN 1 THEN 'Tây' 
                WHEN 2 THEN 'Nam'
                WHEN 3 THEN 'Bắc'
                WHEN 4 THEN 'Đông Nam'
                WHEN 5 THEN 'Đông Bắc'
                WHEN 6 THEN 'Tây Nam'
                ELSE 'Tây Bắc'
            END
        ELSE NULL
    END as value_text,
    CASE 
        WHEN pa.code = 'BALCONY' THEN RANDOM() > 0.2 -- 80% have balcony
        WHEN pa.code = 'AC' THEN RANDOM() > 0.1 -- 90% have AC
        ELSE NULL
    END as value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('BEDROOMS', 'BATHROOMS', 'FLOOR', 'TOTAL_FLOORS', 'BALCONY', 'DIRECTION', 'AC')
WHERE pt.code = 'APARTMENT';

-- ============================================================================
-- RESIDENTIAL PROPERTIES - HOUSE, VILLA, TOWNHOUSE
-- ============================================================================
-- Houses/Villas/Townhouses: bedrooms, bathrooms, floors, garden, garage, direction, parking
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT 
    p.property_id,
    pa.property_attribute_id,
    CASE 
        WHEN pa.code = 'BEDROOMS' THEN 
            CASE pt.code
                WHEN 'VILLA' THEN (3 + RANDOM() * 4)::integer -- 3-7 bedrooms
                WHEN 'HOUSE' THEN (2 + RANDOM() * 3)::integer -- 2-5 bedrooms
                ELSE (2 + RANDOM() * 3)::integer -- 2-5 bedrooms
            END
        WHEN pa.code = 'BATHROOMS' THEN (2 + RANDOM() * 3)::integer -- 2-5 bathrooms
        WHEN pa.code = 'FLOORS' THEN (2 + RANDOM() * 2)::integer -- 2-4 floors
        WHEN pa.code = 'PARKING' THEN (1 + RANDOM() * 3)::integer -- 1-4 parking spots
        ELSE NULL
    END as value_number,
    CASE 
        WHEN pa.code = 'DIRECTION' THEN 
            CASE (RANDOM() * 8)::integer
                WHEN 0 THEN 'Đông'
                WHEN 1 THEN 'Tây'
                WHEN 2 THEN 'Nam'
                WHEN 3 THEN 'Bắc'
                WHEN 4 THEN 'Đông Nam'
                WHEN 5 THEN 'Đông Bắc'
                WHEN 6 THEN 'Tây Nam'
                ELSE 'Tây Bắc'
            END
        ELSE NULL
    END as value_text,
    CASE 
        WHEN pa.code = 'GARDEN' THEN 
            CASE pt.code
                WHEN 'VILLA' THEN RANDOM() > 0.2 -- 80% have garden
                WHEN 'HOUSE' THEN RANDOM() > 0.5 -- 50% have garden
                ELSE RANDOM() > 0.6 -- 40% have garden
            END
        WHEN pa.code = 'GARAGE' THEN RANDOM() > 0.3 -- 70% have garage
        WHEN pa.code = 'AC' THEN RANDOM() > 0.2 -- 80% have AC
        ELSE NULL
    END as value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('BEDROOMS', 'BATHROOMS', 'FLOORS', 'GARDEN', 'GARAGE', 'DIRECTION', 'PARKING', 'AC')
WHERE pt.code IN ('HOUSE', 'VILLA', 'TOWNHOUSE');

-- ============================================================================
-- RESIDENTIAL PROPERTIES - PENTHOUSE, STUDIO
-- ============================================================================
-- Penthouse/Studio: bedrooms, bathrooms, floor (high floors), balcony, direction, AC
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT 
    p.property_id,
    pa.property_attribute_id,
    CASE 
        WHEN pa.code = 'BEDROOMS' THEN 
            CASE pt.code
                WHEN 'PENTHOUSE' THEN (2 + RANDOM() * 3)::integer -- 2-5 bedrooms
                WHEN 'STUDIO' THEN 1 -- 1 bedroom (studio)
            END
        WHEN pa.code = 'BATHROOMS' THEN 
            CASE pt.code
                WHEN 'PENTHOUSE' THEN (2 + RANDOM() * 2)::integer -- 2-4 bathrooms
                WHEN 'STUDIO' THEN 1 -- 1 bathroom
            END
        WHEN pa.code = 'FLOOR' THEN 
            CASE pt.code
                WHEN 'PENTHOUSE' THEN (25 + RANDOM() * 15)::integer -- Top floors 25-40
                WHEN 'STUDIO' THEN (1 + RANDOM() * 20)::integer -- 1-21 floors
            END
        WHEN pa.code = 'TOTAL_FLOORS' THEN (30 + RANDOM() * 10)::integer -- 30-40 total floors
        ELSE NULL
    END as value_number,
    CASE 
        WHEN pa.code = 'DIRECTION' THEN 
            CASE (RANDOM() * 8)::integer
                WHEN 0 THEN 'Đông'
                WHEN 1 THEN 'Tây'
                WHEN 2 THEN 'Nam'
                WHEN 3 THEN 'Bắc'
                WHEN 4 THEN 'Đông Nam'
                WHEN 5 THEN 'Đông Bắc'
                WHEN 6 THEN 'Tây Nam'
                ELSE 'Tây Bắc'
            END
        ELSE NULL
    END as value_text,
    CASE 
        WHEN pa.code = 'BALCONY' THEN RANDOM() > 0.1 -- 90% have balcony
        WHEN pa.code = 'LARGE_BALCONY' AND pt.code = 'PENTHOUSE' THEN RANDOM() > 0.3 -- 70% penthouses have large balcony
        WHEN pa.code = 'AC' THEN RANDOM() > 0.05 -- 95% have AC
        ELSE NULL
    END as value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('BEDROOMS', 'BATHROOMS', 'FLOOR', 'TOTAL_FLOORS', 'BALCONY', 'LARGE_BALCONY', 'DIRECTION', 'AC')
WHERE pt.code IN ('PENTHOUSE', 'STUDIO');

-- ============================================================================
-- COMMERCIAL PROPERTIES - OFFICE
-- ============================================================================
-- Offices: floor, parking, AC, elevators, meeting_rooms
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT 
    p.property_id,
    pa.property_attribute_id,
    CASE 
        WHEN pa.code = 'FLOOR' THEN (1 + RANDOM() * 30)::integer -- Floor 1-31
        WHEN pa.code = 'PARKING' THEN (5 + RANDOM() * 20)::integer -- 5-25 parking spots
        WHEN pa.code = 'MEETING_ROOMS' THEN (1 + RANDOM() * 5)::integer -- 1-6 meeting rooms
        ELSE NULL
    END as value_number,
    NULL as value_text,
    CASE 
        WHEN pa.code = 'AC' THEN RANDOM() > 0.05 -- 95% have AC
        WHEN pa.code = 'ELEVATOR' THEN RANDOM() > 0.2 -- 80% have elevator
        ELSE NULL
    END as value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('FLOOR', 'PARKING', 'MEETING_ROOMS', 'AC', 'ELEVATOR')
WHERE pt.code = 'OFFICE';

-- ============================================================================
-- COMMERCIAL PROPERTIES - RETAIL, RESTAURANT, SHOPHOUSE
-- ============================================================================
-- Retail/Restaurant/Shophouse: floor, parking, kitchen (restaurant), display_window (retail)
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT 
    p.property_id,
    pa.property_attribute_id,
    CASE 
        WHEN pa.code = 'FLOORS' THEN 
            CASE pt.code
                WHEN 'SHOPHOUSE' THEN (2 + RANDOM() * 2)::integer -- 2-4 floors
                ELSE (1 + RANDOM() * 1)::integer -- 1-2 floors
            END
        WHEN pa.code = 'PARKING' THEN (2 + RANDOM() * 8)::integer -- 2-10 parking spots
        WHEN pa.code = 'TABLES' AND pt.code = 'RESTAURANT' THEN (10 + RANDOM() * 40)::integer -- 10-50 tables
        ELSE NULL
    END as value_number,
    NULL as value_text,
    CASE 
        WHEN pa.code = 'RESTAURANT_KITCHEN' AND pt.code = 'RESTAURANT' THEN RANDOM() > 0.1 -- 90% restaurants have kitchen
        WHEN pa.code = 'DISPLAY_WINDOW' AND pt.code = 'RETAIL' THEN RANDOM() > 0.2 -- 80% retail have display window
        WHEN pa.code = 'AC' THEN RANDOM() > 0.3 -- 70% have AC
        ELSE NULL
    END as value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('FLOORS', 'PARKING', 'TABLES', 'RESTAURANT_KITCHEN', 'DISPLAY_WINDOW', 'AC')
WHERE pt.code IN ('RETAIL', 'RESTAURANT', 'SHOPHOUSE');

-- ============================================================================
-- HOSPITALITY - HOTEL
-- ============================================================================
-- Hotels: floors, rooms, parking, restaurant, pool, gym
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT 
    p.property_id,
    pa.property_attribute_id,
    CASE 
        WHEN pa.code = 'FLOORS' THEN (5 + RANDOM() * 15)::integer -- 5-20 floors
        WHEN pa.code = 'ROOMS' THEN (20 + RANDOM() * 180)::integer -- 20-200 rooms
        WHEN pa.code = 'PARKING' THEN (10 + RANDOM() * 90)::integer -- 10-100 parking spots
        ELSE NULL
    END as value_number,
    NULL as value_text,
    CASE 
        WHEN pa.code = 'RESTAURANT_KITCHEN' THEN RANDOM() > 0.2 -- 80% have restaurant
        WHEN pa.code = 'POOL' THEN RANDOM() > 0.4 -- 60% have pool
        WHEN pa.code = 'GYM' THEN RANDOM() > 0.5 -- 50% have gym
        WHEN pa.code = 'AC' THEN RANDOM() > 0.05 -- 95% have AC
        ELSE NULL
    END as value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('FLOORS', 'ROOMS', 'PARKING', 'RESTAURANT_KITCHEN', 'POOL', 'GYM', 'AC')
WHERE pt.code = 'HOTEL';

-- ============================================================================
-- INDUSTRIAL PROPERTIES - FACTORY, WAREHOUSE, WORKSHOP
-- ============================================================================
-- Industrial: height, width, depth, truck_parking, loading_docks, crane
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT 
    p.property_id,
    pa.property_attribute_id,
    CASE 
        WHEN pa.code = 'HEIGHT' THEN (4 + RANDOM() * 8)::numeric(5,1) -- 4-12m height
        WHEN pa.code = 'WIDTH' THEN (20 + RANDOM() * 80)::numeric(6,1) -- 20-100m width
        WHEN pa.code = 'DEPTH' THEN (30 + RANDOM() * 120)::numeric(6,1) -- 30-150m depth
        WHEN pa.code = 'TRUCK_PARKING' THEN (2 + RANDOM() * 8)::integer -- 2-10 truck spots
        WHEN pa.code = 'LOADING_DOCKS' THEN (1 + RANDOM() * 4)::integer -- 1-5 loading docks
        ELSE NULL
    END as value_number,
    NULL as value_text,
    CASE 
        WHEN pa.code = 'CRANE' AND pt.code IN ('FACTORY', 'WAREHOUSE') THEN RANDOM() > 0.6 -- 40% have crane
        WHEN pa.code = 'SECURITY' THEN RANDOM() > 0.3 -- 70% have security
        ELSE NULL
    END as value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('HEIGHT', 'WIDTH', 'DEPTH', 'TRUCK_PARKING', 'LOADING_DOCKS', 'CRANE', 'SECURITY')
WHERE pt.code IN ('FACTORY', 'WAREHOUSE', 'WORKSHOP');

-- ============================================================================
-- COMMERCIAL LARGE - MALL, LOGISTICS
-- ============================================================================
-- Mall/Logistics: floors, shops, parking, elevators, loading_docks
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT 
    p.property_id,
    pa.property_attribute_id,
    CASE 
        WHEN pa.code = 'FLOORS' AND pt.code = 'MALL' THEN (3 + RANDOM() * 4)::integer -- 3-7 floors
        WHEN pa.code = 'SHOPS' AND pt.code = 'MALL' THEN (50 + RANDOM() * 200)::integer -- 50-250 shops
        WHEN pa.code = 'PARKING' THEN 
            CASE pt.code
                WHEN 'MALL' THEN (200 + RANDOM() * 800)::integer -- 200-1000 parking spots
                WHEN 'LOGISTICS' THEN (20 + RANDOM() * 80)::integer -- 20-100 spots
            END
        WHEN pa.code = 'LOADING_DOCKS' AND pt.code = 'LOGISTICS' THEN (5 + RANDOM() * 15)::integer -- 5-20 docks
        WHEN pa.code = 'TRUCK_PARKING' AND pt.code = 'LOGISTICS' THEN (10 + RANDOM() * 40)::integer -- 10-50 truck spots
        ELSE NULL
    END as value_number,
    NULL as value_text,
    CASE 
        WHEN pa.code = 'ELEVATOR' THEN RANDOM() > 0.1 -- 90% have elevators
        WHEN pa.code = 'AC' THEN RANDOM() > 0.1 -- 90% have AC
        WHEN pa.code = 'SECURITY' THEN RANDOM() > 0.2 -- 80% have security
        ELSE NULL
    END as value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('FLOORS', 'SHOPS', 'PARKING', 'LOADING_DOCKS', 'TRUCK_PARKING', 'ELEVATOR', 'AC', 'SECURITY')
WHERE pt.code IN ('MALL', 'LOGISTICS');

-- ============================================================================
-- LAND PROPERTIES - NO BUILDING ATTRIBUTES
-- ============================================================================
-- Land: only size-related attributes, no floors, bedrooms, etc.
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT 
    p.property_id,
    pa.property_attribute_id,
    CASE 
        WHEN pa.code = 'WIDTH' THEN (10 + RANDOM() * 40)::numeric(6,1) -- 10-50m width
        WHEN pa.code = 'DEPTH' THEN (15 + RANDOM() * 85)::numeric(6,1) -- 15-100m depth
        ELSE NULL
    END as value_number,
    CASE 
        WHEN pa.code = 'DIRECTION' THEN 
            CASE (RANDOM() * 8)::integer
                WHEN 0 THEN 'Đông'
                WHEN 1 THEN 'Tây'
                WHEN 2 THEN 'Nam'
                WHEN 3 THEN 'Bắc'
                WHEN 4 THEN 'Đông Nam'
                WHEN 5 THEN 'Đông Bắc'
                WHEN 6 THEN 'Tây Nam'
                ELSE 'Tây Bắc'
            END
        ELSE NULL
    END as value_text,
    NULL as value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('WIDTH', 'DEPTH', 'DIRECTION')
WHERE pt.code IN ('LAND_RESIDENTIAL', 'LAND_COMMERCIAL', 'LAND_INDUSTRIAL', 'LAND_AGRICULTURAL');