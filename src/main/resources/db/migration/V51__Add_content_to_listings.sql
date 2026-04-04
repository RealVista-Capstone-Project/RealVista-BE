-- V51__Add_content_to_listings.sql
-- Add content column to listings table
-- Compatible with both PostgreSQL and H2 databases

-- Add content column (for long-form listing descriptions/articles)
ALTER TABLE listings ADD COLUMN content TEXT;

-- No cascade failure risks as we are only adding a nullable column.
