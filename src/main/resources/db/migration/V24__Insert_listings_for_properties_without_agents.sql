-- V24__Insert_listings_for_properties_without_agents.sql
-- Migration V24: Generate realistic owner listings with HCMC 2024-2026 market pricing
-- Logic: Properties without any agent assignment get a listing with owner_id as user_id.
-- Listing Types: SALE (70%) or RENT (30%).

-- ============================================================================
-- 1. RESIDENTIAL LISTINGS (V18 Properties)
-- ============================================================================

-- APARTMENTS (a11*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        p.property_id,
        p.owner_id,
        CASE WHEN (random() > 0.7) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM properties p
    WHERE p.property_id::text LIKE 'a1100000%' AND p.deleted = FALSE
    AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE)
)
SELECT 
    (md5(cl.property_id::text || cl.owner_id::text))::uuid,
    cl.property_id,
    cl.owner_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (22000000 + floor(random() * 48000000)) -- RENT: 22M-70M
        ELSE (3200000000 + floor(random() * 11800000000)) -- SALE: 3.2B-15B
    END,
    NOW(), NOW(), FALSE,
    'can-ho-' || lower(right(cl.property_id::text, 8)),
    'Căn hộ cao cấp ' || right(cl.property_id::text, 8) || ' - Chính chủ cho thuê/bán'
FROM correlated_listings cl;

-- HOUSES (a21*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        p.property_id,
        p.owner_id,
        CASE WHEN (random() > 0.8) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM properties p
    WHERE p.property_id::text LIKE 'a2100000%' AND p.deleted = FALSE
    AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE)
)
SELECT 
    (md5(cl.property_id::text || cl.owner_id::text))::uuid,
    cl.property_id,
    cl.owner_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (38000000 + floor(random() * 82000000)) -- RENT: 38M-120M
        ELSE (11500000000 + floor(random() * 36500000000)) -- SALE: 11.5B-48B
    END,
    NOW(), NOW(), FALSE,
    'nha-pho-' || lower(right(cl.property_id::text, 8)),
    'Nhà phố chính chủ ' || right(cl.property_id::text, 8) || ' - Vị trí trung tâm'
FROM correlated_listings cl;

-- VILLAS (a31*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        p.property_id,
        p.owner_id,
        CASE WHEN (random() > 0.85) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM properties p
    WHERE p.property_id::text LIKE 'a3100000%' AND p.deleted = FALSE
    AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE)
)
SELECT 
    (md5(cl.property_id::text || cl.owner_id::text))::uuid,
    cl.property_id,
    cl.owner_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (110000000 + floor(random() * 340000000)) -- RENT: 110M-450M
        ELSE (52000000000 + floor(random() * 218000000000)) -- SALE: 52B-270B
    END,
    NOW(), NOW(), FALSE,
    'biet-thu-' || lower(right(cl.property_id::text, 8)),
    'Biệt thự sân vườn ' || right(cl.property_id::text, 8) || ' - Chính chủ sang nhượng'
FROM correlated_listings cl;

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
    'dat-nen-' || lower(right(p.property_id::text, 8)),
    'Lô đất thổ cư ' || right(p.property_id::text, 8) || ' - Sổ hồng chính chủ'
FROM properties p
WHERE p.property_id::text LIKE 'a4100000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- SHOPHOUSE RESO (a51*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        p.property_id,
        p.owner_id,
        CASE WHEN (random() > 0.5) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM properties p
    WHERE p.property_id::text LIKE 'a5100000%' AND p.deleted = FALSE
    AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE)
)
SELECT 
    (md5(cl.property_id::text || cl.owner_id::text))::uuid,
    cl.property_id,
    cl.owner_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (55000000 + floor(random() * 135000000)) -- RENT: 55M-190M
        ELSE (19000000000 + floor(random() * 51000000000)) -- SALE: 19B-70B
    END,
    NOW(), NOW(), FALSE,
    'shophouse-' || lower(right(cl.property_id::text, 8)),
    'Shophouse thương mại ' || right(cl.property_id::text, 8) || ' - Kinh doanh đắc địa'
FROM correlated_listings cl;

-- TOWNHOUSE (a61*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        p.property_id,
        p.owner_id,
        CASE WHEN (random() > 0.7) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM properties p
    WHERE p.property_id::text LIKE 'a6100000%' AND p.deleted = FALSE
    AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE)
)
SELECT 
    (md5(cl.property_id::text || cl.owner_id::text))::uuid,
    cl.property_id,
    cl.owner_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (32000000 + floor(random() * 68000000)) -- RENT: 32M-100M
        ELSE (8500000000 + floor(random() * 26500000000)) -- SALE: 8.5B-35B
    END,
    NOW(), NOW(), FALSE,
    'nha-lien-ke-' || lower(right(cl.property_id::text, 8)),
    'Nhà phố hiện đại ' || right(cl.property_id::text, 8) || ' - Chính chủ đăng tin'
FROM correlated_listings cl;


-- ============================================================================
-- 2. COMMERCIAL LISTINGS (V19 Properties)
-- ============================================================================

-- OFFICE (c11*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        p.property_id,
        p.owner_id,
        CASE WHEN (random() > 0.2) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM properties p
    WHERE p.property_id::text LIKE 'c1100000%' AND p.deleted = FALSE
    AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE)
)
SELECT 
    (md5(cl.property_id::text || cl.owner_id::text))::uuid,
    cl.property_id,
    cl.owner_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (45000000 + floor(random() * 455000000)) -- RENT: 45M-500M
        ELSE (42000000000 + floor(random() * 158000000000)) -- SALE: 42B-200B
    END,
    NOW(), NOW(), FALSE,
    'van-phong-' || lower(right(cl.property_id::text, 8)),
    'Văn phòng hạng A ' || right(cl.property_id::text, 8) || ' - Tiêu chuẩn quốc tế'
