-- Set published_at to 3 minutes before the scheduler runs this migration,
-- so the listing is just about to expire (within the 14-day window but
-- useful for testing the lifetime countdown showing hours/minutes).
-- Actual value: NOW() - 14 days + 3 minutes  →  expires in ~3 minutes from seed time.
UPDATE listings
SET published_at = NOW() - INTERVAL '14 days' + INTERVAL '3 minutes'
WHERE listing_id = '71cea53a-bff0-b29b-3a9e-9e041c3d0524';
