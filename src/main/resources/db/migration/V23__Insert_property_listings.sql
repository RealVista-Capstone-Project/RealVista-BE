-- V23__Insert_property_listings.sql
-- Migration V23: Generate realistic property listings with HCMC 2024-2026 market pricing
-- Logic: Each agent creates ONE listing for their assigned properties. 
-- Listing Types: SALE (70%) or RENT (30%).
-- Pricing is determined by Property Type and Listing Type.

-- ============================================================================
-- 1. RESIDENTIAL LISTINGS (V18 Properties)
-- ============================================================================

-- APARTMENTS (a11*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid as listing_id,
    pa.property_id,
    pa.agent_id,
    CASE WHEN (random() > 0.7) THEN 'RENT' ELSE 'SALE' END as listing_type_val,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.7) THEN (25000000 + floor(random() * 45000000)) -- RENT: 25M-70M
        ELSE (3500000000 + floor(random() * 12000000000)) -- SALE: 3.5B-15.5B
    END as price_val,
    NOW(), NOW(), FALSE,
    'luxury-apt-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Luxury Apartment @ ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4) || ' - Premium Lifestyle'
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a1100000%' AND pa.deleted = FALSE;

-- HOUSES (a21*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN (random() > 0.8) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.8) THEN (40000000 + floor(random() * 80000000)) -- RENT: 40M-120M
        ELSE (12000000000 + floor(random() * 38000000000)) -- SALE: 12B-50B
    END,
    NOW(), NOW(), FALSE,
    'town-house-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Exclusive Townhouse - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4) || ' | Central HCMC'
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a2100000%' AND pa.deleted = FALSE;

-- VILLAS (a31*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN (random() > 0.85) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.85) THEN (120000000 + floor(random() * 380000000)) -- RENT: 120M-500M
        ELSE (55000000000 + floor(random() * 245000000000)) -- SALE: 55B-300B
    END,
    NOW(), NOW(), FALSE,
    'royal-villa-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Royal Waterfront Villa - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4) || ' Heritage Edition'
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a3100000%' AND pa.deleted = FALSE;

-- LAND RESIDENTIAL (a41*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    (8000000000 + floor(random() * 42000000000)), -- SALE: 8B-50B
    NOW(), NOW(), FALSE,
    'prime-land-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Prime Residential Land - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4) || ' (Pink Book)'
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a4100000%' AND pa.deleted = FALSE;

-- SHOPHOUSE RESO (a51*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN (random() > 0.5) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.5) THEN (60000000 + floor(random() * 140000000)) -- RENT: 60M-200M
        ELSE (20000000000 + floor(random() * 55000000000)) -- SALE: 20B-75B
    END,
    NOW(), NOW(), FALSE,
    'urban-shophouse-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Business Hub Shophouse - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a5100000%' AND pa.deleted = FALSE;

-- TOWNHOUSE (a61*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN (random() > 0.7) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.7) THEN (35000000 + floor(random() * 65000000)) -- RENT: 35M-100M
        ELSE (9000000000 + floor(random() * 26000000000)) -- SALE: 9B-35B
    END,
    NOW(), NOW(), FALSE,
    'modern-townhouse-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Modern Townhouse - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4) || ' Heritage Row'
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a6100000%' AND pa.deleted = FALSE;


-- ============================================================================
-- 2. COMMERCIAL LISTINGS (V19 Properties)
-- ============================================================================

-- OFFICE (c11*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN (random() > 0.2) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.2) THEN (50000000 + floor(random() * 450000000)) -- RENT: 50M-500M
        ELSE (45000000000 + floor(random() * 155000000000)) -- SALE: 45B-200B
    END,
    NOW(), NOW(), FALSE,
    'grade-a-office-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Grade A Office Space - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)
FROM property_agents pa
WHERE pa.property_id::text LIKE 'c1100000%' AND pa.deleted = FALSE;

-- SHOPHOUSE COM (c21*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN (random() > 0.4) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.4) THEN (80000000 + floor(random() * 220000000)) -- RENT: 80M-300M
        ELSE (35000000000 + floor(random() * 85000000000)) -- SALE: 35B-120B
    END,
    NOW(), NOW(), FALSE,
    'com-shophouse-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Prime Commercial Shophouse - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)
FROM property_agents pa
WHERE pa.property_id::text LIKE 'c2100000%' AND pa.deleted = FALSE;

-- RETAIL (c31*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN (random() > 0.3) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.3) THEN (40000000 + floor(random() * 160000000)) -- RENT: 40M-200M
        ELSE (25000000000 + floor(random() * 75000000000)) -- SALE: 25B-100B
    END,
    NOW(), NOW(), FALSE,
    'retail-space-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'High Visibility Retail - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)
