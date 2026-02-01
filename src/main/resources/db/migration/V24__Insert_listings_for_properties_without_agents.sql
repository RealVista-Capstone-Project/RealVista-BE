-- V24__Insert_listings_for_properties_without_agents.sql
-- Create listings for properties WITHOUT agents - owners create their own listings
-- Strategy: Properties without any agent assignment get a listing with owner_id as user_id
-- Total: ~25% of properties (those not assigned to any agent)

-- ============================================================================
-- APARTMENT Properties (V18) - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN RANDOM() > 0.7 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 800000 + 600000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'apt-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Modern Apt - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Listing - Premium Location'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'a1100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- HOUSE Properties (V18) - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN RANDOM() > 0.7 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 2000000 + 1500000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'house-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Family House - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Direct Listing'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'a2100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- VILLA Properties (V18) - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN RANDOM() > 0.7 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 5000000 + 3000000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'villa-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Luxury Villa - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Listing'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'a3100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- LAND RESIDENTIAL V18 - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    FLOOR(RANDOM() * 3000000 + 1500000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'land-res-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Development Land - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Direct Sale'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'a4100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- SHOPHOUSE RESIDENTIAL - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN RANDOM() > 0.6 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 2500000 + 1500000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'shophouse-res-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Shophouse - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Listing'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'a5100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- TOWNHOUSE - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN RANDOM() > 0.7 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 1500000 + 800000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'townhouse-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Townhouse - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Direct'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'a6100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- OFFICE Properties (V19 Commercial) - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN RANDOM() > 0.8 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 3000000 + 1500000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'office-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Premium Office - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Listing'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'c1100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- SHOPHOUSE COMMERCIAL - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN RANDOM() > 0.6 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 2500000 + 1200000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'shophouse-com-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Commercial Shophouse - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Direct'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'c2100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- RETAIL Properties - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN RANDOM() > 0.7 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 2000000 + 800000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'retail-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Retail Store - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Listing'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'c3100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- MALL Properties - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    FLOOR(RANDOM() * 8000000 + 4000000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'mall-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Shopping Mall - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Sale'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'c4100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- RESTAURANT Properties - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN RANDOM() > 0.7 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 1500000 + 600000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'restaurant-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Restaurant - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Listing'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'c5100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- HOTEL Properties - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    FLOOR(RANDOM() * 6000000 + 2000000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'hotel-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Hotel - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Investment'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'c6100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- WAREHOUSE Properties (V20 Industrial) - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN RANDOM() > 0.8 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 3000000 + 1000000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'warehouse-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Warehouse - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Listing'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'd1100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- FACTORY Properties - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    FLOOR(RANDOM() * 5000000 + 1500000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'factory-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Factory - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Sale'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'd2100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- WORKSHOP Properties - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN RANDOM() > 0.8 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 2000000 + 800000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'workshop-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Workshop - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Listing'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'd3100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- LOGISTICS Properties - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    CASE WHEN RANDOM() > 0.8 THEN 'RENT' ELSE 'SALE' END,
    'PUBLISHED',
    FLOOR(RANDOM() * 4000000 + 1500000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'logistics-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Logistics Center - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Listing'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'd4100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- LAND RESIDENTIAL V20 - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    FLOOR(RANDOM() * 2500000 + 1000000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'land-res-owner-v20-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Development Land - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Sale'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'd5100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- LAND COMMERCIAL - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    FLOOR(RANDOM() * 3500000 + 1500000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'land-com-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Commercial Land - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Direct'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'd6100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- LAND INDUSTRIAL - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    FLOOR(RANDOM() * 3000000 + 1200000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'land-ind-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Industrial Land - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Sale'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'd7100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);

-- ============================================================================
-- LAND AGRICULTURAL - Listings created by owners
-- ============================================================================
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    FLOOR(RANDOM() * 2000000 + 500000)::NUMERIC(14,2),
    NOW(),
    NOW(),
    FALSE,
    'land-agr-owner-' || LOWER(SUBSTR(p.property_id::text, 1, 8)) || '-' || LOWER(SUBSTR(MD5(p.owner_id::text), 1, 6)),
    'Agricultural Land - ' || SUBSTR(p.property_id::text, 1, 8) || ' Owner Listing'
FROM properties p
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'd8100000%'
AND NOT EXISTS (
    SELECT 1 FROM property_agents pa
    WHERE pa.property_id = p.property_id AND pa.deleted = FALSE
)
AND NOT EXISTS (
    SELECT 1 FROM listings l
    WHERE l.property_id = p.property_id AND l.user_id = p.owner_id
);
