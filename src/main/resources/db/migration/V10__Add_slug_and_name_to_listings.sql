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