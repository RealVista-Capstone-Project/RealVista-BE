-- V111__Fix_Land_Commercial_Media_Quality.sql
-- Systematic update of LAND_COMMERCIAL property images with high-quality, diverse photos
-- Robust version that automatically adjusts to the size of the image pool

WITH 
-- Target property type: LAND_COMMERCIAL
target_type AS (
    SELECT '320e8400-e29b-41d4-a716-446655440018'::uuid as id
),
-- Pool of appropriate commercial land/construction images
image_pool AS (
    SELECT 
        image_id, 
        ROW_NUMBER() OVER () as row_num,
        COUNT(*) OVER () as total_count
    FROM unnest(ARRAY[
        '1503708928676-1cb796a0891e',
        '1531834685032-c34bf0d84c77',
        '1599707254554-027aeb4deacd',
        '1694521787162-5373b598945c',
        '1504307651254-35680f356dfd',
        '1669003153895-1dc8ff33eb32',
        '1429497419816-9ca5cfb4571a',
        '1485083269755-a7b559a4fe5e',
        '1579847188804-ecba0e2ea330',
        '1591955506264-3f5a6834570a',
        '1541888946425-d81bb19240f5',
        '1565008447742-97f6f38c985c',
        '1567954970774-58d6aa6c50dc',
        '1535732759880-bbd5c7265e3f',
        '1508450859948-4e04fabaa4ea',
        '1765133760962-00b1d2195d62',
        '1605197227977-ed86b55d671c',
        '1625085356629-479098bfc60a',
        '1669003152238-5bd17a5bb19c'
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
