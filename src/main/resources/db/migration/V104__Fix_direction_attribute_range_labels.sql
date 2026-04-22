-- Add search_value column to property_attribute_ranges
-- Separates display label from the actual value used for searching/filtering
-- If search_value is NULL, frontend falls back to label
ALTER TABLE property_attribute_ranges ADD COLUMN search_value VARCHAR(200);

-- DIRECTION: stored property values use short form ('Bắc', 'Nam', ...)
-- but range labels have 'Hướng ' prefix → set search_value to strip the prefix
UPDATE property_attribute_ranges
SET search_value = REPLACE(label, 'Hướng ', '')
WHERE property_attribute_id IN (
    SELECT property_attribute_id FROM property_attributes WHERE code IN ('DIRECTION')
)
AND label LIKE 'Hướng %';

-- FACADE_DIRECTION (same pattern if it exists)
UPDATE property_attribute_ranges
SET search_value = REPLACE(label, 'Hướng ', '')
WHERE property_attribute_id IN (
    SELECT property_attribute_id FROM property_attributes WHERE code = 'FACADE_DIRECTION'
)
AND label LIKE 'Hướng %';
