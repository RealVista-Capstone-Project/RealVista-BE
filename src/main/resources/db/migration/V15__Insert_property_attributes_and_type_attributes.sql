-- V15__Insert_property_attributes_and_type_attributes.sql
-- Define reusable property attributes and link to property types
-- Attributes are shared across types: BEDROOMS, BATHROOMS, DIRECTION, etc.
-- Lưu ý: Year Built, Legal Status, Condition, Management Fee đã có trong bảng khác

-- ============================================================================
-- DEFINE REUSABLE ATTRIBUTES
-- ============================================================================

-- ========== RESIDENTIAL COMMON ATTRIBUTES ==========
INSERT INTO property_attributes (property_attribute_id, name, code, data_type, is_searchable, icon, unit, created_at, updated_at, deleted)
VALUES
    -- Residential common
    ('410e8400-e29b-41d4-a716-446655440001', 'Phòng ngủ', 'BEDROOMS', 'NUMBER', TRUE, 'bed', 'phòng', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440002', 'Phòng tắm', 'BATHROOMS', 'NUMBER', TRUE, 'bath', 'phòng', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440003', 'Tầng', 'FLOOR', 'NUMBER', TRUE, 'layers', 'tầng', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440004', 'Hướng nhà', 'DIRECTION', 'TEXT', TRUE, 'compass', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440005', 'Sân vườn', 'GARDEN', 'BOOLEAN', TRUE, 'leaf', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440006', 'Nhà để xe', 'GARAGE', 'BOOLEAN', TRUE, 'car', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440007', 'Số tầng', 'FLOORS', 'NUMBER', TRUE, 'layers', 'tầng', NOW(), NOW(), FALSE),
    
    -- Apartment specific
    ('410e8400-e29b-41d4-a716-446655440008', 'Ban công', 'BALCONY', 'BOOLEAN', TRUE, 'window', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440009', 'Điều hòa', 'AC', 'BOOLEAN', TRUE, 'wind', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440010', 'Tổng tầng', 'TOTAL_FLOORS', 'NUMBER', TRUE, 'building', 'tầng', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440011', 'Hướng ban công', 'BALCONY_TYPE', 'TEXT', TRUE, 'view', NULL, NOW(), NOW(), FALSE),
    
    -- House/Villa/Townhouse
    ('410e8400-e29b-41d4-a716-446655440012', 'Diện tích để xe', 'GARAGE_AREA', 'NUMBER', TRUE, 'car', 'm²', NOW(), NOW(), FALSE),
    
    -- Villa specific
    ('410e8400-e29b-41d4-a716-446655440013', 'Hồ bơi', 'POOL', 'BOOLEAN', TRUE, 'water', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440014', 'Tennis court', 'TENNIS', 'BOOLEAN', TRUE, 'sport', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440015', 'Chỗ để xe', 'PARKING', 'NUMBER', TRUE, 'car', 'chỗ', NOW(), NOW(), FALSE),
    
    -- Penthouse specific
    ('410e8400-e29b-41d4-a716-446655440016', 'Tầng cao nhất', 'TOP_FLOOR', 'BOOLEAN', TRUE, 'layers', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440017', 'Ban công lớn', 'LARGE_BALCONY', 'BOOLEAN', TRUE, 'window', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440018', 'Nhìn ra ngoài', 'VIEW', 'TEXT', TRUE, 'eye', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440019', 'Gym chung', 'GYM', 'BOOLEAN', TRUE, 'dumbbell', NULL, NOW(), NOW(), FALSE),
    
    -- Studio specific
    ('410e8400-e29b-41d4-a716-446655440020', 'Loại phòng', 'ROOMS', 'TEXT', TRUE, 'bed', 'loại', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440021', 'WiFi miễn phí', 'WIFI', 'BOOLEAN', TRUE, 'wifi', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440022', 'Dụng cụ nhà bếp', 'KITCHEN', 'BOOLEAN', TRUE, 'utensils', NULL, NOW(), NOW(), FALSE);

-- ========== COMMERCIAL ATTRIBUTES ==========
INSERT INTO property_attributes (property_attribute_id, name, code, data_type, is_searchable, icon, unit, created_at, updated_at, deleted)
VALUES
    -- Office
    ('410e8400-e29b-41d4-a716-446655440101', 'Phòng làm việc', 'OFFICE_ROOMS', 'NUMBER', TRUE, 'briefcase', 'phòng', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440102', 'Phòng hội nghị', 'MEETING_ROOMS', 'NUMBER', TRUE, 'users', 'phòng', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440103', 'Phòng vệ sinh', 'RESTROOMS', 'NUMBER', TRUE, 'bath', 'phòng', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440104', 'Điều hòa riêng', 'INDIVIDUAL_AC', 'BOOLEAN', TRUE, 'wind', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440105', 'Lễ tân', 'RECEPTION', 'BOOLEAN', TRUE, 'person', NULL, NOW(), NOW(), FALSE),
    
    -- Shophouse/Retail
    ('410e8400-e29b-41d4-a716-446655440106', 'Chiều rộng', 'WIDTH', 'NUMBER', TRUE, 'ruler', 'm', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440107', 'Chiều sâu', 'DEPTH', 'NUMBER', TRUE, 'ruler', 'm', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440108', 'Phòng ngủ trên', 'UPPER_BEDROOMS', 'NUMBER', TRUE, 'bed', 'phòng', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440109', 'Phòng tắm trên', 'UPPER_BATHROOMS', 'NUMBER', TRUE, 'bath', 'phòng', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440110', 'Cửa hàng', 'SHOP', 'BOOLEAN', TRUE, 'shop', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440111', 'Cửa sổ trưng bày', 'DISPLAY_WINDOW', 'BOOLEAN', TRUE, 'window', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440112', 'Lưu thông cao', 'HIGH_TRAFFIC', 'BOOLEAN', TRUE, 'people', NULL, NOW(), NOW(), FALSE),
    
    -- Mall
    ('410e8400-e29b-41d4-a716-446655440113', 'Các cửa hàng', 'SHOPS', 'NUMBER', TRUE, 'shop', 'cửa', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440114', 'Rạp chiếu phim', 'CINEMA', 'BOOLEAN', TRUE, 'film', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440115', 'Khu ăn uống', 'FOOD_COURT', 'BOOLEAN', TRUE, 'utensils', NULL, NOW(), NOW(), FALSE),
    
    -- Restaurant
    ('410e8400-e29b-41d4-a716-446655440116', 'Bàn ăn', 'TABLES', 'NUMBER', TRUE, 'layout-list', 'bàn', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440117', 'Bếp', 'RESTAURANT_KITCHEN', 'BOOLEAN', TRUE, 'utensils', NULL, NOW(), NOW(), FALSE),
    
    -- Hotel
    ('410e8400-e29b-41d4-a716-446655440118', 'Phòng', 'ROOMS_HOTEL', 'NUMBER', TRUE, 'bed', 'phòng', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440119', 'Sao', 'STARS', 'NUMBER', TRUE, 'star', 'sao', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440120', 'Nhà hàng', 'RESTAURANT', 'BOOLEAN', TRUE, 'utensils', NULL, NOW(), NOW(), FALSE);

-- ========== INDUSTRIAL ATTRIBUTES ==========
INSERT INTO property_attributes (property_attribute_id, name, code, data_type, is_searchable, icon, unit, created_at, updated_at, deleted)
VALUES
    ('410e8400-e29b-41d4-a716-446655440201', 'Chiều cao', 'HEIGHT', 'NUMBER', TRUE, 'arrow-up', 'm', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440202', 'Chỗ để xe tải', 'TRUCK_PARKING', 'NUMBER', TRUE, 'truck', 'chỗ', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440203', 'Cửa vào', 'GATES', 'NUMBER', TRUE, 'door-open', 'cửa', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440204', 'Đường sắt', 'RAILWAY', 'BOOLEAN', TRUE, 'train', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440205', 'Nguồn nước', 'WATER', 'BOOLEAN', TRUE, 'droplet', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440206', 'Điện', 'POWER', 'TEXT', TRUE, 'zap', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440207', 'Thoát nước', 'DRAINAGE', 'BOOLEAN', TRUE, 'droplet', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440208', 'Lối vào', 'ENTRANCES', 'NUMBER', TRUE, 'door-open', 'lối', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440209', 'Bảo vệ', 'SECURITY', 'BOOLEAN', TRUE, 'shield', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440210', 'Kho lạnh', 'COLD_STORAGE', 'BOOLEAN', TRUE, 'thermometer', NULL, NOW(), NOW(), FALSE);

-- ========== LAND ATTRIBUTES ==========
INSERT INTO property_attributes (property_attribute_id, name, code, data_type, is_searchable, icon, unit, created_at, updated_at, deleted)
VALUES
    ('410e8400-e29b-41d4-a716-446655440301', 'Mặt tiền', 'FRONTAGE', 'NUMBER', TRUE, 'ruler', 'm', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440302', 'Sâu', 'LAND_DEPTH', 'NUMBER', TRUE, 'ruler', 'm', NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440304', 'Quy hoạch', 'PLANNING', 'TEXT', TRUE, 'map', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440305', 'Mục đích sử dụng', 'PURPOSE', 'TEXT', TRUE, 'target', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440306', 'Quy hoạch chiều cao', 'HEIGHT_PLANNING', 'TEXT', TRUE, 'arrow-up', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440307', 'Khu công nghiệp', 'ZONE', 'TEXT', TRUE, 'map-pin', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440308', 'Loại cây', 'CROP_TYPE', 'TEXT', TRUE, 'leaf', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440309', 'Nguồn nước tưới', 'WATER_SOURCE', 'TEXT', TRUE, 'droplet', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440310', 'Hệ thống tưới', 'IRRIGATION', 'BOOLEAN', TRUE, 'droplet', NULL, NOW(), NOW(), FALSE),
    ('410e8400-e29b-41d4-a716-446655440311', 'Đường vào', 'ACCESS_ROAD', 'TEXT', TRUE, 'road', NULL, NOW(), NOW(), FALSE);

-- ============================================================================
-- LINK ATTRIBUTES TO PROPERTY TYPES
-- ============================================================================

-- APARTMENT (320e8400-e29b-41d4-a716-446655440001)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440001', '410e8400-e29b-41d4-a716-446655440001', TRUE, NOW(), NOW(), FALSE),  -- Bedrooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440001', '410e8400-e29b-41d4-a716-446655440002', TRUE, NOW(), NOW(), FALSE),  -- Bathrooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440001', '410e8400-e29b-41d4-a716-446655440003', TRUE, NOW(), NOW(), FALSE),  -- Floor
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440001', '410e8400-e29b-41d4-a716-446655440008', FALSE, NOW(), NOW(), FALSE), -- Balcony
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440001', '410e8400-e29b-41d4-a716-446655440009', FALSE, NOW(), NOW(), FALSE), -- AC
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440001', '410e8400-e29b-41d4-a716-446655440010', FALSE, NOW(), NOW(), FALSE), -- Total Floors
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440001', '410e8400-e29b-41d4-a716-446655440011', FALSE, NOW(), NOW(), FALSE), -- Balcony Type
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440001', '410e8400-e29b-41d4-a716-446655440004', FALSE, NOW(), NOW(), FALSE); -- Direction

-- HOUSE (320e8400-e29b-41d4-a716-446655440002)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440002', '410e8400-e29b-41d4-a716-446655440001', TRUE, NOW(), NOW(), FALSE),  -- Bedrooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440002', '410e8400-e29b-41d4-a716-446655440002', TRUE, NOW(), NOW(), FALSE),  -- Bathrooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440002', '410e8400-e29b-41d4-a716-446655440007', FALSE, NOW(), NOW(), FALSE), -- Floors
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440002', '410e8400-e29b-41d4-a716-446655440005', FALSE, NOW(), NOW(), FALSE), -- Garden
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440002', '410e8400-e29b-41d4-a716-446655440006', FALSE, NOW(), NOW(), FALSE), -- Garage
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440002', '410e8400-e29b-41d4-a716-446655440012', FALSE, NOW(), NOW(), FALSE), -- Garage Area
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440002', '410e8400-e29b-41d4-a716-446655440004', FALSE, NOW(), NOW(), FALSE); -- Direction

-- VILLA (320e8400-e29b-41d4-a716-446655440003)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440003', '410e8400-e29b-41d4-a716-446655440001', TRUE, NOW(), NOW(), FALSE),  -- Bedrooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440003', '410e8400-e29b-41d4-a716-446655440002', TRUE, NOW(), NOW(), FALSE),  -- Bathrooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440003', '410e8400-e29b-41d4-a716-446655440005', FALSE, NOW(), NOW(), FALSE), -- Garden
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440003', '410e8400-e29b-41d4-a716-446655440013', FALSE, NOW(), NOW(), FALSE), -- Pool
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440003', '410e8400-e29b-41d4-a716-446655440014', FALSE, NOW(), NOW(), FALSE), -- Tennis
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440003', '410e8400-e29b-41d4-a716-446655440006', FALSE, NOW(), NOW(), FALSE), -- Garage
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440003', '410e8400-e29b-41d4-a716-446655440015', FALSE, NOW(), NOW(), FALSE), -- Parking
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440003', '410e8400-e29b-41d4-a716-446655440004', FALSE, NOW(), NOW(), FALSE); -- Direction

-- TOWNHOUSE (320e8400-e29b-41d4-a716-446655440004)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440004', '410e8400-e29b-41d4-a716-446655440001', TRUE, NOW(), NOW(), FALSE),  -- Bedrooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440004', '410e8400-e29b-41d4-a716-446655440002', TRUE, NOW(), NOW(), FALSE),  -- Bathrooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440004', '410e8400-e29b-41d4-a716-446655440007', FALSE, NOW(), NOW(), FALSE), -- Floors
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440004', '410e8400-e29b-41d4-a716-446655440005', FALSE, NOW(), NOW(), FALSE), -- Garden
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440004', '410e8400-e29b-41d4-a716-446655440006', FALSE, NOW(), NOW(), FALSE), -- Garage
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440004', '410e8400-e29b-41d4-a716-446655440004', FALSE, NOW(), NOW(), FALSE); -- Direction

