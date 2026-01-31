-- Add bedrooms and bathrooms as separate columns for better query performance
-- These are commonly used filters and should not be in JSONB

ALTER TABLE properties ADD COLUMN bedrooms INTEGER;
ALTER TABLE properties ADD COLUMN bathrooms INTEGER;

-- Migrate existing data from extraAttributes JSONB to new columns
UPDATE properties
SET bedrooms = (extra_attributes->>'bedrooms')::INTEGER
WHERE extra_attributes->>'bedrooms' IS NOT NULL
  AND extra_attributes->>'bedrooms' ~ '^[0-9]+$';

UPDATE properties
SET bathrooms = (extra_attributes->>'bathrooms')::INTEGER
WHERE extra_attributes->>'bathrooms' IS NOT NULL
  AND extra_attributes->>'bathrooms' ~ '^[0-9]+$';

-- Optional: Remove bedrooms/bathrooms from JSONB to avoid duplication
-- UPDATE properties SET extra_attributes = extra_attributes - 'bedrooms' - 'bathrooms';
