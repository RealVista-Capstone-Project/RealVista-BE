-- V100__Ensure_studio_listings_exist.sql
-- Force restoration of STUDIO property listings using robust property_type_id joins.
-- This bypasses any potential failures in earlier pattern-matching logic (LIKE 'a61%').

-- ============================================================================
-- Step 1: Insert missing listings for STUDIO properties with agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, min_price, max_price, is_negotiable, published_at, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text || 'v100'))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN (MOD(EXTRACT(SECOND FROM NOW())::INT, 10) > 1) THEN 'RENT' ELSE 'SALE' END as l_type,
    'PUBLISHED',
    CASE 
        WHEN (MOD(EXTRACT(SECOND FROM NOW())::INT, 10) > 1) THEN (15000000 + floor(random() * 10000000)) -- RENT: 15M-25M
        ELSE (2800000000 + floor(random() * 3200000000)) -- SALE: 2.8B-6B
    END as price,
    0, 0, TRUE, -- min, max, negotiable placeholder
    NOW(), NOW(), NOW(), FALSE,
    'studio-' || lower(right(pa.property_id::text, 8)) || '-' || lower(right(pa.agent_id::text, 4)) || '-v100',
    'Căn hộ Studio cao cấp ' || right(pa.property_id::text, 8)
FROM property_agents pa
JOIN properties p ON pa.property_id = p.property_id
WHERE p.property_type_id = '320e8400-e29b-41d4-a716-446655440006' -- STUDIO
  AND pa.deleted = FALSE
  -- Only insert if NO listing of any status exists for this property
  AND NOT EXISTS (
      SELECT 1 FROM listings l WHERE l.property_id = pa.property_id
  );

-- ============================================================================
-- Step 2: Insert missing listings for STUDIO properties WITHOUT agents (Direct Owner)
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, min_price, max_price, is_negotiable, published_at, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text || 'v100_owner'))::uuid,
    p.property_id,
    p.owner_id,
    'SALE', -- Direct owner listings defaulted to SALE
    'PUBLISHED',
    (3000000000 + floor(random() * 3000000000)), -- 3B-6B
    0, 0, TRUE,
    NOW(), NOW(), NOW(), FALSE,
    'studio-chinh-chu-' || lower(right(p.property_id::text, 8)) || '-v100',
    'Bán căn hộ Studio chính chủ ' || right(p.property_id::text, 8)
FROM properties p
WHERE p.property_type_id = '320e8400-e29b-41d4-a716-446655440006' -- STUDIO
  AND p.deleted = FALSE
  -- No agent assigned
  AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE)
  -- No listing exists
  AND NOT EXISTS (SELECT 1 FROM listings l WHERE l.property_id = p.property_id);

-- ============================================================================
-- Step 3: Run standard backfill updates for the new listings
-- ============================================================================

-- Update min/max prices
UPDATE listings 
SET 
    min_price = ROUND(price * 0.9, 0),
    max_price = ROUND(price * 1.2, 0)
WHERE (min_price = 0 OR min_price IS NULL) 
  AND slug LIKE '%-v100';

-- Update content with realistic Studio marketing text
UPDATE listings l
SET updated_at = NOW()
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
WHERE l.property_id = p.property_id
  AND pt.code = 'STUDIO'
  AND l.slug LIKE '%-v100';

-- Standard content backfill logic for Studio
UPDATE listings l
SET name = 'Căn hộ Studio ' || right(p.property_id::text, 4) || ' - Full nội thất, view đẹp tại ' || loc.name
FROM properties p
JOIN locations loc ON p.location_id = loc.location_id
JOIN property_types pt ON p.property_type_id = pt.property_type_id
WHERE l.property_id = p.property_id
  AND pt.code = 'STUDIO'
  AND l.slug LIKE '%-v100';