FROM property_agents pa
WHERE pa.property_id::text LIKE 'c3100000%' AND pa.deleted = FALSE;

-- MALL (c41*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    (350000000000 + floor(random() * 650000000000)), -- SALE: 350B-1000B
    NOW(), NOW(), FALSE,
    'mega-mall-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Shopping Mall Complex - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4) || ' Asset'
FROM property_agents pa
WHERE pa.property_id::text LIKE 'c4100000%' AND pa.deleted = FALSE;

-- RESTAURANT (c51*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN (random() > 0.5) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.5) THEN (50000000 + floor(random() * 150000000)) -- RENT: 50M-200M
        ELSE (18000000000 + floor(random() * 42000000000)) -- SALE: 18B-60B
    END,
    NOW(), NOW(), FALSE,
    'restaurant-ops-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Premium Restaurant Space - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)
FROM property_agents pa
WHERE pa.property_id::text LIKE 'c5100000%' AND pa.deleted = FALSE;

-- HOTEL (c61*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    (150000000000 + floor(random() * 850000000000)), -- SALE: 150B-1000B
    NOW(), NOW(), FALSE,
    'grand-hotel-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Luxury Hotel Property - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)
FROM property_agents pa
WHERE pa.property_id::text LIKE 'c6100000%' AND pa.deleted = FALSE;


-- ============================================================================
-- 3. INDUSTRIAL & LAND LISTINGS (V20 Properties)
-- ============================================================================

-- WAREHOUSE (a71*) - Note: V20 uses a71* prefix
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN (random() > 0.2) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.2) THEN (100000000 + floor(random() * 400000000)) -- RENT: 100M-500M
        ELSE (45000000000 + floor(random() * 105000000000)) -- SALE: 45B-150B
    END,
    NOW(), NOW(), FALSE,
    'log-warehouse-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Modern Warehouse Hub - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a7100000%' AND pa.deleted = FALSE;

-- FACTORY (a72*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    (85000000000 + floor(random() * 215000000000)), -- SALE: 85B-300B
    NOW(), NOW(), FALSE,
    'smart-factory-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Smart Manufacturing Plant - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a7200000%' AND pa.deleted = FALSE;

-- WORKSHOP (a73*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN (random() > 0.5) THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    CASE 
        WHEN (random() > 0.5) THEN (40000000 + floor(random() * 80000000)) -- RENT: 40M-120M
        ELSE (15000000000 + floor(random() * 35000000000)) -- SALE: 15B-50B
    END,
    NOW(), NOW(), FALSE,
    'workshop-space-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Industrial Workshop - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a7300000%' AND pa.deleted = FALSE;

-- LOGISTICS CENTER (a74*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    (150000000000 + floor(random() * 450000000000)), -- SALE: 150B-600B
    NOW(), NOW(), FALSE,
    'log-center-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Full-Service Logistics Hub - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a7400000%' AND pa.deleted = FALSE;

-- LAND RESIDENTIAL V20 (a75*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    (12000000000 + floor(random() * 48000000000)), -- SALE: 12B-60B
    NOW(), NOW(), FALSE,
    'res-land-v20-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Exclusive Residential Lot - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a7500000%' AND pa.deleted = FALSE;

-- LAND COMMERCIAL V20 (a76*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    (25000000000 + floor(random() * 75000000000)), -- SALE: 25B-100B
    NOW(), NOW(), FALSE,
    'com-land-v20-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Commercial Development Land - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a7600000%' AND pa.deleted = FALSE;

-- LAND INDUSTRIAL V20 (a77*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    (20000000000 + floor(random() * 60000000000)), -- SALE: 20B-80B
    NOW(), NOW(), FALSE,
    'ind-land-v20-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Industrial Zoned Land - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a7700000%' AND pa.deleted = FALSE;

-- LAND AGRICULTURAL V20 (a78*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    (5000000000 + floor(random() * 25000000000)), -- SALE: 5B-30B
    NOW(), NOW(), FALSE,
    'agr-land-v20-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Agricultural Garden Land - ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a7800000%' AND pa.deleted = FALSE;