-- PENTHOUSE (320e8400-e29b-41d4-a716-446655440005)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440005', '410e8400-e29b-41d4-a716-446655440001', TRUE, NOW(), NOW(), FALSE),  -- Bedrooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440005', '410e8400-e29b-41d4-a716-446655440002', TRUE, NOW(), NOW(), FALSE),  -- Bathrooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440005', '410e8400-e29b-41d4-a716-446655440016', FALSE, NOW(), NOW(), FALSE), -- Top Floor
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440005', '410e8400-e29b-41d4-a716-446655440017', FALSE, NOW(), NOW(), FALSE), -- Large Balcony
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440005', '410e8400-e29b-41d4-a716-446655440018', FALSE, NOW(), NOW(), FALSE), -- View
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440005', '410e8400-e29b-41d4-a716-446655440013', FALSE, NOW(), NOW(), FALSE), -- Pool
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440005', '410e8400-e29b-41d4-a716-446655440019', FALSE, NOW(), NOW(), FALSE), -- Gym
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440005', '410e8400-e29b-41d4-a716-446655440004', FALSE, NOW(), NOW(), FALSE); -- Direction

-- STUDIO (320e8400-e29b-41d4-a716-446655440006)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440006', '410e8400-e29b-41d4-a716-446655440020', FALSE, NOW(), NOW(), FALSE), -- Rooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440006', '410e8400-e29b-41d4-a716-446655440002', TRUE, NOW(), NOW(), FALSE),  -- Bathrooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440006', '410e8400-e29b-41d4-a716-446655440003', FALSE, NOW(), NOW(), FALSE), -- Floor
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440006', '410e8400-e29b-41d4-a716-446655440009', FALSE, NOW(), NOW(), FALSE), -- AC
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440006', '410e8400-e29b-41d4-a716-446655440021', FALSE, NOW(), NOW(), FALSE), -- WiFi
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440006', '410e8400-e29b-41d4-a716-446655440022', FALSE, NOW(), NOW(), FALSE); -- Kitchen

