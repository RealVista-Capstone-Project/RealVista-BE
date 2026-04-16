-- BLOCK appointments do not belong to any listing, so listing_id must be nullable
ALTER TABLE appointments ALTER COLUMN listing_id DROP NOT NULL;
