-- Set published_at to 3 minutes before the scheduler runs this migration,
-- so the listing is just about to expire (within the 14-day window but
-- useful for testing the lifetime countdown showing hours/minutes).
-- Actual value: NOW() - 14 days + 3 minutes  →  expires in ~3 minutes from seed time.
UPDATE listings
SET published_at = NOW() - INTERVAL '14 days' + INTERVAL '3 minutes'
WHERE listing_id = (SELECT listing_id FROM listings WHERE property_id = 'a1100000-0000-0000-0000-000000000010' LIMIT 1);