-- OFFICE (320e8400-e29b-41d4-a716-446655440007)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440007', '410e8400-e29b-41d4-a716-446655440101', FALSE, NOW(), NOW(), FALSE), -- Office Rooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440007', '410e8400-e29b-41d4-a716-446655440102', FALSE, NOW(), NOW(), FALSE), -- Meeting Rooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440007', '410e8400-e29b-41d4-a716-446655440003', FALSE, NOW(), NOW(), FALSE), -- Floor
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440007', '410e8400-e29b-41d4-a716-446655440015', FALSE, NOW(), NOW(), FALSE), -- Parking
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440007', '410e8400-e29b-41d4-a716-446655440105', FALSE, NOW(), NOW(), FALSE), -- Reception
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440007', '410e8400-e29b-41d4-a716-446655440103', FALSE, NOW(), NOW(), FALSE), -- Restrooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440007', '410e8400-e29b-41d4-a716-446655440104', FALSE, NOW(), NOW(), FALSE); -- Individual AC

-- SHOPHOUSE (320e8400-e29b-41d4-a716-446655440008)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440008', '410e8400-e29b-41d4-a716-446655440106', TRUE, NOW(), NOW(), FALSE),  -- Width
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440008', '410e8400-e29b-41d4-a716-446655440107', FALSE, NOW(), NOW(), FALSE), -- Depth
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440008', '410e8400-e29b-41d4-a716-446655440108', FALSE, NOW(), NOW(), FALSE), -- Upper Bedrooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440008', '410e8400-e29b-41d4-a716-446655440109', FALSE, NOW(), NOW(), FALSE), -- Upper Bathrooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440008', '410e8400-e29b-41d4-a716-446655440110', TRUE, NOW(), NOW(), FALSE),  -- Shop
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440008', '410e8400-e29b-41d4-a716-446655440006', FALSE, NOW(), NOW(), FALSE); -- Garage

