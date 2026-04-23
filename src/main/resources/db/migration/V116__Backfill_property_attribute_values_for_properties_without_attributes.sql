-- V116__Backfill_property_attribute_values_for_properties_without_attributes.sql
-- Properties inserted in V107__Expand_Properties_And_Align_Media.sql (and any other rows)
-- were created after V30 ran, so they had no property_attribute_values.
-- This migration applies the same per-type attribute seeding logic as V30, only for
-- properties that currently have zero attribute value rows.

-- ============================================================================
-- RESIDENTIAL PROPERTIES - APARTMENT
-- ============================================================================
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT
    p.property_id,
    pa.property_attribute_id,
    CASE
        WHEN pa.code = 'BEDROOMS' THEN (1 + RANDOM() * 4)::integer
        WHEN pa.code = 'BATHROOMS' THEN (1 + RANDOM() * 2)::integer
        WHEN pa.code = 'FLOOR' THEN (1 + RANDOM() * 39)::integer
        WHEN pa.code = 'TOTAL_FLOORS' THEN (20 + RANDOM() * 20)::integer
        ELSE NULL
    END AS value_number,
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
    END AS value_text,
    CASE
        WHEN pa.code = 'BALCONY' THEN RANDOM() > 0.2
        WHEN pa.code = 'AC' THEN RANDOM() > 0.1
        ELSE NULL
    END AS value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('BEDROOMS', 'BATHROOMS', 'FLOOR', 'TOTAL_FLOORS', 'BALCONY', 'DIRECTION', 'AC')
WHERE pt.code = 'APARTMENT'
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_attribute_values e WHERE e.property_id = p.property_id);

-- ============================================================================
-- RESIDENTIAL PROPERTIES - HOUSE, VILLA, TOWNHOUSE
-- ============================================================================
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT
    p.property_id,
    pa.property_attribute_id,
    CASE
        WHEN pa.code = 'BEDROOMS' THEN
            CASE pt.code
                WHEN 'VILLA' THEN (3 + RANDOM() * 4)::integer
                WHEN 'HOUSE' THEN (2 + RANDOM() * 3)::integer
                ELSE (2 + RANDOM() * 3)::integer
            END
        WHEN pa.code = 'BATHROOMS' THEN (2 + RANDOM() * 3)::integer
        WHEN pa.code = 'FLOORS' THEN (2 + RANDOM() * 2)::integer
        WHEN pa.code = 'PARKING' THEN (1 + RANDOM() * 3)::integer
        ELSE NULL
    END AS value_number,
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
    END AS value_text,
    CASE
        WHEN pa.code = 'GARDEN' THEN
            CASE pt.code
                WHEN 'VILLA' THEN RANDOM() > 0.2
                WHEN 'HOUSE' THEN RANDOM() > 0.5
                ELSE RANDOM() > 0.6
            END
        WHEN pa.code = 'GARAGE' THEN RANDOM() > 0.3
        WHEN pa.code = 'AC' THEN RANDOM() > 0.2
        ELSE NULL
    END AS value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('BEDROOMS', 'BATHROOMS', 'FLOORS', 'GARDEN', 'GARAGE', 'DIRECTION', 'PARKING', 'AC')
WHERE pt.code IN ('HOUSE', 'VILLA', 'TOWNHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_attribute_values e WHERE e.property_id = p.property_id);

-- ============================================================================
-- RESIDENTIAL PROPERTIES - PENTHOUSE, STUDIO
-- ============================================================================
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT
    p.property_id,
    pa.property_attribute_id,
    CASE
        WHEN pa.code = 'BEDROOMS' THEN
            CASE pt.code
                WHEN 'PENTHOUSE' THEN (2 + RANDOM() * 3)::integer
                WHEN 'STUDIO' THEN 1
            END
        WHEN pa.code = 'BATHROOMS' THEN
            CASE pt.code
                WHEN 'PENTHOUSE' THEN (2 + RANDOM() * 2)::integer
                WHEN 'STUDIO' THEN 1
            END
        WHEN pa.code = 'FLOOR' THEN
            CASE pt.code
                WHEN 'PENTHOUSE' THEN (25 + RANDOM() * 15)::integer
                WHEN 'STUDIO' THEN (1 + RANDOM() * 20)::integer
            END
        WHEN pa.code = 'TOTAL_FLOORS' THEN (30 + RANDOM() * 10)::integer
        ELSE NULL
    END AS value_number,
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
    END AS value_text,
    CASE
        WHEN pa.code = 'BALCONY' THEN RANDOM() > 0.1
        WHEN pa.code = 'LARGE_BALCONY' AND pt.code = 'PENTHOUSE' THEN RANDOM() > 0.3
        WHEN pa.code = 'AC' THEN RANDOM() > 0.05
        ELSE NULL
    END AS value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('BEDROOMS', 'BATHROOMS', 'FLOOR', 'TOTAL_FLOORS', 'BALCONY', 'LARGE_BALCONY', 'DIRECTION', 'AC')
WHERE pt.code IN ('PENTHOUSE', 'STUDIO')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_attribute_values e WHERE e.property_id = p.property_id);

