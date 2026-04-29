-- V110__Fix_Land_Residential_Media_Quality.sql
-- Systematic update of LAND_RESIDENTIAL property images with high-quality, diverse photos

WITH 
-- Target property type: LAND_RESIDENTIAL
target_type AS (
    SELECT '320e8400-e29b-41d4-a716-446655440017'::uuid as id
),
-- Pool of appropriate land plot images
image_pool AS (
    SELECT unnest(ARRAY[
        '1726592058743-384b550930a6',
        '1726592058757-3d5c0ba6af29',
        '1726592059108-72912a6da5df',
        '1773299567657-a4bf83503ce5',
        '1764223531702-1614efb82e40',
        '1671555220536-be73f1a095b9',
        '1672851195263-9d5eb43dad10',
        '1747336755289-89f02e694606',
        '1588955838643-25b06769006c',
        '1773215023063-e662ea91a69c',
        '1706333593413-cf6c8664afe4',
        '1770280016095-3462d75f9a36',
        '1598577748202-a08517b953e9',
        '1764222233275-87dc016c11dc',
        '1629857902059-748f7159c7ca',
        '1557167305-526996e2b68b',
        '1531846610762-5222922be038',
        '1629156478928-33562ea81c81',
        '1747077885559-352fca2e3e45',
        '1671884424297-ed0194654208',
        '1652356999604-f866af3527b0'
    ]) as image_id,
    generate_series(1, 21) as row_num
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
    JOIN image_pool ip ON ((pm.rn - 1) % 21) + 1 = ip.row_num
)
-- Perform the update
UPDATE property_medias
SET 
    media_url = 'https://images.unsplash.com/photo-' || mm.image_id || '?auto=format&fit=crop&w=1200&q=80',
    thumbnail_url = 'https://images.unsplash.com/photo-' || mm.image_id || '?auto=format&fit=crop&w=400&q=60'
FROM mapped_media mm
WHERE property_medias.property_media_id = mm.property_media_id;