-- RETAIL (320e8400-e29b-41d4-a716-446655440009)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440009', '410e8400-e29b-41d4-a716-446655440106', TRUE, NOW(), NOW(), FALSE),  -- Width
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440009', '410e8400-e29b-41d4-a716-446655440107', FALSE, NOW(), NOW(), FALSE), -- Depth
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440009', '410e8400-e29b-41d4-a716-446655440111', FALSE, NOW(), NOW(), FALSE), -- Display Window
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440009', '410e8400-e29b-41d4-a716-446655440015', FALSE, NOW(), NOW(), FALSE), -- Parking
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440009', '410e8400-e29b-41d4-a716-446655440112', FALSE, NOW(), NOW(), FALSE); -- High Traffic

-- MALL (320e8400-e29b-41d4-a716-446655440010)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440010', '410e8400-e29b-41d4-a716-446655440007', FALSE, NOW(), NOW(), FALSE), -- Floors
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440010', '410e8400-e29b-41d4-a716-446655440113', FALSE, NOW(), NOW(), FALSE), -- Shops
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440010', '410e8400-e29b-41d4-a716-446655440114', FALSE, NOW(), NOW(), FALSE), -- Cinema
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440010', '410e8400-e29b-41d4-a716-446655440115', FALSE, NOW(), NOW(), FALSE), -- Food Court
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440010', '410e8400-e29b-41d4-a716-446655440015', FALSE, NOW(), NOW(), FALSE); -- Parking

