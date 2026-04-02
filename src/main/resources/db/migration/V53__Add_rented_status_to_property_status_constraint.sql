-- V53__Add_rented_status_to_property_status_constraint.sql
-- Update property status check constraint to include 'RENTED'

ALTER TABLE properties 
DROP CONSTRAINT IF EXISTS chk_property_status;

ALTER TABLE properties 
ADD CONSTRAINT chk_property_status 
CHECK (status IN ('DRAFT', 'PENDING', 'VERIFIED', 'REJECTED', 'AVAILABLE', 'RESERVED', 'SOLD', 'RENTED'));
