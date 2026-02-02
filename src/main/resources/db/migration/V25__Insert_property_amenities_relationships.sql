-- V25__Insert_property_amenities_relationships.sql
-- Create property_amenity relationships with logical assignment based on property type
-- Logic:
--   RESIDENTIAL (Apt, House, Villa): Security, comfort, outdoor, services, community
--   LAND properties: Infrastructure, access, proximity amenities ONLY
--   COMMERCIAL (Office, Shophouse, Retail, Mall): Facilities, services, security, proximity
--   INDUSTRIAL (Warehouse, Factory, Workshop, Logistics): Industrial facilities, access, parking
--   RESTAURANT, HOTEL: Dining, services, facilities

-- ============================================================================
-- APARTMENT - Residential Amenities
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'a1100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440001',  -- Bảo vệ 24/7
    '420e8400-e29b-41d4-a716-446655440002',  -- Camera giám sát
    '420e8400-e29b-41d4-a716-446655440005',  -- Kiểm soát ra vào
    '420e8400-e29b-41d4-a716-446655440010',  -- Điều hòa tập trung
    '420e8400-e29b-41d4-a716-446655440018',  -- Thang máy
    '420e8400-e29b-41d4-a716-446655440019',  -- Bãi đỗ xe
    '420e8400-e29b-41d4-a716-446655440034',  -- Hồ bơi
    '420e8400-e29b-41d4-a716-446655440035',  -- Trung tâm thể dục
    '420e8400-e29b-41d4-a716-446655440038',  -- Ban công
    '420e8400-e29b-41d4-a716-446655440047',  -- WiFi miễn phí
    '420e8400-e29b-41d4-a716-446655440049',  -- Điện dự phòng
    '420e8400-e29b-41d4-a716-446655440054',  -- Lễ tân
    '420e8400-e29b-41d4-a716-446655440069',  -- Phòng tập gym
    '420e8400-e29b-41d4-a716-446655440102',  -- Gần bệnh viện
    '420e8400-e29b-41d4-a716-446655440103',  -- Gần trường học
    '420e8400-e29b-41d4-a716-446655440104',  -- Gần chợ/siêu thị
    '420e8400-e29b-41d4-a716-446655440105'   -- Gần trạm MRT
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- HOUSE - Residential Amenities with Garden/Outdoor
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'a2100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440001',  -- Bảo vệ 24/7
    '420e8400-e29b-41d4-a716-446655440002',  -- Camera giám sát
    '420e8400-e29b-41d4-a716-446655440010',  -- Điều hòa tập trung
    '420e8400-e29b-41d4-a716-446655440040',  -- Sân vườn riêng
    '420e8400-e29b-41d4-a716-446655440042',  -- Nhà để xe
    '420e8400-e29b-41d4-a716-446655440043',  -- Hồ bơi riêng
    '420e8400-e29b-41d4-a716-446655440044',  -- Sân tennis riêng
    '420e8400-e29b-41d4-a716-446655440047',  -- WiFi miễn phí
    '420e8400-e29b-41d4-a716-446655440069',  -- Phòng tập gym
    '420e8400-e29b-41d4-a716-446655440102',  -- Gần bệnh viện
    '420e8400-e29b-41d4-a716-446655440103',  -- Gần trường học
    '420e8400-e29b-41d4-a716-446655440104',  -- Gần chợ/siêu thị
    '420e8400-e29b-41d4-a716-446655440107'   -- Gần công viên
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- VILLA - Premium Residential Amenities
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'a3100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440001',  -- Bảo vệ 24/7
    '420e8400-e29b-41d4-a716-446655440003',  -- Bảo vệ VIP 24/7
    '420e8400-e29b-41d4-a716-446655440040',  -- Sân vườn riêng
    '420e8400-e29b-41d4-a716-446655440042',  -- Nhà để xe
    '420e8400-e29b-41d4-a716-446655440043',  -- Hồ bơi riêng
    '420e8400-e29b-41d4-a716-446655440044',  -- Sân tennis riêng
    '420e8400-e29b-41d4-a716-446655440045',  -- Sân vườn cảnh quan
    '420e8400-e29b-41d4-a716-446655440014',  -- Nhà thông minh
    '420e8400-e29b-41d4-a716-446655440015',  -- Phòng spa
    '420e8400-e29b-41d4-a716-446655440016',  -- Phòng xông hơi
    '420e8400-e29b-41d4-a716-446655440047',  -- WiFi cao tốc
    '420e8400-e29b-41d4-a716-446655440102',  -- Gần bệnh viện
    '420e8400-e29b-41d4-a716-446655440107'   -- Gần công viên
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- LAND RESIDENTIAL V18 - Infrastructure & Access Only
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'a4100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440087',  -- Tiếp cận đường chính
    '420e8400-e29b-41d4-a716-446655440088',  -- Đất bằng phẳng
    '420e8400-e29b-41d4-a716-446655440090',  -- Lưu thông cao
    '420e8400-e29b-41d4-a716-446655440098',  -- Hệ thống tưới sẵn
    '420e8400-e29b-41d4-a716-446655440099',  -- Thoát nước tốt
    '420e8400-e29b-41d4-a716-446655440102',  -- Gần bệnh viện
    '420e8400-e29b-41d4-a716-446655440103',  -- Gần trường học
    '420e8400-e29b-41d4-a716-446655440104'   -- Gần chợ/siêu thị
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- SHOPHOUSE RESIDENTIAL - Mixed Amenities
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'a5100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440001',  -- Bảo vệ 24/7
    '420e8400-e29b-41d4-a716-446655440002',  -- Camera giám sát
    '420e8400-e29b-41d4-a716-446655440010',  -- Điều hòa tập trung
    '420e8400-e29b-41d4-a716-446655440018',  -- Thang máy
    '420e8400-e29b-41d4-a716-446655440019',  -- Bãi đỗ xe
    '420e8400-e29b-41d4-a716-446655440047',  -- WiFi miễn phí
    '420e8400-e29b-41d4-a716-446655440104',  -- Gần chợ/siêu thị
    '420e8400-e29b-41d4-a716-446655440105'   -- Gần trạm MRT
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- TOWNHOUSE - Residential Amenities
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'a6100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440001',  -- Bảo vệ 24/7
    '420e8400-e29b-41d4-a716-446655440002',  -- Camera giám sát
    '420e8400-e29b-41d4-a716-446655440040',  -- Sân vườn riêng
    '420e8400-e29b-41d4-a716-446655440042',  -- Nhà để xe
    '420e8400-e29b-41d4-a716-446655440047',  -- WiFi miễn phí
    '420e8400-e29b-41d4-a716-446655440070',  -- Trung tâm cộng đồng
    '420e8400-e29b-41d4-a716-446655440104',  -- Gần chợ/siêu thị
    '420e8400-e29b-41d4-a716-446655440107'   -- Gần công viên
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- OFFICE - Commercial Amenities
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'c1100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440001',  -- Bảo vệ 24/7
    '420e8400-e29b-41d4-a716-446655440002',  -- Camera giám sát
    '420e8400-e29b-41d4-a716-446655440005',  -- Kiểm soát ra vào
    '420e8400-e29b-41d4-a716-446655440010',  -- Điều hòa tập trung
    '420e8400-e29b-41d4-a716-446655440018',  -- Thang máy
    '420e8400-e29b-41d4-a716-446655440019',  -- Bãi đỗ xe
    '420e8400-e29b-41d4-a716-446655440022',  -- Phòng họp
    '420e8400-e29b-41d4-a716-446655440023',  -- Quán cà phê
    '420e8400-e29b-41d4-a716-446655440047',  -- WiFi cao tốc
    '420e8400-e29b-41d4-a716-446655440054',  -- Lễ tân
    '420e8400-e29b-41d4-a716-446655440105',  -- Gần trạm MRT
    '420e8400-e29b-41d4-a716-446655440109'   -- Gần khu kinh doanh
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- SHOPHOUSE COMMERCIAL - Retail & Services
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'c2100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440001',  -- Bảo vệ 24/7
    '420e8400-e29b-41d4-a716-446655440019',  -- Bãi đỗ xe
    '420e8400-e29b-41d4-a716-446655440047',  -- WiFi miễn phí
    '420e8400-e29b-41d4-a716-446655440062',  -- Điện 3 pha
    '420e8400-e29b-41d4-a716-446655440104',  -- Gần chợ/siêu thị
    '420e8400-e29b-41d4-a716-446655440105',  -- Gần trạm MRT
    '420e8400-e29b-41d4-a716-446655440109'   -- Gần khu kinh doanh
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- RETAIL - High Visibility Amenities
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'c3100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440019',  -- Bãi đỗ xe
    '420e8400-e29b-41d4-a716-446655440047',  -- WiFi miễn phí
    '420e8400-e29b-41d4-a716-446655440062',  -- Điện 3 pha
    '420e8400-e29b-41d4-a716-446655440102',  -- Gần bệnh viện
    '420e8400-e29b-41d4-a716-446655440104',  -- Gần chợ/siêu thị
    '420e8400-e29b-41d4-a716-446655440105',  -- Gần trạm MRT
    '420e8400-e29b-41d4-a716-446655440112'   -- Vị trí lưu thông cao
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- MALL - Premium Retail & Entertainment
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'c4100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440001',  -- Bảo vệ 24/7
    '420e8400-e29b-41d4-a716-446655440018',  -- Thang máy
    '420e8400-e29b-41d4-a716-446655440025',  -- Thang cuốn
    '420e8400-e29b-41d4-a716-446655440026',  -- Bãi đỗ xe nhiều tầng
    '420e8400-e29b-41d4-a716-446655440028',  -- Khu ăn uống
    '420e8400-e29b-41d4-a716-446655440047',  -- WiFi cao tốc
    '420e8400-e29b-41d4-a716-446655440054',  -- Lễ tân
    '420e8400-e29b-41d4-a716-446655440105',  -- Gần trạm MRT
    '420e8400-e29b-41d4-a716-446655440112'   -- Vị trí lưu thông cao
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- RESTAURANT - Dining & Service Amenities
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'c5100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440001',  -- Bảo vệ 24/7
    '420e8400-e29b-41d4-a716-446655440019',  -- Bãi đỗ xe
    '420e8400-e29b-41d4-a716-446655440024',  -- Phòng bếp
    '420e8400-e29b-41d4-a716-446655440028',  -- Khu ăn uống
    '420e8400-e29b-41d4-a716-446655440047',  -- WiFi miễn phí
    '420e8400-e29b-41d4-a716-446655440054',  -- Lễ tân
    '420e8400-e29b-41d4-a716-446655440104',  -- Gần chợ/siêu thị
    '420e8400-e29b-41d4-a716-446655440106'   -- Gần nhà hàng
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- HOTEL - Premium Hospitality Amenities
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'c6100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440001',  -- Bảo vệ 24/7
    '420e8400-e29b-41d4-a716-446655440018',  -- Thang máy
    '420e8400-e29b-41d4-a716-446655440019',  -- Bãi đỗ xe
    '420e8400-e29b-41d4-a716-446655440028',  -- Khu ăn uống
    '420e8400-e29b-41d4-a716-446655440034',  -- Hồ bơi
    '420e8400-e29b-41d4-a716-446655440035',  -- Trung tâm thể dục
    '420e8400-e29b-41d4-a716-446655440047',  -- WiFi cao tốc
    '420e8400-e29b-41d4-a716-446655440054',  -- Lễ tân
    '420e8400-e29b-41d4-a716-446655440057',  -- Dịch vụ valet
    '420e8400-e29b-41d4-a716-446655440105',  -- Gần trạm MRT
    '420e8400-e29b-41d4-a716-446655440108'   -- Gần sân bay
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- WAREHOUSE - Industrial Amenities
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'd1100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440073',  -- Cổng vào rộng
    '420e8400-e29b-41d4-a716-446655440074',  -- Bãi xếp hàng
    '420e8400-e29b-41d4-a716-446655440075',  -- Đường trải nhựa
    '420e8400-e29b-41d4-a716-446655440076',  -- Khu văn phòng
    '420e8400-e29b-41d4-a716-446655440080',  -- Căn tin
    '420e8400-e29b-41d4-a716-446655440082',  -- Nhiều cổng vào
    '420e8400-e29b-41d4-a716-446655440083',  -- Chỗ đỗ xe tải
    '420e8400-e29b-41d4-a716-446655440092',  -- Đường lưu thông xe tải
    '420e8400-e29b-41d4-a716-446655440095'   -- Khu công nghiệp
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- FACTORY - Production Amenities
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'd2100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440073',  -- Cổng vào rộng
    '420e8400-e29b-41d4-a716-446655440074',  -- Bãi xếp hàng
    '420e8400-e29b-41d4-a716-446655440075',  -- Đường trải nhựa
    '420e8400-e29b-41d4-a716-446655440076',  -- Khu văn phòng
    '420e8400-e29b-41d4-a716-446655440079',  -- Khu xưởng
    '420e8400-e29b-41d4-a716-446655440080',  -- Căn tin
    '420e8400-e29b-41d4-a716-446655440082',  -- Nhiều cổng vào
    '420e8400-e29b-41d4-a716-446655440083',  -- Chỗ đỗ xe tải
    '420e8400-e29b-41d4-a716-446655440095'   -- Khu công nghiệp
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- WORKSHOP - Trade & Manufacturing
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'd3100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440073',  -- Cổng vào rộng
    '420e8400-e29b-41d4-a716-446655440075',  -- Đường trải nhựa
    '420e8400-e29b-41d4-a716-446655440076',  -- Khu văn phòng
    '420e8400-e29b-41d4-a716-446655440081',  -- Khu xưởng
    '420e8400-e29b-41d4-a716-446655440083',  -- Chỗ đỗ xe tải
    '420e8400-e29b-41d4-a716-446655440086',  -- Kho chính
    '420e8400-e29b-41d4-a716-446655440095'   -- Khu công nghiệp
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- LOGISTICS - Supply Chain & Distribution
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'd4100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440073',  -- Cổng vào rộng
    '420e8400-e29b-41d4-a716-446655440074',  -- Bãi xếp hàng
    '420e8400-e29b-41d4-a716-446655440075',  -- Đường trải nhựa
    '420e8400-e29b-41d4-a716-446655440076',  -- Khu văn phòng
    '420e8400-e29b-41d4-a716-446655440080',  -- Căn tin
    '420e8400-e29b-41d4-a716-446655440082',  -- Nhiều cổng vào
    '420e8400-e29b-41d4-a716-446655440083',  -- Chỗ đỗ xe tải
    '420e8400-e29b-41d4-a716-446655440085',  -- Kho lạnh
    '420e8400-e29b-41d4-a716-446655440086',  -- Kho chính
    '420e8400-e29b-41d4-a716-446655440092'   -- Đường lưu thông xe tải
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- LAND RESIDENTIAL V20 - Infrastructure Only
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'd5100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440087',  -- Tiếp cận đường chính
    '420e8400-e29b-41d4-a716-446655440088',  -- Đất bằng phẳng
    '420e8400-e29b-41d4-a716-446655440090',  -- Lưu thông cao
    '420e8400-e29b-41d4-a716-446655440098',  -- Hệ thống tưới sẵn
    '420e8400-e29b-41d4-a716-446655440102',  -- Gần bệnh viện
    '420e8400-e29b-41d4-a716-446655440103'   -- Gần trường học
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- LAND COMMERCIAL - Business Access & Infrastructure
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'd6100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440087',  -- Tiếp cận đường chính
    '420e8400-e29b-41d4-a716-446655440088',  -- Đất bằng phẳng
    '420e8400-e29b-41d4-a716-446655440090',  -- Lưu thông cao
    '420e8400-e29b-41d4-a716-446655440092',  -- Đường lưu thông xe tải
    '420e8400-e29b-41d4-a716-446655440095',  -- Khu công nghiệp
    '420e8400-e29b-41d4-a716-446655440105',  -- Gần trạm MRT
    '420e8400-e29b-41d4-a716-446655440109'   -- Gần khu kinh doanh
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- LAND INDUSTRIAL - Heavy Access & Infrastructure
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'd7100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440087',  -- Tiếp cận đường chính
    '420e8400-e29b-41d4-a716-446655440088',  -- Đất bằng phẳng
    '420e8400-e29b-41d4-a716-446655440092',  -- Đường lưu thông xe tải
    '420e8400-e29b-41d4-a716-446655440095',  -- Khu công nghiệp
    '420e8400-e29b-41d4-a716-446655440097',  -- Giếng nước
    '420e8400-e29b-41d4-a716-446655440099'   -- Thoát nước tốt
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);

-- ============================================================================
-- LAND AGRICULTURAL - Farming & Natural Resources
-- ============================================================================
INSERT INTO property_amenities (property_id, amenity_id, created_at, updated_at, deleted)
SELECT 
    p.property_id,
    a.amenity_id,
    NOW(),
    NOW(),
    FALSE
FROM properties p
CROSS JOIN amenities a
WHERE p.deleted = FALSE
AND p.property_id::text LIKE 'd8100000%'
AND a.amenity_id IN (
    '420e8400-e29b-41d4-a716-446655440087',  -- Tiếp cận đường chính
    '420e8400-e29b-41d4-a716-446655440088',  -- Đất bằng phẳng
    '420e8400-e29b-41d4-a716-446655440089',  -- Cây xanh sẵn
    '420e8400-e29b-41d4-a716-446655440097',  -- Giếng nước
    '420e8400-e29b-41d4-a716-446655440098',  -- Hệ thống tưới sẵn
    '420e8400-e29b-41d4-a716-446655440100'   -- Đất béo phì
)
AND NOT EXISTS (
    SELECT 1 FROM property_amenities pa
    WHERE pa.property_id = p.property_id AND pa.amenity_id = a.amenity_id
);
