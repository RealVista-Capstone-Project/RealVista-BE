-- V60: Revert lease_agreements back to listing_id (undo V59)
-- The LeaseAgreement entity references listings, not properties.

-- Step 1: Delete lease agreements whose property has no associated listing (orphaned records)
DELETE FROM lease_agreements
WHERE property_id NOT IN (SELECT DISTINCT property_id FROM listings WHERE property_id IS NOT NULL);

-- Step 2: Add listing_id column (nullable for backfill)
ALTER TABLE lease_agreements
    ADD COLUMN listing_id UUID;

-- Step 3: Backfill listing_id from the listings table (pick the first/oldest listing per property)
UPDATE lease_agreements la
SET listing_id = (
    SELECT l.listing_id
    FROM listings l
    WHERE l.property_id = la.property_id
    ORDER BY l.created_at
    LIMIT 1
);

-- Step 4: Make listing_id NOT NULL
ALTER TABLE lease_agreements
    ALTER COLUMN listing_id SET NOT NULL;

-- Step 4: Add FK constraint to listings
ALTER TABLE lease_agreements
    ADD CONSTRAINT fk_lease_listing
    FOREIGN KEY (listing_id) REFERENCES listings (listing_id);

-- Step 5: Drop old index and FK on property_id
DROP INDEX IF EXISTS idx_lease_property;
ALTER TABLE lease_agreements
    DROP CONSTRAINT IF EXISTS fk_lease_property;

-- Step 6: Drop property_id column
ALTER TABLE lease_agreements
    DROP COLUMN property_id;

-- Step 7: Create index on listing_id
CREATE INDEX idx_lease_listing ON lease_agreements (listing_id);
