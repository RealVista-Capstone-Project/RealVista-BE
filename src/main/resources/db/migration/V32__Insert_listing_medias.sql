-- V33__Insert_listing_medias.sql
-- Migration V33: Create listing media relationships from property media
-- Links each listing to all media from its corresponding property

-- ============================================================================
-- Insert listing medias based on property medias
-- ============================================================================
WITH listing_property_medias AS (
    SELECT 
        l.listing_id,
        pm.property_media_id,
        pm.media_type,
        pm.is_primary as property_is_primary,
        ROW_NUMBER() OVER (
            PARTITION BY l.listing_id, pm.media_type 
            ORDER BY 
                pm.is_primary DESC,  -- Primary media first
                pm.created_at ASC    -- Then by creation order
        ) as display_order_by_type,
        ROW_NUMBER() OVER (
            PARTITION BY l.listing_id 
            ORDER BY 
                CASE pm.media_type 
                    WHEN 'IMAGE' THEN 1  -- Images first
                    WHEN 'VIDEO' THEN 2  -- Videos second  
                    WHEN '3D' THEN 3     -- 3D tours last
                END,
                pm.is_primary DESC,      -- Primary media first within each type
                pm.created_at ASC        -- Then by creation order
        ) as overall_display_order
    FROM listings l
    JOIN properties p ON l.property_id = p.property_id
    JOIN property_medias pm ON p.property_id = pm.property_id
    WHERE l.deleted = FALSE 
      AND pm.deleted = FALSE
)
INSERT INTO listing_medias (listing_id, property_media_id, display_order, is_primary)
SELECT 
    lpm.listing_id,
    lpm.property_media_id,
    lpm.overall_display_order as display_order,
    CASE 
        WHEN lpm.media_type = 'IMAGE' AND lpm.property_is_primary = TRUE THEN TRUE
        ELSE FALSE
    END as is_primary
FROM listing_property_medias lpm;

-- ============================================================================
-- Ensure each listing has exactly one primary media (should be primary image)
-- ============================================================================
-- Fix listings that might have no primary media (set first image as primary)
UPDATE listing_medias 
SET is_primary = TRUE 
WHERE listing_media_id IN (
    SELECT lm.listing_media_id 
    FROM listing_medias lm
    JOIN property_medias pm ON lm.property_media_id = pm.property_media_id
    WHERE pm.media_type = 'IMAGE' 
      AND lm.deleted = FALSE
      AND lm.listing_id NOT IN (
          SELECT DISTINCT lm2.listing_id 
          FROM listing_medias lm2
          WHERE lm2.is_primary = TRUE AND lm2.deleted = FALSE
      )
      AND lm.display_order = (
          SELECT MIN(lm3.display_order)
          FROM listing_medias lm3
          JOIN property_medias pm3 ON lm3.property_media_id = pm3.property_media_id
          WHERE lm3.listing_id = lm.listing_id 
            AND pm3.media_type = 'IMAGE'
            AND lm3.deleted = FALSE
      )
);

-- ============================================================================
-- Fix any listings with multiple primary media (keep first image only)
-- ============================================================================
UPDATE listing_medias 
SET is_primary = FALSE 
WHERE listing_media_id IN (
    SELECT ranked.listing_media_id 
    FROM (
        SELECT 
            lm.listing_media_id,
            lm.listing_id,
            pm.media_type,
            ROW_NUMBER() OVER (
                PARTITION BY lm.listing_id 
                ORDER BY 
                    CASE pm.media_type 
                        WHEN 'IMAGE' THEN 1 
                        ELSE 2 
                    END,
                    lm.display_order ASC
            ) as rn
        FROM listing_medias lm
        JOIN property_medias pm ON lm.property_media_id = pm.property_media_id
        WHERE lm.is_primary = TRUE AND lm.deleted = FALSE
    ) ranked 
    WHERE ranked.rn > 1
);