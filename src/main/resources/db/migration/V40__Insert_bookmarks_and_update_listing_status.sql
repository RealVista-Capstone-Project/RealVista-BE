-- V40__Insert_bookmarks_and_update_listing_status.sql
-- Add bookmarks for user 550e8400-e29b-41d4-a716-446655440301
-- Update two listing statuses based on listing_type

-- ============================================================================
-- INSERT BOOKMARKS
-- ============================================================================

INSERT INTO bookmarks (user_id, listing_id, created_at, updated_at, deleted)
VALUES
    ('550e8400-e29b-41d4-a716-446655440301', '27199eda-c29e-7a94-c7cc-93959e8115cc', NOW(), NOW(), FALSE),
    ('550e8400-e29b-41d4-a716-446655440301', '0ebfeac9-da31-8dbb-4ec8-4efec6fd1f5f', NOW(), NOW(), FALSE),
    ('550e8400-e29b-41d4-a716-446655440301', '4ddaec56-16f1-5b3e-bfce-75a20122936a', NOW(), NOW(), FALSE),
    ('550e8400-e29b-41d4-a716-446655440301', '71baaf95-8908-f0a2-fd22-7108d82d214f', NOW(), NOW(), FALSE),
    ('550e8400-e29b-41d4-a716-446655440301', 'a768f183-2092-6662-1ae0-ad940d2b1fd9', NOW(), NOW(), FALSE),
    ('550e8400-e29b-41d4-a716-446655440301', '35978f82-bed2-5548-f2a4-5afff7d488fe', NOW(), NOW(), FALSE),
    ('550e8400-e29b-41d4-a716-446655440301', '56765c7c-4dd1-c149-9b52-07960c033972', NOW(), NOW(), FALSE),
    ('550e8400-e29b-41d4-a716-446655440301', '6ec0095f-b38d-3be4-11c1-043998053c5b', NOW(), NOW(), FALSE),
    ('550e8400-e29b-41d4-a716-446655440301', 'b5867db6-73c8-b7a0-9312-e4c3a9143376', NOW(), NOW(), FALSE),
    ('550e8400-e29b-41d4-a716-446655440301', '8db18a86-73fb-4810-69d2-f3812142e396', NOW(), NOW(), FALSE),
    ('550e8400-e29b-41d4-a716-446655440301', '41b4116b-ec14-7fd1-646d-ceb7e24e1e39', NOW(), NOW(), FALSE),
    ('550e8400-e29b-41d4-a716-446655440301', '71cea53a-bff0-b29b-3a9e-9e041c3d0524', NOW(), NOW(), FALSE),
    ('550e8400-e29b-41d4-a716-446655440301', '6c8879ae-90ef-ddc2-33dd-1499ad0d9cc9', NOW(), NOW(), FALSE)
ON CONFLICT (user_id, listing_id) DO NOTHING;

-- ============================================================================
-- UPDATE LISTING STATUS BASED ON listing_type
-- ============================================================================

UPDATE listings
SET    status     = 'SOLD',
       updated_at = NOW()
WHERE  listing_id = '27199eda-c29e-7a94-c7cc-93959e8115cc'
  AND  listing_type = 'SALE';

UPDATE listings
SET    status     = 'RENTED',
       updated_at = NOW()
WHERE  listing_id = '27199eda-c29e-7a94-c7cc-93959e8115cc'
  AND  listing_type = 'RENT';

UPDATE listings
SET    status     = 'SOLD',
       updated_at = NOW()
WHERE  listing_id = '0ebfeac9-da31-8dbb-4ec8-4efec6fd1f5f'
  AND  listing_type = 'SALE';

UPDATE listings
SET    status     = 'RENTED',
       updated_at = NOW()
WHERE  listing_id = '0ebfeac9-da31-8dbb-4ec8-4efec6fd1f5f'
  AND  listing_type = 'RENT';
