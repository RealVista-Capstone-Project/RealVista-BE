-- V76__Add_price_range_to_properties.sql
-- Add price_range column to properties table

ALTER TABLE properties
    ADD COLUMN price_range JSONB;
