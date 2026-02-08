-- V23__Insert_property_listings.sql
-- Create property listings based on property_agents relationships
-- Strategy: Each agent creates ONE listing for each property they are assigned to
-- Columns: listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name
-- Total: ~531 listings (one per property_agent relationship from V21 + V22)

-- ============================================================================
-- APARTMENT Properties (V18) - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN RANDOM() > 0.7 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 800000 + 600000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'apt-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Modern Apt - ' || SUBSTR(pa.property_id::text, 1, 8) || ' CBD Premium Amenities'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'a1100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- HOUSE Properties (V18) - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN RANDOM() > 0.7 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 2000000 + 1500000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'house-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Family House - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Garden & Modern Kitchen'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'a2100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- VILLA Properties (V18) - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN RANDOM() > 0.7 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 5000000 + 3000000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'villa-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Luxury Villa - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Private Pool 24/7 Security'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'a3100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- LAND RESIDENTIAL V18 - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    FLOOR(RANDOM() * 3000000 + 1500000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'land-res-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Development Land - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Growth Potential'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'a4100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- SHOPHOUSE RESIDENTIAL - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN RANDOM() > 0.6 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 2500000 + 1500000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'shophouse-res-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Shophouse - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Business + Living'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'a5100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- TOWNHOUSE - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN RANDOM() > 0.7 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 1500000 + 800000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'townhouse-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Townhouse - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Vibrant Neighborhood'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'a6100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- OFFICE Properties (V19 Commercial) - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN RANDOM() > 0.8 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 3000000 + 1500000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'office-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Premium Office - ' || SUBSTR(pa.property_id::text, 1, 8) || ' CBD Connectivity'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'c1100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- SHOPHOUSE COMMERCIAL - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN RANDOM() > 0.6 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 2500000 + 1200000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'shophouse-com-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Commercial Shophouse - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Prime Visibility'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'c2100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- RETAIL Properties - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN RANDOM() > 0.7 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 2000000 + 800000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'retail-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Retail Store - ' || SUBSTR(pa.property_id::text, 1, 8) || ' High Customer Flow'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'c3100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- MALL Properties - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    FLOOR(RANDOM() * 8000000 + 4000000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'mall-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Shopping Mall - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Multi-level Investment'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'c4100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- RESTAURANT Properties - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN RANDOM() > 0.7 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 1500000 + 600000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'restaurant-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Restaurant - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Dining Prime Location'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'c5100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- HOTEL Properties - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    FLOOR(RANDOM() * 6000000 + 2000000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'hotel-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Hotel - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Tourism Investment'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'c6100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- WAREHOUSE Properties (V20 Industrial) - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN RANDOM() > 0.8 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 3000000 + 1000000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'warehouse-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Warehouse - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Logistics Ready'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'd1100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- FACTORY Properties - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    FLOOR(RANDOM() * 5000000 + 1500000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'factory-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Factory - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Production Ready'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'd2100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- WORKSHOP Properties - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN RANDOM() > 0.8 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 2000000 + 800000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'workshop-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Workshop - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Trade Space'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'd3100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- LOGISTICS Properties - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    CASE WHEN RANDOM() > 0.8 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 4000000 + 1500000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'logistics-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Logistics Center - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Supply Chain Hub'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'd4100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- LAND RESIDENTIAL V20 - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    FLOOR(RANDOM() * 2500000 + 1000000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'land-res-v20-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Development Land - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Investment Opportunity'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'd5100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- LAND COMMERCIAL - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    FLOOR(RANDOM() * 3500000 + 1500000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'land-com-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Commercial Land - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Prime Visibility'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'd6100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- LAND INDUSTRIAL - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    FLOOR(RANDOM() * 3000000 + 1200000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'land-ind-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Industrial Land - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Manufacturing Zone'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'd7100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);

-- ============================================================================
-- LAND AGRICULTURAL - Listings created by assigned agents
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(pa.property_id::text || pa.agent_id::text))::uuid,
    pa.property_id,
    pa.agent_id,
    'SALE',
    'PUBLISHED',
    FLOOR(RANDOM() * 2000000 + 500000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'land-agr-' || LOWER(SUBSTR(pa.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(pa.agent_id::text), 1, 6)),
    'Agricultural Land - ' || SUBSTR(pa.property_id::text, 1, 8) || ' Farming Land'
FROM property_agents pa
WHERE pa.deleted = FALSE
AND pa.property_id::text LIKE 'd8100000%'
AND NOT EXISTS (
    SELECT 1 FROM listings l 
    WHERE l.property_id = pa.property_id AND l.user_id = pa.agent_id
);
