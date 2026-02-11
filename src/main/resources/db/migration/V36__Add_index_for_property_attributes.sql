-- V36__Add_index_for_property_attributes.sql
-- Add GIN index for JSONB extra_attributes column in properties table
-- This drastically improves performance for dynamic attribute filtering

-- Create index if it doesn't exist
CREATE INDEX IF NOT EXISTS idx_properties_extra_attributes 
ON properties USING GIN (extra_attributes);
