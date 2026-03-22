-- V41__Add_priority_to_property_attribute_values.sql
-- Adds a priority column to property_type_attributes.
-- For each property type, required attributes are numbered first (1, 2, 3, ...),
-- then non-required attributes continue after (both groups ordered by attribute code).

-- ============================================================================
-- ADD COLUMN
-- ============================================================================

ALTER TABLE property_type_attributes
    ADD COLUMN priority INTEGER NOT NULL DEFAULT 0;

-- ============================================================================
-- POPULATE PRIORITY PER PROPERTY TYPE
-- Required attributes: 1, 2, 3, ... (ordered by attribute code)
-- Non-required attributes: continue after required (ordered by attribute code)
-- ============================================================================

UPDATE property_type_attributes pta
SET priority = ranked.rn
FROM (
    SELECT
        pta2.property_type_attribute_id,
        ROW_NUMBER() OVER (
            PARTITION BY pta2.property_type_id
            ORDER BY
                -- Required attributes first (0 = required, 1 = not required → ASC puts 0 first)
                CASE WHEN pta2.is_required = TRUE THEN 0 ELSE 1 END,
                pa.code
        ) AS rn
    FROM property_type_attributes pta2
    JOIN property_attributes pa
        ON pa.property_attribute_id = pta2.property_attribute_id
    WHERE pta2.deleted = FALSE
) ranked
WHERE pta.property_type_attribute_id = ranked.property_type_attribute_id;

-- ============================================================================
-- INDEX
-- ============================================================================

CREATE INDEX idx_property_type_attr_priority ON property_type_attributes (property_type_id, priority);