-- ============================================================================
-- COMMERCIAL PROPERTIES - OFFICE
-- ============================================================================
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT
    p.property_id,
    pa.property_attribute_id,
    CASE
        WHEN pa.code = 'FLOOR' THEN (1 + RANDOM() * 30)::integer
        WHEN pa.code = 'PARKING' THEN (5 + RANDOM() * 20)::integer
        WHEN pa.code = 'MEETING_ROOMS' THEN (1 + RANDOM() * 5)::integer
        ELSE NULL
    END AS value_number,
    NULL AS value_text,
    CASE
        WHEN pa.code = 'AC' THEN RANDOM() > 0.05
        WHEN pa.code = 'ELEVATOR' THEN RANDOM() > 0.2
        ELSE NULL
    END AS value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('FLOOR', 'PARKING', 'MEETING_ROOMS', 'AC', 'ELEVATOR')
WHERE pt.code = 'OFFICE'
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_attribute_values e WHERE e.property_id = p.property_id);

-- ============================================================================
-- COMMERCIAL PROPERTIES - RETAIL, RESTAURANT, SHOPHOUSE
-- ============================================================================
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT
    p.property_id,
    pa.property_attribute_id,
    CASE
        WHEN pa.code = 'FLOORS' THEN
            CASE pt.code
                WHEN 'SHOPHOUSE' THEN (2 + RANDOM() * 2)::integer
                ELSE (1 + RANDOM() * 1)::integer
            END
        WHEN pa.code = 'PARKING' THEN (2 + RANDOM() * 8)::integer
        WHEN pa.code = 'TABLES' AND pt.code = 'RESTAURANT' THEN (10 + RANDOM() * 40)::integer
        ELSE NULL
    END AS value_number,
    NULL AS value_text,
    CASE
        WHEN pa.code = 'RESTAURANT_KITCHEN' AND pt.code = 'RESTAURANT' THEN RANDOM() > 0.1
        WHEN pa.code = 'DISPLAY_WINDOW' AND pt.code = 'RETAIL' THEN RANDOM() > 0.2
        WHEN pa.code = 'AC' THEN RANDOM() > 0.3
        ELSE NULL
    END AS value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('FLOORS', 'PARKING', 'TABLES', 'RESTAURANT_KITCHEN', 'DISPLAY_WINDOW', 'AC')
WHERE pt.code IN ('RETAIL', 'RESTAURANT', 'SHOPHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_attribute_values e WHERE e.property_id = p.property_id);

-- ============================================================================
-- HOSPITALITY - HOTEL
-- ============================================================================
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT
    p.property_id,
    pa.property_attribute_id,
    CASE
        WHEN pa.code = 'FLOORS' THEN (5 + RANDOM() * 15)::integer
        WHEN pa.code = 'ROOMS' THEN (20 + RANDOM() * 180)::integer
        WHEN pa.code = 'PARKING' THEN (10 + RANDOM() * 90)::integer
        ELSE NULL
    END AS value_number,
    NULL AS value_text,
    CASE
        WHEN pa.code = 'RESTAURANT_KITCHEN' THEN RANDOM() > 0.2
        WHEN pa.code = 'POOL' THEN RANDOM() > 0.4
        WHEN pa.code = 'GYM' THEN RANDOM() > 0.5
        WHEN pa.code = 'AC' THEN RANDOM() > 0.05
        ELSE NULL
    END AS value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('FLOORS', 'ROOMS', 'PARKING', 'RESTAURANT_KITCHEN', 'POOL', 'GYM', 'AC')
WHERE pt.code = 'HOTEL'
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_attribute_values e WHERE e.property_id = p.property_id);

