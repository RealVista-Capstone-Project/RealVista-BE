-- V10__Add_slug_and_name_to_listings.sql
-- Add slug and name fields to listings table
-- Compatible with both PostgreSQL and H2 databases

-- Add slug column (unique, for SEO-friendly URLs)
ALTER TABLE listings ADD COLUMN slug VARCHAR(255);

-- Add name column (human-readable listing title)
ALTER TABLE listings ADD COLUMN name VARCHAR(500);

-- Add unique constraint on slug
ALTER TABLE listings ADD CONSTRAINT uk_listings_slug UNIQUE (slug);

-- Add index for faster lookups by slug
CREATE INDEX idx_listing_slug ON listings(slug);

-- Update existing listings with slug and name values
-- Property 1: Luxury Apartment for rent
UPDATE listings
SET
    slug = 'luxury-2-bedroom-apartment-nguyen-hue',
    name = 'Luxury 2-Bedroom Apartment in District 1'
WHERE listing_id = '610e8400-e29b-41d4-a716-446655440001';

-- Property 2: Modern House for sale
UPDATE listings
SET
    slug = 'modern-3-story-house-vo-van-tan',
    name = 'Modern 3-Story House with Garden'
WHERE listing_id = '610e8400-e29b-41d4-a716-446655440002';

-- Property 3: Luxury Villa for rent
UPDATE listings
SET
    slug = 'luxury-5-bedroom-villa-hai-ba-trung',
    name = 'Luxury 5-Bedroom Villa with Private Pool'
WHERE listing_id = '610e8400-e29b-41d4-a716-446655440003';

-- Make slug column NOT NULL after data has been populated
-- (PostgreSQL and H2 support this syntax)
ALTER TABLE listings ALTER COLUMN slug SET NOT NULL;

-- Make name column NOT NULL after data has been populated
ALTER TABLE listings ALTER COLUMN name SET NOT NULL;