FROM correlated_listings cl;

-- SHOPHOUSE COM (c21*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        p.property_id,
        p.owner_id,
        CASE WHEN (random() > 0.4) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM properties p
    WHERE p.property_id::text LIKE 'c2100000%' AND p.deleted = FALSE
    AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE)
)
SELECT 
    (md5(cl.property_id::text || cl.owner_id::text))::uuid,
    cl.property_id,
    cl.owner_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (75000000 + floor(random() * 225000000)) -- RENT: 75M-300M
        ELSE (32000000000 + floor(random() * 88000000000)) -- SALE: 32B-120B
    END,
    NOW(), NOW(), FALSE,
    'shophouse-tm-' || lower(right(cl.property_id::text, 8)),
    'Shophouse kinh doanh ' || right(cl.property_id::text, 8) || ' - Vị trí vàng'
FROM correlated_listings cl;

-- RETAIL (c31*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        p.property_id,
        p.owner_id,
        CASE WHEN (random() > 0.3) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM properties p
    WHERE p.property_id::text LIKE 'c3100000%' AND p.deleted = FALSE
    AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE)
)
SELECT 
    (md5(cl.property_id::text || cl.owner_id::text))::uuid,
    cl.property_id,
    cl.owner_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (35000000 + floor(random() * 165000000)) -- RENT: 35M-200M
        ELSE (22000000000 + floor(random() * 78000000000)) -- SALE: 22B-100B
    END,
    NOW(), NOW(), FALSE,
    'mat-bang-' || lower(right(cl.property_id::text, 8)),
    'Mặt bằng kinh doanh ' || right(cl.property_id::text, 8) || ' - Chính chủ cho thuê'
FROM correlated_listings cl;

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
    'tttm-' || lower(right(p.property_id::text, 8)),
    'Mặt bằng Trung tâm Thương mại ' || right(p.property_id::text, 8)
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
    'khach-san-' || lower(right(p.property_id::text, 8)),
    'Khách sạn cao cấp ' || right(p.property_id::text, 8) || ' - Vị trí du lịch'
FROM properties p
WHERE p.property_id::text LIKE 'c6100000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);


-- ============================================================================
-- 3. INDUSTRIAL & LAND LISTINGS (V20 Properties)
-- ============================================================================

-- WAREHOUSE (a71*) 
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        p.property_id,
        p.owner_id,
        CASE WHEN (random() > 0.2) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM properties p
    WHERE p.property_id::text LIKE 'a7100000%' AND p.deleted = FALSE
    AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE)
)
SELECT 
    (md5(cl.property_id::text || cl.owner_id::text))::uuid,
    cl.property_id,
    cl.owner_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (95000000 + floor(random() * 405000000)) -- RENT: 95M-500M
        ELSE (42000000000 + floor(random() * 108000000000)) -- SALE: 42B-150B
    END,
    NOW(), NOW(), FALSE,
    'kho-bai-' || lower(right(cl.property_id::text, 8)),
    'Hệ thống kho bãi ' || right(cl.property_id::text, 8) || ' - Chính chủ cho thuê'
FROM correlated_listings cl;

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
    'nha-xuong-' || lower(right(p.property_id::text, 8)),
    'Nhà máy sản xuất ' || right(p.property_id::text, 8) || ' - Tiêu chuẩn hiện đại'
FROM properties p
WHERE p.property_id::text LIKE 'a7200000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);

-- WORKSHOP (a73*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        p.property_id,
        p.owner_id,
        CASE WHEN (random() > 0.5) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM properties p
    WHERE p.property_id::text LIKE 'a7300000%' AND p.deleted = FALSE
    AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE)
)
SELECT 
    (md5(cl.property_id::text || cl.owner_id::text))::uuid,
    cl.property_id,
    cl.owner_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (35000000 + floor(random() * 85000000)) -- RENT: 35M-120M
        ELSE (12000000000 + floor(random() * 38000000000)) -- SALE: 12B-50B
    END,
    NOW(), NOW(), FALSE,
    'xuong-sx-' || lower(right(cl.property_id::text, 8)),
    'Xưởng sản xuất nhỏ ' || right(cl.property_id::text, 8) || ' - Chính chủ'
FROM correlated_listings cl;

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
    'dat-tho-cu-' || lower(right(p.property_id::text, 8)),
    'Lô đất thổ cư ' || right(p.property_id::text, 8) || ' - Khu dân cư hiện hữu'
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
    'dat-tm-' || lower(right(p.property_id::text, 8)),
    'Quỹ đất thương mại ' || right(p.property_id::text, 8) || ' - Tiềm năng đa năng'
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
    'dat-kcn-' || lower(right(p.property_id::text, 8)),
    'Đất quy hoạch công nghiệp ' || right(p.property_id::text, 8) || ' - Hạ tầng đồng bộ'
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
    'dat-vuon-' || lower(right(p.property_id::text, 8)),
    'Đất vườn nông nghiệp ' || right(p.property_id::text, 8) || ' - Chính chủ bán'
FROM properties p
WHERE p.property_id::text LIKE 'a7800000%' AND p.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE);
