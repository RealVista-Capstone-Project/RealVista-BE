-- V113__Fix_Land_Agricultural_Media_Quality.sql
-- Systematic update of LAND_AGRICULTURAL property images with high-quality industrial land related images as requested
-- Robust version that automatically adjusts to the size of the image pool

WITH 
-- Target property type: LAND_AGRICULTURAL
target_type AS (
    SELECT '320e8400-e29b-41d4-a716-446655440020'::uuid as id
),
-- Pool of appropriate industrial land/facility images based on instruction to use industrial land photos
image_pool AS (
    SELECT 
        image_id, 
        ROW_NUMBER() OVER () as row_num,
        COUNT(*) OVER () as total_count
    FROM unnest(ARRAY[
        '1602770816979-f2b2c641ebd6',
        '1706463249710-03c70da3fdb3',
        '1729818828837-d9e1f6acae4d',
        '1641410955547-6cbbcc4a845e',
        '1605146830600-fa34fb48158f',
        '1615811361523-6bd03d7748e7',
        '1603938878151-6c8591eddd40',
        '1621928372414-30e144d51d49',
        '1621928372414-30e144d51d49',
        '1715229861508-b10ef278ede1',
        '1720155390935-f49e18525eb9',
        '1725338388113-d53fd935a5be',
        '1629725657499-0dcb129a427e',
        '1713205740272-c74dd56c465e',
        '1713091680068-4ae9306ec9ef',
        '1744623092840-914d3aa881f2',
        '1744623092840-914d3aa881f2',
        '1776074335214-78869351f12e',
        '1601141898474-fda425180346',
        '1615149596797-da241c6f8e30',
        '1624371552961-9670baaebf46',
        '1607070370787-d17ea699cd61',
        '1772512800212-e176f0bdf0ba',
        '1772512799658-c47bde91c3bc',
        '1749055480046-6d651de4867e',
        '1749055467924-8175410e986c',
        '1748580086330-726774532b5a'
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
