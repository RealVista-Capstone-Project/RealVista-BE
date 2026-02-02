-- V9__Add_unique_constraint_property_fee_services.sql
-- Migration V9: Add UNIQUE constraint to prevent duplicate fee types per property
-- Ensures: A property cannot have two WATER fees, two ELECTRICITY fees, etc.
--
-- This constraint prevents data integrity issues where one property 
-- could have multiple fees of the same type with different prices.
-- Uses partial index to only apply constraint to active (non-deleted) records.

CREATE UNIQUE INDEX uk_property_fee_type 
ON property_fee_services(property_id, fee_type) 
WHERE deleted = FALSE;