-- RESTAURANT (320e8400-e29b-41d4-a716-446655440011)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440011', '410e8400-e29b-41d4-a716-446655440106', TRUE, NOW(), NOW(), FALSE),  -- Width
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440011', '410e8400-e29b-41d4-a716-446655440107', FALSE, NOW(), NOW(), FALSE), -- Depth
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440011', '410e8400-e29b-41d4-a716-446655440116', FALSE, NOW(), NOW(), FALSE), -- Tables
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440011', '410e8400-e29b-41d4-a716-446655440015', FALSE, NOW(), NOW(), FALSE), -- Parking
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440011', '410e8400-e29b-41d4-a716-446655440117', FALSE, NOW(), NOW(), FALSE); -- Kitchen

-- HOTEL (320e8400-e29b-41d4-a716-446655440012)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440012', '410e8400-e29b-41d4-a716-446655440007', FALSE, NOW(), NOW(), FALSE), -- Floors
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440012', '410e8400-e29b-41d4-a716-446655440118', TRUE, NOW(), NOW(), FALSE),  -- Rooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440012', '410e8400-e29b-41d4-a716-446655440119', FALSE, NOW(), NOW(), FALSE), -- Stars
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440012', '410e8400-e29b-41d4-a716-446655440013', FALSE, NOW(), NOW(), FALSE), -- Pool
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440012', '410e8400-e29b-41d4-a716-446655440120', FALSE, NOW(), NOW(), FALSE), -- Restaurant
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440012', '410e8400-e29b-41d4-a716-446655440019', FALSE, NOW(), NOW(), FALSE), -- Gym
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440012', '410e8400-e29b-41d4-a716-446655440015', FALSE, NOW(), NOW(), FALSE); -- Parking

