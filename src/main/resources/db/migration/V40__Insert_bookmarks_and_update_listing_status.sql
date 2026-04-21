-- V40__Insert_bookmarks_and_update_listing_status.sql
-- Add bookmarks for user 550e8400-e29b-41d4-a716-446655440301
-- Update two listing statuses based on listing_type

-- ============================================================================
-- INSERT BOOKMARKS
-- ============================================================================

INSERT INTO bookmarks (user_id, listing_id, created_at, updated_at, deleted)
SELECT 
    '550e8400-e29b-41d4-a716-446655440301',
    l.listing_id,
    NOW(),
    NOW(),
    FALSE
FROM listings l
WHERE l.property_id IN (
    'a1100000-0000-0000-0000-000000000001',
    'a1100000-0000-0000-0000-000000000002',
    'a1100000-0000-0000-0000-000000000003',
    'a1100000-0000-0000-0000-000000000004',
    'a2100000-0000-0000-0000-000000000001',
    'a2100000-0000-0000-0000-000000000002',
    'a3100000-0000-0000-0000-000000000001',
    'a3100000-0000-0000-0000-000000000002',
    'a6100000-0000-0000-0000-000000000001',
    'a6100000-0000-0000-0000-000000000002'
)
ON CONFLICT (user_id, listing_id) DO NOTHING;

-- ============================================================================
-- UPDATE LISTING STATUS BASED ON listing_type
-- ============================================================================

UPDATE listings
SET    status     = 'SOLD',
       updated_at = NOW()
WHERE  property_id IN (
    'a1100000-0000-0000-0000-000000000001',
    'a1100000-0000-0000-0000-000000000002'
)
  AND  listing_type = 'SALE';

UPDATE listings
SET    status     = 'RENTED',
       updated_at = NOW()
WHERE  property_id IN (
    'a1100000-0000-0000-0000-000000000001',
    'a1100000-0000-0000-0000-000000000002'
)
  AND  listing_type = 'RENT';