-- ============================================================================
-- INDUSTRIAL PROPERTIES - FACTORY, WAREHOUSE, WORKSHOP
-- ============================================================================
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT
    p.property_id,
    pa.property_attribute_id,
    CASE
        WHEN pa.code = 'HEIGHT' THEN (4 + RANDOM() * 8)::numeric(5, 1)
        WHEN pa.code = 'WIDTH' THEN (20 + RANDOM() * 80)::numeric(6, 1)
        WHEN pa.code = 'DEPTH' THEN (30 + RANDOM() * 120)::numeric(6, 1)
        WHEN pa.code = 'TRUCK_PARKING' THEN (2 + RANDOM() * 8)::integer
        WHEN pa.code = 'LOADING_DOCKS' THEN (1 + RANDOM() * 4)::integer
        ELSE NULL
    END AS value_number,
    NULL AS value_text,
    CASE
        WHEN pa.code = 'CRANE' AND pt.code IN ('FACTORY', 'WAREHOUSE') THEN RANDOM() > 0.6
        WHEN pa.code = 'SECURITY' THEN RANDOM() > 0.3
        ELSE NULL
    END AS value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('HEIGHT', 'WIDTH', 'DEPTH', 'TRUCK_PARKING', 'LOADING_DOCKS', 'CRANE', 'SECURITY')
WHERE pt.code IN ('FACTORY', 'WAREHOUSE', 'WORKSHOP')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_attribute_values e WHERE e.property_id = p.property_id);

-- ============================================================================
-- COMMERCIAL LARGE - MALL, LOGISTICS
-- ============================================================================
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT
    p.property_id,
    pa.property_attribute_id,
    CASE
        WHEN pa.code = 'FLOORS' AND pt.code = 'MALL' THEN (3 + RANDOM() * 4)::integer
        WHEN pa.code = 'SHOPS' AND pt.code = 'MALL' THEN (50 + RANDOM() * 200)::integer
        WHEN pa.code = 'PARKING' THEN
            CASE pt.code
                WHEN 'MALL' THEN (200 + RANDOM() * 800)::integer
                WHEN 'LOGISTICS' THEN (20 + RANDOM() * 80)::integer
            END
        WHEN pa.code = 'LOADING_DOCKS' AND pt.code = 'LOGISTICS' THEN (5 + RANDOM() * 15)::integer
        WHEN pa.code = 'TRUCK_PARKING' AND pt.code = 'LOGISTICS' THEN (10 + RANDOM() * 40)::integer
        ELSE NULL
    END AS value_number,
    NULL AS value_text,
    CASE
        WHEN pa.code = 'ELEVATOR' THEN RANDOM() > 0.1
        WHEN pa.code = 'AC' THEN RANDOM() > 0.1
        WHEN pa.code = 'SECURITY' THEN RANDOM() > 0.2
        ELSE NULL
    END AS value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('FLOORS', 'SHOPS', 'PARKING', 'LOADING_DOCKS', 'TRUCK_PARKING', 'ELEVATOR', 'AC', 'SECURITY')
WHERE pt.code IN ('MALL', 'LOGISTICS')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_attribute_values e WHERE e.property_id = p.property_id);

-- ============================================================================
-- LAND PROPERTIES
-- ============================================================================
INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT
    p.property_id,
    pa.property_attribute_id,
    CASE
        WHEN pa.code = 'WIDTH' THEN (10 + RANDOM() * 40)::numeric(6, 1)
        WHEN pa.code = 'DEPTH' THEN (15 + RANDOM() * 85)::numeric(6, 1)
        ELSE NULL
    END AS value_number,
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
    END AS value_text,
    NULL AS value_boolean
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN property_attributes pa ON pa.code IN ('WIDTH', 'DEPTH', 'DIRECTION')
WHERE pt.code IN ('LAND_RESIDENTIAL', 'LAND_COMMERCIAL', 'LAND_INDUSTRIAL', 'LAND_AGRICULTURAL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_attribute_values e WHERE e.property_id = p.property_id);