-- WAREHOUSE (320e8400-e29b-41d4-a716-446655440013)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440013', '410e8400-e29b-41d4-a716-446655440201', FALSE, NOW(), NOW(), FALSE), -- Height
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440013', '410e8400-e29b-41d4-a716-446655440202', FALSE, NOW(), NOW(), FALSE), -- Truck Parking
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440013', '410e8400-e29b-41d4-a716-446655440203', FALSE, NOW(), NOW(), FALSE), -- Gates
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440013', '410e8400-e29b-41d4-a716-446655440204', FALSE, NOW(), NOW(), FALSE), -- Railway
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440013', '410e8400-e29b-41d4-a716-446655440205', FALSE, NOW(), NOW(), FALSE); -- Water

-- FACTORY (320e8400-e29b-41d4-a716-446655440014)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440014', '410e8400-e29b-41d4-a716-446655440201', FALSE, NOW(), NOW(), FALSE), -- Height
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440014', '410e8400-e29b-41d4-a716-446655440206', FALSE, NOW(), NOW(), FALSE), -- Power
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440014', '410e8400-e29b-41d4-a716-446655440101', FALSE, NOW(), NOW(), FALSE), -- Office Rooms
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440014', '410e8400-e29b-41d4-a716-446655440015', FALSE, NOW(), NOW(), FALSE), -- Parking
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440014', '410e8400-e29b-41d4-a716-446655440207', FALSE, NOW(), NOW(), FALSE); -- Drainage

-- WORKSHOP (320e8400-e29b-41d4-a716-446655440015)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440015', '410e8400-e29b-41d4-a716-446655440201', FALSE, NOW(), NOW(), FALSE), -- Height
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440015', '410e8400-e29b-41d4-a716-446655440206', FALSE, NOW(), NOW(), FALSE), -- Power
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440015', '410e8400-e29b-41d4-a716-446655440203', FALSE, NOW(), NOW(), FALSE), -- Gates
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440015', '410e8400-e29b-41d4-a716-446655440015', FALSE, NOW(), NOW(), FALSE), -- Parking
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440015', '410e8400-e29b-41d4-a716-446655440103', FALSE, NOW(), NOW(), FALSE); -- Restrooms

