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
WITH correlated_listings AS (
    SELECT 
        pa.property_id,
        pa.agent_id,
        CASE WHEN (random() > 0.7) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM property_agents pa
    WHERE pa.property_id::text LIKE 'a1100000%' AND pa.deleted = FALSE
)
SELECT 
    (md5(cl.property_id::text || cl.agent_id::text))::uuid,
    cl.property_id,
    cl.agent_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (25000000 + floor(random() * 45000000)) -- RENT: 25M-70M
        ELSE (3500000000 + floor(random() * 12000000000)) -- SALE: 3.5B-15.5B
    END,
    NOW(), NOW(), FALSE,
    'can-ho-' || lower(right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4)),
    'Căn hộ cao cấp ' || right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4) || ' - Tiện ích đẳng cấp'
FROM correlated_listings cl;

-- HOUSES (a21*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        pa.property_id,
        pa.agent_id,
        CASE WHEN (random() > 0.8) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM property_agents pa
    WHERE pa.property_id::text LIKE 'a2100000%' AND pa.deleted = FALSE
)
SELECT 
    (md5(cl.property_id::text || cl.agent_id::text))::uuid,
    cl.property_id,
    cl.agent_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (40000000 + floor(random() * 80000000)) -- RENT: 40M-120M
        ELSE (12000000000 + floor(random() * 38000000000)) -- SALE: 12B-50B
    END,
    NOW(), NOW(), FALSE,
    'nha-pho-' || lower(right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4)),
    'Nhà phố liền kề ' || right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4) || ' - Vị trí trung tâm'
FROM correlated_listings cl;

-- VILLAS (a31*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        pa.property_id,
        pa.agent_id,
        CASE WHEN (random() > 0.85) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM property_agents pa
    WHERE pa.property_id::text LIKE 'a3100000%' AND pa.deleted = FALSE
)
SELECT 
    (md5(cl.property_id::text || cl.agent_id::text))::uuid,
    cl.property_id,
    cl.agent_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (120000000 + floor(random() * 380000000)) -- RENT: 120M-500M
        ELSE (55000000000 + floor(random() * 245000000000)) -- SALE: 55B-300B
    END,
    NOW(), NOW(), FALSE,
    'biet-thu-' || lower(right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4)),
    'Biệt thự sân vườn ' || right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4) || ' - Nghỉ dưỡng thượng lưu'
FROM correlated_listings cl;

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
    'dat-nen-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Đất nền thổ cư ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4) || ' - Sổ hồng riêng'
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a4100000%' AND pa.deleted = FALSE;

-- SHOPHOUSE RESO (a51*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        pa.property_id,
        pa.agent_id,
        CASE WHEN (random() > 0.5) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM property_agents pa
    WHERE pa.property_id::text LIKE 'a5100000%' AND pa.deleted = FALSE
)
SELECT 
    (md5(cl.property_id::text || cl.agent_id::text))::uuid,
    cl.property_id,
    cl.agent_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (60000000 + floor(random() * 140000000)) -- RENT: 60M-200M
        ELSE (20000000000 + floor(random() * 55000000000)) -- SALE: 20B-75B
    END,
    NOW(), NOW(), FALSE,
    'shophouse-' || lower(right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4)),
    'Shophouse thương mại ' || right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4) || ' - Kinh doanh đắc địa'
FROM correlated_listings cl;

-- TOWNHOUSE (a61*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        pa.property_id,
        pa.agent_id,
        CASE WHEN (random() > 0.7) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM property_agents pa
    WHERE pa.property_id::text LIKE 'a6100000%' AND pa.deleted = FALSE
)
SELECT 
    (md5(cl.property_id::text || cl.agent_id::text))::uuid,
    cl.property_id,
    cl.agent_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (35000000 + floor(random() * 65000000)) -- RENT: 35M-100M
        ELSE (9000000000 + floor(random() * 26000000000)) -- SALE: 9B-35B
    END,
    NOW(), NOW(), FALSE,
    'nha-lien-ke-' || lower(right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4)),
    'Nhà phố hiện đại ' || right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4) || ' - Khu dân cư trí thức'
FROM correlated_listings cl;


-- ============================================================================
-- 2. COMMERCIAL LISTINGS (V19 Properties)
-- ============================================================================

-- OFFICE (c11*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        pa.property_id,
        pa.agent_id,
        CASE WHEN (random() > 0.2) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM property_agents pa
    WHERE pa.property_id::text LIKE 'c1100000%' AND pa.deleted = FALSE
)
SELECT 
    (md5(cl.property_id::text || cl.agent_id::text))::uuid,
    cl.property_id,
    cl.agent_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (50000000 + floor(random() * 450000000)) -- RENT: 50M-500M
        ELSE (45000000000 + floor(random() * 155000000000)) -- SALE: 45B-200B
    END,
    NOW(), NOW(), FALSE,
    'van-phong-' || lower(right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4)),
    'Văn phòng hạng A ' || right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4) || ' - Tiêu chuẩn quốc tế'
FROM correlated_listings cl;

-- SHOPHOUSE COM (c21*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        pa.property_id,
        pa.agent_id,
        CASE WHEN (random() > 0.4) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM property_agents pa
    WHERE pa.property_id::text LIKE 'c2100000%' AND pa.deleted = FALSE
)
SELECT 
    (md5(cl.property_id::text || cl.agent_id::text))::uuid,
    cl.property_id,
    cl.agent_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (80000000 + floor(random() * 220000000)) -- RENT: 80M-300M
        ELSE (35000000000 + floor(random() * 85000000000)) -- SALE: 35B-120B
    END,
    NOW(), NOW(), FALSE,
    'shophouse-tm-' || lower(right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4)),
    'Shophouse kinh doanh ' || right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4) || ' - Vị trí đắc địa'
