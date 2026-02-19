-- Add composite spatial index for map bounding box queries
-- This significantly improves performance for latitude/longitude range queries
CREATE INDEX idx_property_coordinates ON properties(latitude, longitude)
WHERE deleted = false;

-- Add price index on listings for future price range filtering
CREATE INDEX idx_listing_price ON listings(price)
WHERE status = 'PUBLISHED' AND deleted = false;

-- Add composite index for common listing queries
CREATE INDEX idx_listing_status_type ON listings(status, listing_type, published_at DESC)
WHERE deleted = false;

-- Add comment for documentation
COMMENT ON INDEX idx_property_coordinates IS 'Composite spatial index for efficient map bounding box queries on property coordinates';
COMMENT ON INDEX idx_listing_price IS 'Partial index for published listings price filtering';
COMMENT ON INDEX idx_listing_status_type IS 'Composite index for listing search with status and type filtering';
