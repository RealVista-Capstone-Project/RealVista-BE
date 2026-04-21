-- V24__Insert_listings_for_properties_without_agents.sql
-- Migration V24: Generate realistic owner listings with HCMC 2024-2026 market pricing
-- Logic: Properties without any agent assignment get a listing with owner_id as user_id.
-- Listing Types: SALE (70%) or RENT (30%).

-- ============================================================================
-- 1. RESIDENTIAL LISTINGS (V18 Properties)
-- ============================================================================

-- APARTMENTS (a11*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN (random() > 0.7) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.7) THEN (22000000 + floor(random() * 48000000)) -- RENT: 22M-70M
        ELSE (3200000000 + floor(random() * 11800000000)) -- SALE: 3.2B-15B
    END,
    NOW(), NOW(), FALSE,
    'apt-owner-' || lower(right(p.property_id::text, 8)),
    'Modern Apartment - ' || right(p.property_id::text, 8) || ' (Owner Direct)'
FROM properties p
WHERE p.property_id::text LIKE 'a1100000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- HOUSES (a21*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN (random() > 0.8) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.8) THEN (35000000 + floor(random() * 85000000)) -- RENT: 35M-120M
        ELSE (11000000000 + floor(random() * 37000000000)) -- SALE: 11B-48B
    END,
    NOW(), NOW(), FALSE,
    'house-owner-' || lower(right(p.property_id::text, 8)),
    'Family House - ' || right(p.property_id::text, 8) || ' (Owner Listing)'
FROM properties p
WHERE p.property_id::text LIKE 'a2100000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- VILLAS (a31*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN (random() > 0.85) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.85) THEN (110000000 + floor(random() * 390000000)) -- RENT: 110M-500M
        ELSE (50000000000 + floor(random() * 250000000000)) -- SALE: 50B-300B
    END,
    NOW(), NOW(), FALSE,
    'villa-owner-' || lower(right(p.property_id::text, 8)),
    'Luxury Villa Property - ' || right(p.property_id::text, 8) || ' (Direct)'
FROM properties p
WHERE p.property_id::text LIKE 'a3100000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- LAND RESIDENTIAL (a41*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    (7500000000 + floor(random() * 42500000000)), -- SALE: 7.5B-50B
    NOW(), NOW(), FALSE,
    'land-owner-' || lower(right(p.property_id::text, 8)),
    'Residential Land Lot - ' || right(p.property_id::text, 8) || ' (Owner Sale)'
FROM properties p
WHERE p.property_id::text LIKE 'a4100000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- SHOPHOUSE RESO (a51*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN (random() > 0.5) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.5) THEN (55000000 + floor(random() * 145000000)) -- RENT: 55M-200M
        ELSE (18000000000 + floor(random() * 57000000000)) -- SALE: 18B-75B
    END,
    NOW(), NOW(), FALSE,
    'shophouse-owner-' || lower(right(p.property_id::text, 8)),
    'Business Shophouse - ' || right(p.property_id::text, 8) || ' (Owner Direct)'
FROM properties p
WHERE p.property_id::text LIKE 'a5100000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- TOWNHOUSE (a61*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN (random() > 0.7) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.7) THEN (32000000 + floor(random() * 68000000)) -- RENT: 32M-100M
        ELSE (8500000000 + floor(random() * 26500000000)) -- SALE: 8.5B-35B
    END,
    NOW(), NOW(), FALSE,
    'townhouse-owner-' || lower(right(p.property_id::text, 8)),
    'Urban Townhouse - ' || right(p.property_id::text, 8) || ' (Owner Listing)'
FROM properties p
WHERE p.property_id::text LIKE 'a6100000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);


-- ============================================================================
-- 2. COMMERCIAL LISTINGS (V19 Properties)
-- ============================================================================

-- OFFICE (c11*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN (random() > 0.2) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.2) THEN (45000000 + floor(random() * 455000000)) -- RENT: 45M-500M
        ELSE (42000000000 + floor(random() * 158000000000)) -- SALE: 42B-200B
    END,
    NOW(), NOW(), FALSE,
    'office-owner-' || lower(right(p.property_id::text, 8)),
    'Corporate Office Space - ' || right(p.property_id::text, 8)
FROM properties p
WHERE p.property_id::text LIKE 'c1100000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- SHOPHOUSE COM (c21*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN (random() > 0.4) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.4) THEN (75000000 + floor(random() * 225000000)) -- RENT: 75M-300M
        ELSE (32000000000 + floor(random() * 88000000000)) -- SALE: 32B-120B
    END,
    NOW(), NOW(), FALSE,
    'com-shophouse-owner-' || lower(right(p.property_id::text, 8)),
    'Prime Shophouse - ' || right(p.property_id::text, 8) || ' (Owner Direct)'
