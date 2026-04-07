-- V59: Revert lease_agreements back to listing_id
-- The LeaseAgreement entity references listings, not properties.
-- This migration is made idempotent to handle environments where V3 might have been modified.

DO $$
BEGIN
    -- Only proceed if property_id exists (indicating we are in the "old" state)
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'lease_agreements' 
               AND column_name = 'property_id') THEN
        
        -- Step 1: Delete lease agreements whose property has no associated listing (orphaned records)
        DELETE FROM lease_agreements
        WHERE property_id NOT IN (SELECT DISTINCT property_id FROM listings WHERE property_id IS NOT NULL);

        -- Step 2: Add listing_id column if it doesn't exist
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                       WHERE table_name = 'lease_agreements' 
                       AND column_name = 'listing_id') THEN
            ALTER TABLE lease_agreements ADD COLUMN listing_id UUID;
        END IF;

        -- Step 3: Backfill listing_id from the listings table (pick the first/oldest listing per property)
        UPDATE lease_agreements la
        SET listing_id = (
            SELECT l.listing_id
            FROM listings l
            WHERE l.property_id = la.property_id
            ORDER BY l.created_at
            LIMIT 1
        );

        -- Step 4: Make listing_id NOT NULL if it isn't already
        ALTER TABLE lease_agreements ALTER COLUMN listing_id SET NOT NULL;

        -- Step 5: Add FK constraint to listings if it doesn't exist
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_lease_listing') THEN
            ALTER TABLE lease_agreements
                ADD CONSTRAINT fk_lease_listing
                FOREIGN KEY (listing_id) REFERENCES listings (listing_id);
        END IF;

        -- Step 6: Drop old index and FK on property_id
        DROP INDEX IF EXISTS idx_lease_property;
        ALTER TABLE lease_agreements DROP CONSTRAINT IF EXISTS fk_lease_property;

        -- Step 7: Drop property_id column
        ALTER TABLE lease_agreements DROP COLUMN property_id;

        -- Step 8: Create index on listing_id if it doesn't exist
        IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_lease_listing') THEN
            CREATE INDEX idx_lease_listing ON lease_agreements (listing_id);
        END IF;
    END IF;
END $$;

