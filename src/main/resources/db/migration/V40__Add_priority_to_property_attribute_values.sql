-- V40__Add_priority_to_property_attribute_values.sql
-- Adds a priority column to property_attribute_values.
-- For each property, required attributes are numbered first (1, 2, 3, ...),
-- then non-required attributes continue after (both groups ordered by attribute code).

-- ============================================================================
-- ADD COLUMN
-- ============================================================================

ALTER TABLE property_attribute_values
    ADD COLUMN priority INTEGER NOT NULL DEFAULT 0;

-- ============================================================================
-- POPULATE PRIORITY PER PROPERTY
-- Required attributes: 1, 2, 3, ... (ordered by attribute code)
-- Non-required attributes: continue after required (ordered by attribute code)
-- ============================================================================

UPDATE property_attribute_values pav
SET priority = ranked.rn
FROM (
    SELECT
        pav2.property_attribute_value_id,
        ROW_NUMBER() OVER (
            PARTITION BY pav2.property_id
            ORDER BY
                -- Required attributes first (0 = required, 1 = not required → ASC puts 0 first)
                CASE WHEN pta.is_required = TRUE THEN 0 ELSE 1 END,
                pa.code
        ) AS rn
    FROM property_attribute_values pav2
    JOIN property_attributes pa
        ON pa.property_attribute_id = pav2.property_attribute_id
    JOIN properties p
        ON p.property_id = pav2.property_id
    LEFT JOIN property_type_attributes pta
        ON pta.property_attribute_id = pav2.property_attribute_id
        AND pta.property_type_id = p.property_type_id
        AND pta.deleted = FALSE
    WHERE pav2.deleted = FALSE
) ranked
WHERE pav.property_attribute_value_id = ranked.property_attribute_value_id;

-- ============================================================================
-- INDEX
-- ============================================================================

CREATE INDEX idx_property_attr_value_priority ON property_attribute_values (property_id, priority);