FROM correlated_listings cl;

-- RETAIL (c31*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        pa.property_id,
        pa.agent_id,
        CASE WHEN (random() > 0.3) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM property_agents pa
    WHERE pa.property_id::text LIKE 'c3100000%' AND pa.deleted = FALSE
)
SELECT 
    (md5(cl.property_id::text || cl.agent_id::text))::uuid,
    cl.property_id,
    cl.agent_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (40000000 + floor(random() * 160000000)) -- RENT: 40M-200M
        ELSE (25000000000 + floor(random() * 75000000000)) -- SALE: 25B-100B
    END,
    NOW(), NOW(), FALSE,
    'mat-bang-' || lower(right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4)),
    'Mặt bằng kinh doanh ' || right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4) || ' - Vị trí vàng'
FROM correlated_listings cl;

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
    'tttm-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Mặt bằng Trung tâm Thương mại ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)
FROM property_agents pa
WHERE pa.property_id::text LIKE 'c4100000%' AND pa.deleted = FALSE;

-- RESTAURANT (c51*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        pa.property_id,
        pa.agent_id,
        CASE WHEN (random() > 0.5) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM property_agents pa
    WHERE pa.property_id::text LIKE 'c5100000%' AND pa.deleted = FALSE
)
SELECT 
    (md5(cl.property_id::text || cl.agent_id::text))::uuid,
    cl.property_id,
    cl.agent_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (50000000 + floor(random() * 150000000)) -- RENT: 50M-200M
        ELSE (18000000000 + floor(random() * 42000000000)) -- SALE: 18B-60B
    END,
    NOW(), NOW(), FALSE,
    'nha-hang-' || lower(right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4)),
    'Mặt bằng nhà hàng ' || right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4) || ' - Hạ tầng hoàn thiện'
FROM correlated_listings cl;

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
    'khach-san-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Khách sạn cao cấp ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4) || ' - Vị trí du lịch'
FROM property_agents pa
WHERE pa.property_id::text LIKE 'c6100000%' AND pa.deleted = FALSE;


-- ============================================================================
-- 3. INDUSTRIAL & LAND LISTINGS (V20 Properties)
-- ============================================================================

-- WAREHOUSE (a71*) - Note: V20 uses a71* prefix
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        pa.property_id,
        pa.agent_id,
        CASE WHEN (random() > 0.2) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM property_agents pa
    WHERE pa.property_id::text LIKE 'a7100000%' AND pa.deleted = FALSE
)
SELECT 
    (md5(cl.property_id::text || cl.agent_id::text))::uuid,
    cl.property_id,
    cl.agent_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (100000000 + floor(random() * 400000000)) -- RENT: 100M-500M
        ELSE (45000000000 + floor(random() * 105000000000)) -- SALE: 45B-150B
    END,
    NOW(), NOW(), FALSE,
    'kho-bai-' || lower(right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4)),
    'Hệ thống kho bãi ' || right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4) || ' - Giao thông thuận tiện'
FROM correlated_listings cl;

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
    'nha-xuong-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Nhà máy sản xuất ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4) || ' - Tiêu chuẩn hiện đại'
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a7200000%' AND pa.deleted = FALSE;

-- WORKSHOP (a73*)
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, created_at, updated_at, deleted, slug, name)
WITH correlated_listings AS (
    SELECT 
        pa.property_id,
        pa.agent_id,
        CASE WHEN (random() > 0.5) THEN 'RENT' ELSE 'SALE' END as l_type
    FROM property_agents pa
    WHERE pa.property_id::text LIKE 'a7300000%' AND pa.deleted = FALSE
)
SELECT 
    (md5(cl.property_id::text || cl.agent_id::text))::uuid,
    cl.property_id,
    cl.agent_id,
    cl.l_type,
    'PUBLISHED',
    CASE 
        WHEN cl.l_type = 'RENT' THEN (40000000 + floor(random() * 80000000)) -- RENT: 40M-120M
        ELSE (15000000000 + floor(random() * 35000000000)) -- SALE: 15B-50B
    END,
    NOW(), NOW(), FALSE,
    'xuong-sx-' || lower(right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4)),
    'Xưởng sản xuất nhỏ ' || right(cl.property_id::text, 8) || '-' || right(cl.agent_id::text, 4) || ' - Điện 3 pha'
FROM correlated_listings cl;

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
    'logistics-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Trung tâm Logistics ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4) || ' - Kết nối cảng'
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
    'dat-tho-cu-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Lô đất thổ cư ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4) || ' - Khu dân cư hiện hữu'
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
    'dat-tm-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Quỹ đất thương mại ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4) || ' - Đa chức năng'
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
    'dat-kcn-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Đất quy hoạch công nghiệp ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4) || ' - Hạ tầng đồng bộ'
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
    'dat-vuon-' || lower(right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4)),
    'Đất vườn nông nghiệp ' || right(pa.property_id::text, 8) || '-' || right(pa.agent_id::text, 4) || ' - Tiềm năng đầu tư'
FROM property_agents pa
WHERE pa.property_id::text LIKE 'a7800000%' AND pa.deleted = FALSE;
