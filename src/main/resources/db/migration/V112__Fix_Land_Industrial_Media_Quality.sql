-- V112__Fix_Land_Industrial_Media_Quality.sql
-- Systematic update of LAND_INDUSTRIAL property images with high-quality, diverse photos
-- Robust version that automatically adjusts to the size of the image pool

WITH 
-- Target property type: LAND_INDUSTRIAL
target_type AS (
    SELECT '320e8400-e29b-41d4-a716-446655440019'::uuid as id
),
-- Pool of appropriate industrial land/facility images
image_pool AS (
    SELECT 
        image_id, 
        ROW_NUMBER() OVER () as row_num,
        COUNT(*) OVER () as total_count
    FROM unnest(ARRAY[
        '1669003152631-c953ac9f3ed3',
        '1669003153970-5af72bd2a302',
        '1669003153698-6e087110f06f',
        '1669003153603-07aace6756cc',
        '1669003153698-6e087110f06f',
        '1669003153698-6e087110f06f',
        '1669003152740-848eaba58580',
        '1669003154471-b72fe01a899d',
        '1669003153363-6d7ba8e20c7e',
        '1669003152238-5bd17a5bb19c',
        '1669003154697-56290eaf5023',
        '1669003153731-8b070665306e',
        '1669003153851-29a3f91e2908',
        '1669003154058-e1876138ac3c',
        '1775931437797-c4c8710976c8',
        '1651530874070-7014147f60ad',
        '1650871129552-5058a155db15',
        '1651530874070-7014147f60ad',
        '1669003155412-62440fc69836',
        '1669003154471-b72fe01a899d',
        '1669003156699-b1cd3c7904a6'
    ]) as image_id
),
-- Map existing media to the pool
mapped_media AS (
    SELECT 
        pm.property_media_id,
        ip.image_id
    FROM (
        SELECT 
            pm.property_media_id,
            ROW_NUMBER() OVER (ORDER BY pm.property_media_id) as rn
        FROM property_medias pm
        JOIN properties p ON pm.property_id = p.property_id
        WHERE p.property_type_id = (SELECT id FROM target_type)
          AND pm.media_type = 'IMAGE'
          AND pm.deleted = FALSE
    ) pm
    CROSS JOIN (SELECT total_count FROM image_pool LIMIT 1) tc
    JOIN image_pool ip ON ((pm.rn - 1) % tc.total_count) + 1 = ip.row_num
)
-- Perform the update
UPDATE property_medias
SET 
    media_url = 'https://images.unsplash.com/photo-' || mm.image_id || '?auto=format&fit=crop&w=1200&q=80',
    thumbnail_url = 'https://images.unsplash.com/photo-' || mm.image_id || '?auto=format&fit=crop&w=400&q=60'
FROM mapped_media mm
WHERE property_medias.property_media_id = mm.property_media_id;
