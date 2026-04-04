-- V43__Add_listing_analytics_indexes.sql
-- Optimizes queries for listing analytics aggregations
-- Compatible with both PostgreSQL and H2 databases

-- Add composite index for faster analytics queries
-- This index helps when aggregating view counts and counting unique viewers
CREATE INDEX IF NOT EXISTS idx_listing_views_composite
    ON listing_views(listing_id, user_id, deleted)
    WHERE deleted = FALSE;

-- Add index on listing_id for appointments to optimize tour booking counts
-- This speeds up queries that count appointments by listing
CREATE INDEX IF NOT EXISTS idx_appointment_listing_type
    ON appointments(listing_id, appointment_type)
    WHERE deleted = FALSE;
