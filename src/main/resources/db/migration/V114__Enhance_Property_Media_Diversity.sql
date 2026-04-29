-- V114__Enhance_Property_Media_Diversity.sql
-- Add a highly diverse pool of images for Residential, Commercial, and Industrial properties

WITH 
target_properties AS (
    SELECT p.property_id
    FROM properties p
    JOIN property_types pt ON pt.property_type_id = p.property_type_id
    JOIN property_categories pc ON pc.property_category_id = pt.property_category_id
    WHERE pc.code IN ('RESIDENTIAL', 'COMMERCIAL', 'INDUSTRIAL')
),
image_pool AS (
    SELECT 
        image_id,
        ROW_NUMBER() OVER () as row_num,
        COUNT(*) OVER () as total_count
    FROM unnest(ARRAY[
        '1600596542815-ffad4c1539a9', '1512917774080-9991f1c4c750', '1600585154340-be6161a56a0c', '1416331108676-a22ccb276e35',
        '1600607686527-6fb886090705', '1523217582562-09d0def993a6', '1691425700585-c108acad6467', '1582268611958-ebfd161ef9cf',
        '1671621556339-d833f511ab5d', '1613977257363-707ba9348227', '1513584684374-8bab748fbf90', '1667584523543-d1d9cc828a15',
        '1720247520862-7e4b14176fa8', '1592401526914-7e5d94a8d6fa', '1639663742190-1b3dba2eebcf', '1638284457192-27d3d0ec51aa',
        '1720247520881-672bc136da8a', '1639059790587-95625e6b764c', '1668089677938-b52086753f77', '1639751907353-3629fc00d2b2',
        '1640109414028-4c7f29f39ad4', '1662454419736-de132ff75638', '1600489000125-0345b89df4a6', '1640109478916-f445f8f19b11',
        '1614635884840-85cf80d23844', '1643034738686-d69e7bc047e1', '1639751787355-bbc3ed1fd639', '1600489000022-c2086d79f9d4',
        '1592506119503-c0b18879bd5a', '1610177534644-34d881503b83', '1556912167-f556f1f39fdf', '1588796460718-f457ad1e1a1f',
        '1638799869566-b17fa794c4de', '1609280069904-ab36feb3f20c', '1595515106969-1ce29566ff1c', '1595514534892-a1ce92ee8677',
        '1595514535116-d0401260e7cf', '1595515106705-257fa2d62381', '1595514535711-c31bb982d8b3', '1564013799919-ab600027ffc6',
        '1497366216548-37526070297c', '1580587771525-78b9dba3b914', '1486406146926-c627a92ad1ab', '1584622650111-993a426fbf0a'
    ]) as image_id
),
mapped_media AS (
    SELECT 
        pm.property_media_id,
        ip.image_id
    FROM (
        SELECT 
            pm.property_media_id,
            ROW_NUMBER() OVER (ORDER BY pm.property_media_id) as rn
        FROM property_medias pm
        JOIN target_properties tp ON pm.property_id = tp.property_id
        WHERE pm.media_type = 'IMAGE' AND pm.deleted = FALSE
    ) pm
    CROSS JOIN (SELECT total_count FROM image_pool LIMIT 1) tc
    JOIN image_pool ip ON ((pm.rn - 1) % tc.total_count) + 1 = ip.row_num
)
UPDATE property_medias
SET 
    media_url = 'https://images.unsplash.com/photo-' || mm.image_id || '?auto=format&fit=crop&w=1200&q=80',
    thumbnail_url = 'https://images.unsplash.com/photo-' || mm.image_id || '?auto=format&fit=crop&w=400&q=60'
FROM mapped_media mm
WHERE property_medias.property_media_id = mm.property_media_id;
