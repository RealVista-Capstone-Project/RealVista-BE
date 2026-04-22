-- V105__Strip_redundant_prefixes_from_text_attribute_range_labels.sql
--
-- Remove redundant prefix words (Hướng, Nhìn Ra, Đường, Điện, Quy Hoạch) from
-- TEXT-type attribute range labels. These prefixes are already implied by the
-- attribute name itself (e.g. attribute "Hướng nhà" + label "Hướng Bắc" →
-- duplicates "Hướng").
--
-- Context: property_attribute_values.value_text for DIRECTION stores "Bắc",
-- "Tây", "Đông Bắc", ... (see V30), but range labels were seeded as
-- "Hướng Bắc", "Hướng Tây", "Hướng Đông Bắc". Frontend sends range.label as the
-- dropdown value, so filter "Hướng Tây" never matched stored "Tây".
--
-- Using REGEXP_REPLACE with anchored pattern (^) so only leading prefix is
-- stripped. Compatible with both PostgreSQL and H2.

-- ============================================================================
-- DIRECTION (410e8400-e29b-41d4-a716-446655440004)
-- "Hướng Bắc" → "Bắc", "Hướng Tây Bắc" → "Tây Bắc", ...
-- ============================================================================
UPDATE property_attribute_ranges
SET label = REGEXP_REPLACE(label, '^Hướng ', '')
WHERE property_attribute_id = '410e8400-e29b-41d4-a716-446655440004'
  AND label LIKE 'Hướng %';

-- ============================================================================
-- BALCONY_TYPE (410e8400-e29b-41d4-a716-446655440011)
-- "Hướng Bắc" → "Bắc", ... ("Đa Hướng" is left untouched)
-- ============================================================================
UPDATE property_attribute_ranges
SET label = REGEXP_REPLACE(label, '^Hướng ', '')
WHERE property_attribute_id = '410e8400-e29b-41d4-a716-446655440011'
  AND label LIKE 'Hướng %';

-- ============================================================================
-- VIEW (410e8400-e29b-41d4-a716-446655440018)
-- "Nhìn Ra Biển" → "Biển", "Nhìn Ra Thành Phố" → "Thành Phố", ...
-- ============================================================================
UPDATE property_attribute_ranges
SET label = REGEXP_REPLACE(label, '^Nhìn Ra ', '')
WHERE property_attribute_id = '410e8400-e29b-41d4-a716-446655440018'
  AND label LIKE 'Nhìn Ra %';

-- ============================================================================
-- POWER (410e8400-e29b-41d4-a716-446655440206)
-- "Điện Yếu" → "Yếu", "Điện Trung Bình" → "Trung Bình", "Điện Mạnh" → "Mạnh"
-- ============================================================================
UPDATE property_attribute_ranges
SET label = REGEXP_REPLACE(label, '^Điện ', '')
WHERE property_attribute_id = '410e8400-e29b-41d4-a716-446655440206'
  AND label LIKE 'Điện %';

-- ============================================================================
-- PLANNING (410e8400-e29b-41d4-a716-446655440304)
-- "Quy Hoạch Sẵn" → "Sẵn", "Quy Hoạch Giới Hạn" → "Giới Hạn", ...
-- ============================================================================
UPDATE property_attribute_ranges
SET label = REGEXP_REPLACE(label, '^Quy Hoạch ', '')
WHERE property_attribute_id = '410e8400-e29b-41d4-a716-446655440304'
  AND label LIKE 'Quy Hoạch %';

-- ============================================================================
-- ACCESS_ROAD (410e8400-e29b-41d4-a716-446655440311)
-- "Đường Yếu" → "Yếu", "Đường Trung Bình" → "Trung Bình", ...
-- ============================================================================
UPDATE property_attribute_ranges
SET label = REGEXP_REPLACE(label, '^Đường ', '')
WHERE property_attribute_id = '410e8400-e29b-41d4-a716-446655440311'
  AND label LIKE 'Đường %';

-- ============================================================================
-- Backfill: strip the same prefixes from any existing property_attribute_values
-- that were stored through the FE (it persists range.label as value_text).
-- This guarantees stored values match the new shorter labels going forward.
-- ============================================================================
UPDATE property_attribute_values
SET value_text = REGEXP_REPLACE(value_text, '^Hướng ', '')
WHERE property_attribute_id IN (
    '410e8400-e29b-41d4-a716-446655440004',  -- DIRECTION
    '410e8400-e29b-41d4-a716-446655440011'   -- BALCONY_TYPE
)
  AND value_text LIKE 'Hướng %';

UPDATE property_attribute_values
SET value_text = REGEXP_REPLACE(value_text, '^Nhìn Ra ', '')
WHERE property_attribute_id = '410e8400-e29b-41d4-a716-446655440018'  -- VIEW
  AND value_text LIKE 'Nhìn Ra %';

UPDATE property_attribute_values
SET value_text = REGEXP_REPLACE(value_text, '^Điện ', '')
WHERE property_attribute_id = '410e8400-e29b-41d4-a716-446655440206'  -- POWER
  AND value_text LIKE 'Điện %';

UPDATE property_attribute_values
SET value_text = REGEXP_REPLACE(value_text, '^Quy Hoạch ', '')
WHERE property_attribute_id = '410e8400-e29b-41d4-a716-446655440304'  -- PLANNING
  AND value_text LIKE 'Quy Hoạch %';

UPDATE property_attribute_values
SET value_text = REGEXP_REPLACE(value_text, '^Đường ', '')
WHERE property_attribute_id = '410e8400-e29b-41d4-a716-446655440311'  -- ACCESS_ROAD
  AND value_text LIKE 'Đường %';

-- ============================================================================
-- Keep search_value (V104) in sync. After stripping prefixes, label already
-- equals the stored value, so search_value becomes redundant. Null it out so
-- the FE/BE always falls back to the label.
-- ============================================================================
UPDATE property_attribute_ranges
SET search_value = NULL
WHERE property_attribute_id IN (
    '410e8400-e29b-41d4-a716-446655440004',  -- DIRECTION
    '410e8400-e29b-41d4-a716-446655440011'   -- BALCONY_TYPE
);
