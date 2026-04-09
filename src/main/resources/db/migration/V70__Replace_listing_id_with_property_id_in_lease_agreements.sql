-- V59: Replace listing_id with property_id in lease_agreements table
-- Leases now tie directly to a property instead of a specific listing.

-- 1. Drop existing FK constraint referencing listings
ALTER TABLE lease_agreements
    DROP CONSTRAINT IF EXISTS lease_agreements_listing_id_fkey;

-- 2. Drop the listing index
DROP INDEX IF EXISTS idx_lease_listing;

-- 3. Drop listing_id column
ALTER TABLE lease_agreements
    DROP COLUMN IF EXISTS listing_id;

-- 4. Add property_id column (NOT NULL enforced after backfill; dev only — no data to migrate)
ALTER TABLE lease_agreements
    ADD COLUMN property_id UUID NOT NULL;

-- 5. Add FK constraint referencing properties
ALTER TABLE lease_agreements
    ADD CONSTRAINT fk_lease_property
        FOREIGN KEY (property_id) REFERENCES properties (property_id);

-- 6. Create index for property-based lookups
CREATE INDEX idx_lease_property ON lease_agreements (property_id);
