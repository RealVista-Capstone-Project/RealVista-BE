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
    'nha-rieng-' || lower(right(cl.property_id::text, 8)),
    'Nhà riêng chính chủ ' || right(cl.property_id::text, 8) || ' - Không gian yên tĩnh'
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

-- TOWNHOUSE (a41*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        p.property_id,
        p.owner_id,
        CASE WHEN (random() > 0.8) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM properties p
    WHERE p.property_id::text LIKE 'a4100000%' AND p.deleted = FALSE
    AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE)
)
SELECT 
    (md5(cl.property_id::text || cl.owner_id::text))::uuid,
    cl.property_id,
    cl.owner_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (40000000 + floor(random() * 95000000)) -- RENT: 40M-135M
        ELSE (17500000000 + floor(random() * 32500000000)) -- SALE: 17.5B-50B
    END,
    NOW(), NOW(), FALSE,
    'nha-pho-' || lower(right(cl.property_id::text, 8)),
    'Nhà phố hiện đại ' || right(cl.property_id::text, 8) || ' - Chính chủ cần bán/cho thuê'
FROM correlated_listings cl;

-- PENTHOUSE (a51*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        p.property_id,
        p.owner_id,
        CASE WHEN (random() > 0.8) THEN 'RENT' ELSE 'SALE' END as l_type
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
        WHEN cl.l_type = 'RENT' THEN (140000000 + floor(random() * 260000000)) -- RENT: 140M-400M
        ELSE (42000000000 + floor(random() * 108000000000)) -- SALE: 42B-150B
    END,
    NOW(), NOW(), FALSE,
    'penthouse-' || lower(right(cl.property_id::text, 8)),
    'Penthouse đẳng cấp ' || right(cl.property_id::text, 8) || ' - View triệu đô, chính chủ'
FROM correlated_listings cl;

-- STUDIO / SERVICED APARTMENT (a61*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        p.property_id,
        p.owner_id,
        CASE WHEN (random() > 0.1) THEN 'RENT' ELSE 'SALE' END as l_type
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
        WHEN cl.l_type = 'RENT' THEN (10000000 + floor(random() * 20000000)) -- RENT: 10M-30M
        ELSE (2400000000 + floor(random() * 4600000000)) -- SALE: 2.4B-7B
    END,
    NOW(), NOW(), FALSE,
    'studio-' || lower(right(cl.property_id::text, 8)),
    'Căn hộ Studio tiện nghi ' || right(cl.property_id::text, 8) || ' - Full nội thất, chính chủ'
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

-- RESTAURANT (c51*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        p.property_id,
        p.owner_id,
        CASE WHEN (random() > 0.5) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM properties p
    WHERE p.property_id::text LIKE 'c5100000%' AND p.deleted = FALSE
    AND NOT EXISTS (SELECT 1 FROM property_agents pa WHERE pa.property_id = p.property_id AND pa.deleted = FALSE)
)
SELECT 
    (md5(cl.property_id::text || cl.owner_id::text))::uuid,
    cl.property_id,
    cl.owner_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (45000000 + floor(random() * 155000000)) -- RENT: 45M-200M
        ELSE (17000000000 + floor(random() * 41000000000)) -- SALE: 17B-58B
    END,
    NOW(), NOW(), FALSE,
    'nha-hang-' || lower(right(cl.property_id::text, 8)),
    'Mặt bằng nhà hàng ' || right(cl.property_id::text, 8) || ' - Chính chủ sang nhượng'
FROM correlated_listings cl;


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

-- LOGISTICS CENTER (a74*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
SELECT 
    (md5(p.property_id::text || p.owner_id::text))::uuid,
    p.property_id,
    p.owner_id,
    'SALE',
    'PUBLISHED',
    (140000000000 + floor(random() * 460000000000)), -- SALE: 140B-600B
    NOW(), NOW(), FALSE,
    'logistics-' || lower(right(p.property_id::text, 8)),
    'Trung tâm Logistics ' || right(p.property_id::text, 8) || ' - Chính chủ chuyển nhượng'
FROM properties p
WHERE p.property_id::text LIKE 'a7400000%' AND p.deleted = FALSE
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
