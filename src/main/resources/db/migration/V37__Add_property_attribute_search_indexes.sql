-- V42__Add_property_attribute_search_indexes.sql
-- Add composite indexes on property_attribute_values to optimize
-- search queries filtering by bedrooms/bathrooms via the normalized attribute tables.

-- Composite index for searching attribute values by attribute_id + value_number
-- Optimizes subquery: WHERE property_attribute_id = X AND value_number >= Y
CREATE INDEX IF NOT EXISTS idx_pav_attr_value_number
ON property_attribute_values (property_attribute_id, value_number);

-- Composite index for searching by property_id + attribute_id
-- Optimizes JOINs between properties and their attribute values
CREATE INDEX IF NOT EXISTS idx_pav_property_attr
ON property_attribute_values (property_id, property_attribute_id);
