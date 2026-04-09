-- V62: Restore property_id in lease_agreements (undo V61 revert)
-- The entity maps property_id → properties, so the DB must match.

DO $$
BEGIN
    -- Only run if listing_id still exists (V61 was applied)
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'lease_agreements' AND column_name = 'listing_id') THEN

        -- Add property_id column if missing
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = 'lease_agreements' AND column_name = 'property_id') THEN
            ALTER TABLE lease_agreements ADD COLUMN property_id UUID;
        END IF;

        -- Backfill property_id from the associated listing
        UPDATE lease_agreements la
        SET property_id = (
            SELECT l.property_id
            FROM listings l
            WHERE l.listing_id = la.listing_id
            LIMIT 1
        );

        -- Make it NOT NULL
        ALTER TABLE lease_agreements ALTER COLUMN property_id SET NOT NULL;

        -- Add FK constraint to properties if not already present
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_lease_property') THEN
            ALTER TABLE lease_agreements
                ADD CONSTRAINT fk_lease_property
                    FOREIGN KEY (property_id) REFERENCES properties (property_id);
        END IF;

        -- Create index if missing
        IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_lease_property') THEN
            CREATE INDEX idx_lease_property ON lease_agreements (property_id);
        END IF;

        -- Drop listing_id FK and column
        DROP INDEX IF EXISTS idx_lease_listing;
        ALTER TABLE lease_agreements DROP CONSTRAINT IF EXISTS fk_lease_listing;
        ALTER TABLE lease_agreements DROP CONSTRAINT IF EXISTS lease_agreements_listing_id_fkey;
        ALTER TABLE lease_agreements DROP COLUMN listing_id;

    END IF;
END $$;