FROM properties p
WHERE p.property_id::text LIKE 'c2100000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- RETAIL (c31*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN (random() > 0.3) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.3) THEN (35000000 + floor(random() * 165000000)) -- RENT: 35M-200M
        ELSE (22000000000 + floor(random() * 78000000000)) -- SALE: 22B-100B
    END,
    NOW(), NOW(), FALSE,
    'retail-owner-' || lower(right(p.property_id::text, 8)),
    'Retail Space Listing - ' || right(p.property_id::text, 8) || ' (Owner)'
FROM properties p
WHERE p.property_id::text LIKE 'c3100000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- MALL (c41*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    (320000000000 + floor(random() * 680000000000)), -- SALE: 320B-1000B
    NOW(), NOW(), FALSE,
    'mall-owner-' || lower(right(p.property_id::text, 8)),
    'Major Mall Complex - ' || right(p.property_id::text, 8) || ' (Asset Sale)'
FROM properties p
WHERE p.property_id::text LIKE 'c4100000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- HOTEL (c61*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    (145000000000 + floor(random() * 855000000000)), -- SALE: 145B-1000B
    NOW(), NOW(), FALSE,
    'hotel-owner-' || lower(right(p.property_id::text, 8)),
    'Premium Hotel Property - ' || right(p.property_id::text, 8)
FROM properties p
WHERE p.property_id::text LIKE 'c6100000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);


-- ============================================================================
-- 3. INDUSTRIAL & LAND LISTINGS (V20 Properties)
-- ============================================================================

-- WAREHOUSE (a71*) 
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN (random() > 0.2) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.2) THEN (95000000 + floor(random() * 405000000)) -- RENT: 95M-500M
        ELSE (42000000000 + floor(random() * 108000000000)) -- SALE: 42B-150B
    END,
    NOW(), NOW(), FALSE,
    'warehouse-owner-' || lower(right(p.property_id::text, 8)),
    'Industrial Warehouse - ' || right(p.property_id::text, 8) || ' (Direct)'
FROM properties p
WHERE p.property_id::text LIKE 'a7100000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- FACTORY (a72*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    (80000000000 + floor(random() * 220000000000)), -- SALE: 80B-300B
    NOW(), NOW(), FALSE,
    'factory-owner-' || lower(right(p.property_id::text, 8)),
    'Production Plant - ' || right(p.property_id::text, 8) || ' (Owner Sale)'
FROM properties p
WHERE p.property_id::text LIKE 'a7200000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- WORKSHOP (a73*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN (random() > 0.5) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.5) THEN (35000000 + floor(random() * 85000000)) -- RENT: 35M-120M
        ELSE (12000000000 + floor(random() * 38000000000)) -- SALE: 12B-50B
    END,
    NOW(), NOW(), FALSE,
    'workshop-owner-' || lower(right(p.property_id::text, 8)),
    'Workshop Space - ' || right(p.property_id::text, 8) || ' (Owner)'
FROM properties p
WHERE p.property_id::text LIKE 'a7300000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- LAND RESIDENTIAL V20 (a75*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    (11000000000 + floor(random() * 49000000000)), -- SALE: 11B-60B
    NOW(), NOW(), FALSE,
    'land-v20-owner-' || lower(right(p.property_id::text, 8)),
    'Residential Lot - ' || right(p.property_id::text, 8) || ' (Owner Direct)'
FROM properties p
WHERE p.property_id::text LIKE 'a7500000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- LAND COMMERCIAL V20 (a76*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    (22000000000 + floor(random() * 78000000000)), -- SALE: 22B-100B
    NOW(), NOW(), FALSE,
    'com-land-v20-owner-' || lower(right(p.property_id::text, 8)),
    'Commercial Development - ' || right(p.property_id::text, 8) || ' (Direct)'
FROM properties p
WHERE p.property_id::text LIKE 'a7600000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- LAND INDUSTRIAL V20 (a77*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    (18000000000 + floor(random() * 62000000000)), -- SALE: 18B-80B
    NOW(), NOW(), FALSE,
    'ind-land-v20-owner-' || lower(right(p.property_id::text, 8)),
    'Industrial Zoned Lot - ' || right(p.property_id::text, 8) || ' (Sale)'
FROM properties p
WHERE p.property_id::text LIKE 'a7700000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- LAND AGRICULTURAL V20 (a78*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    (4500000000 + floor(random() * 25500000000)), -- SALE: 4.5B-30B
    NOW(), NOW(), FALSE,
    'agr-land-v20-owner-' || lower(right(p.property_id::text, 8)),
    'Agricultural Garden - ' || right(p.property_id::text, 8) || ' (Contact Owner)'
FROM properties p
WHERE p.property_id::text LIKE 'a7800000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);
