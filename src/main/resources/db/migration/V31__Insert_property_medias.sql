-- V32__Insert_property_medias.sql  
-- Migration V32: Insert random property media (videos and images)
-- Each property gets 1 random video + 8 random images (1 primary image)

-- ============================================================================
-- Insert Videos for Properties (1 video per property)
-- ============================================================================
WITH video_urls AS (
    SELECT unnest(ARRAY[
        'https://res.cloudinary.com/dkpl8mokq/video/upload/v1742477422/wddwcnqtqymowebixr3k.mp4',
        'https://res.cloudinary.com/dkpl8mokq/video/upload/v1744353627/or83dttsvqylxvmy1koi.mp4',
        'https://res.cloudinary.com/dkpl8mokq/video/upload/v1727879376/pnuglzdd9yrc2psxhlwh.mp4',
        'https://res.cloudinary.com/dkpl8mokq/video/upload/v1727879766/vbqxu9bgfpngxm4cdyqr.mp4'
    ]) as video_url
)
INSERT INTO property_medias (property_id, upload_by, media_type, media_url, thumbnail_url, is_primary)
SELECT 
    p.property_id,
    p.owner_id as upload_by, -- Use property owner as uploader
    'VIDEO' as media_type,
    (SELECT video_url FROM video_urls ORDER BY RANDOM() LIMIT 1) as media_url,
    NULL as thumbnail_url, -- Videos don't need thumbnails in this case
    FALSE as is_primary -- Videos are not primary media
FROM properties p
WHERE p.deleted = FALSE;

-- ============================================================================
-- Insert Images for Properties (8 images per property, 1 primary)
-- ============================================================================
WITH image_urls AS (
    SELECT unnest(ARRAY[
        -- Exteriors
        '1600596542815-ffad4c1539a9', '1523217582562-09d0def993a6', '1691425700585-c108acad6467', '1582268611958-ebfd161ef9cf', '1671621556339-d833f511ab5d', '1613977257363-707ba9348227', '1513584684374-8bab748fbf90',
        -- Living Rooms
        '1667584523543-d1d9cc828a15', '1720247520862-7e4b14176fa8', '1592401526914-7e5d94a8d6fa', '1639663742190-1b3dba2eebcf', '1638284457192-27d3d0ec51aa', '1720247520881-672bc136da8a', '1639059790587-95625e6b764c',
        -- Bedrooms
        '1668089677938-b52086753f77', '1639751907353-3629fc00d2b2', '1640109414028-4c7f29f39ad4', '1662454419736-de132ff75638', '1600489000125-0345b89df4a6', '1640109478916-f445f8f19b11', '1614635884840-85cf80d23844',
        -- Kitchens
        '1643034738686-d69e7bc047e1', '1639751787355-bbc3ed1fd639', '1600489000022-c2086d79f9d4', '1592506119503-c0b18879bd5a', '1610177534644-34d881503b83', '1556912167-f556f1f39fdf', '1588796460718-f457ad1e1a1f',
        -- Bathrooms
        '1638799869566-b17fa794c4de', '1609280069904-ab36feb3f20c', '1595515106969-1ce29566ff1c', '1595514534892-a1ce92ee8677', '1595514535116-d0401260e7cf', '1595515106705-257fa2d62381', '1595514535711-c31bb982d8b3'
    ]) as image_id
)
INSERT INTO property_medias (property_id, upload_by, media_type, media_url, thumbnail_url, is_primary)
SELECT 
    p.property_id,
    p.owner_id as upload_by,
    'IMAGE' as media_type,
    'https://images.unsplash.com/photo-' || img.id || '?auto=format&fit=crop&w=1200&q=80' as media_url,
    'https://images.unsplash.com/photo-' || img.id || '?auto=format&fit=crop&w=1200&q=80' as thumbnail_url,
    (img.rn = 1) as is_primary -- First random image picked for EACH property is the primary one
FROM properties p
CROSS JOIN LATERAL (
    -- For each property, select 8 random unique images from the pool
    SELECT image_id as id, ROW_NUMBER() OVER () as rn
    FROM image_urls
    ORDER BY RANDOM()
    LIMIT 8
) img
WHERE p.deleted = FALSE;

-- ============================================================================
-- Ensure each property has exactly one primary image
-- ============================================================================
-- Fix properties that might have multiple primary images (keep first one)
UPDATE property_medias 
SET is_primary = FALSE 
WHERE property_media_id IN (
    SELECT property_media_id 
    FROM (
        SELECT 
            property_media_id,
            property_id,
            ROW_NUMBER() OVER (PARTITION BY property_id ORDER BY created_at) as rn
        FROM property_medias 
        WHERE media_type = 'IMAGE' AND is_primary = TRUE AND deleted = FALSE
    ) ranked 
    WHERE rn > 1
);

-- Fix properties that have no primary image (set first image as primary)
UPDATE property_medias 
SET is_primary = TRUE 
WHERE property_media_id IN (
    SELECT property_media_id 
    FROM (
        SELECT 
            pm.property_media_id,
            pm.property_id,
            ROW_NUMBER() OVER (PARTITION BY pm.property_id ORDER BY pm.created_at) as rn
        FROM property_medias pm
        WHERE pm.media_type = 'IMAGE' 
          AND pm.deleted = FALSE
          AND pm.property_id NOT IN (
              SELECT DISTINCT property_id 
              FROM property_medias 
              WHERE media_type = 'IMAGE' AND is_primary = TRUE AND deleted = FALSE
          )
    ) unranked 
    WHERE rn = 1
);