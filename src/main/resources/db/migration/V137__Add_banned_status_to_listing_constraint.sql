-- V144__Add_banned_status_to_listing_constraint.sql
-- Add BANNED status to listing status constraint

ALTER TABLE listings DROP CONSTRAINT chk_listing_status;
ALTER TABLE listings ADD CONSTRAINT chk_listing_status 
    CHECK (status IN ('DRAFT', 'PENDING', 'PUBLISHED', 'SOLD', 'RENTED', 'EXPIRED', 'BANNED'));
