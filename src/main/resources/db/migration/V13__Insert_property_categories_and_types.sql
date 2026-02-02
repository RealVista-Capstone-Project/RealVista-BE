-- V13__Insert_property_categories_and_types.sql
-- Insert comprehensive property categories and types based on real estate market
-- Compatible with both PostgreSQL and H2 databases

-- 4 Categories and 20 Types
-- RESIDENTIAL (6 loại): Căn hộ, Nhà riêng, Biệt thự, Nhà phố, Penthouse, Studio
-- COMMERCIAL (6 loại): Văn phòng, Shophouse, Retail, Mall, Nhà hàng, Khách sạn
-- INDUSTRIAL (4 loại): Kho bãi, Nhà máy, Xưởng, Trung tâm logistics
-- LAND (4 loại): Đất nhà ở, Đất thương mại, Đất công nghiệp, Đất nông nghiệp

-- ============================================================================
-- PROPERTY CATEGORIES (Loại bất động sản chính)
-- ============================================================================
-- Based on real market categories in Vietnam and globally

INSERT INTO property_categories (property_category_id, name, code, created_at, updated_at, deleted)
VALUES
    ('220e8400-e29b-41d4-a716-446655440001', 'Nhà Ở', 'RESIDENTIAL', NOW(), NOW(), FALSE),
    ('220e8400-e29b-41d4-a716-446655440002', 'Thương Mại', 'COMMERCIAL', NOW(), NOW(), FALSE),
    ('220e8400-e29b-41d4-a716-446655440003', 'Công Nghiệp', 'INDUSTRIAL', NOW(), NOW(), FALSE),
    ('220e8400-e29b-41d4-a716-446655440004', 'Đất', 'LAND', NOW(), NOW(), FALSE);

-- ============================================================================
-- PROPERTY TYPES - RESIDENTIAL CATEGORY
-- ============================================================================

-- Apartment/Condominium
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440001',
    '220e8400-e29b-41d4-a716-446655440001',
    'Căn Hộ / Chung Cư',
    'APARTMENT',
    'Căn hộ trong tòa nhà chung cư cao tầng, có dịch vụ quản lý tòa nhà',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- House/Single Family Home
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440002',
    '220e8400-e29b-41d4-a716-446655440001',
    'Nhà Riêng',
    'HOUSE',
    'Nhà riêng lẻ, nhà bán riêng hoặc townhouse thấp tầng',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Villa
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440003',
    '220e8400-e29b-41d4-a716-446655440001',
    'Biệt Thự',
    'VILLA',
    'Biệt thự cao cấp với sân vườn riêng, thiết kế độc quyền',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Townhouse
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440004',
    '220e8400-e29b-41d4-a716-446655440001',
    'Nhà Phố',
    'TOWNHOUSE',
    'Nhà phố liên kế hoặc nhà liên tầng trong dự án khu đô thị',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Penthouse
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440005',
    '220e8400-e29b-41d4-a716-446655440001',
    'Penthouse',
    'PENTHOUSE',
    'Căn hộ tầng thượng cao cấp với diện tích lớn và tiện nghi sang trọng',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Studio
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440006',
    '220e8400-e29b-41d4-a716-446655440001',
    'Studio / Căn Hộ Dịch Vụ',
    'STUDIO',
    'Căn hộ studio nhỏ gọn, thường cho thuê theo ngắn hạn hoặc dài hạn',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- ============================================================================
-- PROPERTY TYPES - COMMERCIAL CATEGORY
-- ============================================================================

-- Office
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440007',
    '220e8400-e29b-41d4-a716-446655440002',
    'Văn Phòng',
    'OFFICE',
    'Không gian văn phòng thương mại, từ văn phòng riêng đến tầng lầu',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Shop House
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440008',
    '220e8400-e29b-41d4-a716-446655440002',
    'Nhà Cửa Hàng / Shop',
    'SHOPHOUSE',
    'Nhà cửa hàng kết hợp bán hàng ở tầng 1 và cư trú tầng trên',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Retail Space
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440009',
    '220e8400-e29b-41d4-a716-446655440002',
    'Không Gian Bán Lẻ',
    'RETAIL',
    'Cửa hàng bán lẻ hoặc showroom trong trung tâm thương mại',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Shopping Mall / Shopping Center
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440010',
    '220e8400-e29b-41d4-a716-446655440002',
    'Trung Tâm Thương Mại',
    'MALL',
    'Trung tâm thương mại lớn với nhiều cửa hàng và dịch vụ',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Restaurant / Cafe
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440011',
    '220e8400-e29b-41d4-a716-446655440002',
    'Nhà Hàng / Quán Cà Phê',
    'RESTAURANT',
    'Không gian kinh doanh nhà hàng, quán cà phê hoặc bar',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Hotel / Guest House
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440012',
    '220e8400-e29b-41d4-a716-446655440002',
    'Khách Sạn / Nhà Khách',
    'HOTEL',
    'Khách sạn hoặc nhà khách cho thuê phòng theo đêm',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- ============================================================================
-- PROPERTY TYPES - INDUSTRIAL CATEGORY
-- ============================================================================

-- Warehouse
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440013',
    '220e8400-e29b-41d4-a716-446655440003',
    'Kho Bãi',
    'WAREHOUSE',
    'Kho bãi chứa hàng, có công suất lưu trữ lớn',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Factory
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440014',
    '220e8400-e29b-41d4-a716-446655440003',
    'Nhà Máy',
    'FACTORY',
    'Nhà máy sản xuất với đầy đủ tiện nghi công nghiệp',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Workshop
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440015',
    '220e8400-e29b-41d4-a716-446655440003',
    'Xưởng',
    'WORKSHOP',
    'Xưởng sản xuất hoặc xưởng gia công nhỏ',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Logistics Center
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440016',
    '220e8400-e29b-41d4-a716-446655440003',
    'Trung Tâm Logistics',
    'LOGISTICS',
    'Trung tâm logistics hiện đại với hệ thống vận chuyển tích hợp',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- ============================================================================
-- PROPERTY TYPES - LAND CATEGORY
-- ============================================================================

-- Residential Land
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440017',
    '220e8400-e29b-41d4-a716-446655440004',
    'Đất Nhà Ở',
    'LAND_RESIDENTIAL',
    'Đất nền hoặc đất nông nghiệp cho mục đích xây dựng nhà ở',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Commercial Land
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440018',
    '220e8400-e29b-41d4-a716-446655440004',
    'Đất Thương Mại',
    'LAND_COMMERCIAL',
    'Đất có mục đích kinh doanh thương mại',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Industrial Land
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440019',
    '220e8400-e29b-41d4-a716-446655440004',
    'Đất Công Nghiệp',
    'LAND_INDUSTRIAL',
    'Đất công nghiệp trong khu công nghiệp',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);

-- Agricultural Land
INSERT INTO property_types (property_type_id, property_category_id, name, code, description, status, created_at, updated_at, deleted)
VALUES (
    '320e8400-e29b-41d4-a716-446655440020',
    '220e8400-e29b-41d4-a716-446655440004',
    'Đất Nông Nghiệp',
    'LAND_AGRICULTURAL',
    'Đất nông nghiệp hoặc đất lâm nghiệp',
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
);
