-- V36__Add_index_for_property_attributes.sql
-- Performance: Add GIN index for faster JSONB queries on extra_attributes
-- Note: This index supports queries like: WHERE extra_attributes->>'direction' = 'EAST'

CREATE INDEX IF NOT EXISTS idx_properties_extra_attributes
ON properties USING GIN (extra_attributes);