-- LOGISTICS (320e8400-e29b-41d4-a716-446655440016)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440016', '410e8400-e29b-41d4-a716-446655440201', FALSE, NOW(), NOW(), FALSE), -- Height
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440016', '410e8400-e29b-41d4-a716-446655440208', FALSE, NOW(), NOW(), FALSE), -- Entrances
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440016', '410e8400-e29b-41d4-a716-446655440204', FALSE, NOW(), NOW(), FALSE), -- Railway
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440016', '410e8400-e29b-41d4-a716-446655440202', FALSE, NOW(), NOW(), FALSE), -- Truck Parking
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440016', '410e8400-e29b-41d4-a716-446655440209', FALSE, NOW(), NOW(), FALSE), -- Security
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440016', '410e8400-e29b-41d4-a716-446655440210', FALSE, NOW(), NOW(), FALSE); -- Cold Storage

-- LAND_RESIDENTIAL (320e8400-e29b-41d4-a716-446655440017)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440017', '410e8400-e29b-41d4-a716-446655440301', TRUE, NOW(), NOW(), FALSE),  -- Frontage
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440017', '410e8400-e29b-41d4-a716-446655440302', FALSE, NOW(), NOW(), FALSE), -- Depth
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440017', '410e8400-e29b-41d4-a716-446655440304', FALSE, NOW(), NOW(), FALSE); -- Planning

-- LAND_COMMERCIAL (320e8400-e29b-41d4-a716-446655440018)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440018', '410e8400-e29b-41d4-a716-446655440301', TRUE, NOW(), NOW(), FALSE),  -- Frontage
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440018', '410e8400-e29b-41d4-a716-446655440302', FALSE, NOW(), NOW(), FALSE), -- Depth
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440018', '410e8400-e29b-41d4-a716-446655440305', FALSE, NOW(), NOW(), FALSE), -- Purpose
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440018', '410e8400-e29b-41d4-a716-446655440306', FALSE, NOW(), NOW(), FALSE); -- Height Planning

-- LAND_INDUSTRIAL (320e8400-e29b-41d4-a716-446655440019)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440019', '410e8400-e29b-41d4-a716-446655440301', TRUE, NOW(), NOW(), FALSE),  -- Frontage
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440019', '410e8400-e29b-41d4-a716-446655440302', FALSE, NOW(), NOW(), FALSE), -- Depth
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440019', '410e8400-e29b-41d4-a716-446655440307', FALSE, NOW(), NOW(), FALSE), -- Zone
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440019', '410e8400-e29b-41d4-a716-446655440206', FALSE, NOW(), NOW(), FALSE), -- Power
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440019', '410e8400-e29b-41d4-a716-446655440205', FALSE, NOW(), NOW(), FALSE); -- Water

-- LAND_AGRICULTURAL (320e8400-e29b-41d4-a716-446655440020)
INSERT INTO property_type_attributes (property_type_attribute_id, property_type_id, property_attribute_id, is_required, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440020', '410e8400-e29b-41d4-a716-446655440308', FALSE, NOW(), NOW(), FALSE), -- Crop Type
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440020', '410e8400-e29b-41d4-a716-446655440309', FALSE, NOW(), NOW(), FALSE), -- Water Source
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440020', '410e8400-e29b-41d4-a716-446655440310', FALSE, NOW(), NOW(), FALSE), -- Irrigation
    (gen_random_uuid(), '320e8400-e29b-41d4-a716-446655440020', '410e8400-e29b-41d4-a716-446655440311', FALSE, NOW(), NOW(), FALSE); -- Access Road
