-- V16__Insert_property_attribute_ranges.sql
-- Create range definitions for all property attributes (for filtering)
-- Format: label, min_value, max_value

-- ============================================================================
-- RESIDENTIAL ATTRIBUTES RANGES
-- ============================================================================

-- BEDROOMS (410e8400-e29b-41d4-a716-446655440001)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440001', '1 Phòng', 1, 1, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440001', '2-3 Phòng', 2, 3, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440001', '4-5 Phòng', 4, 5, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440001', '6+ Phòng', 6, NULL, 4, NOW(), NOW(), FALSE);

-- BATHROOMS (410e8400-e29b-41d4-a716-446655440002)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440002', '1 Phòng', 1, 1, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440002', '2-3 Phòng', 2, 3, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440002', '4+ Phòng', 4, NULL, 3, NOW(), NOW(), FALSE);

-- FLOOR (410e8400-e29b-41d4-a716-446655440003)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440003', 'Tầng 1-2', 1, 2, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440003', 'Tầng 3-5', 3, 5, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440003', 'Tầng 6-10', 6, 10, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440003', 'Tầng 11-20', 11, 20, 4, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440003', 'Tầng 21-30', 21, 30, 5, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440003', 'Tầng 31-40', 31, 40, 6, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440003', 'Tầng 40+', 40, NULL, 7, NOW(), NOW(), FALSE);

-- DIRECTION (410e8400-e29b-41d4-a716-446655440004)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440004', 'Hướng Bắc', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440004', 'Hướng Nam', NULL, NULL, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440004', 'Hướng Đông', NULL, NULL, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440004', 'Hướng Tây', NULL, NULL, 4, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440004', 'Hướng Đông Bắc', NULL, NULL, 5, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440004', 'Hướng Tây Bắc', NULL, NULL, 6, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440004', 'Hướng Đông Nam', NULL, NULL, 7, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440004', 'Hướng Tây Nam', NULL, NULL, 8, NOW(), NOW(), FALSE);

-- GARDEN (410e8400-e29b-41d4-a716-446655440005)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440005', 'Có Sân Vườn', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440005', 'Không Có Sân Vườn', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- GARAGE (410e8400-e29b-41d4-a716-446655440006)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440006', 'Có Nhà Để Xe', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440006', 'Không Có Nhà Để Xe', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- FLOORS (410e8400-e29b-41d4-a716-446655440007)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440007', '1 Tầng', 1, 1, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440007', '2-3 Tầng', 2, 3, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440007', '4-5 Tầng', 4, 5, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440007', '6-8 Tầng', 6, 8, 4, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440007', '9+ Tầng', 9, NULL, 5, NOW(), NOW(), FALSE);

-- BALCONY (410e8400-e29b-41d4-a716-446655440008)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440008', 'Có Ban Công', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440008', 'Không Ban Công', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- AC (410e8400-e29b-41d4-a716-446655440009)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440009', 'Có Điều Hòa', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440009', 'Không Điều Hòa', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- TOTAL_FLOORS (410e8400-e29b-41d4-a716-446655440010)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440010', '5-10 Tầng', 5, 10, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440010', '11-20 Tầng', 11, 20, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440010', '21-30 Tầng', 21, 30, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440010', '31-40 Tầng', 31, 40, 4, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440010', '41-50 Tầng', 41, 50, 5, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440010', '50+ Tầng', 50, NULL, 6, NOW(), NOW(), FALSE);

-- BALCONY_TYPE (410e8400-e29b-41d4-a716-446655440011)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440011', 'Hướng Bắc', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440011', 'Hướng Nam', NULL, NULL, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440011', 'Hướng Đông', NULL, NULL, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440011', 'Hướng Tây', NULL, NULL, 4, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440011', 'Đa Hướng', NULL, NULL, 5, NOW(), NOW(), FALSE);

-- GARAGE_AREA (410e8400-e29b-41d4-a716-446655440012)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440012', 'Dưới 20m²', NULL, 20, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440012', '20-50m²', 20, 50, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440012', '50-100m²', 50, 100, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440012', '100m² trở lên', 100, NULL, 4, NOW(), NOW(), FALSE);

-- POOL (410e8400-e29b-41d4-a716-446655440013)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440013', 'Có Hồ Bơi', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440013', 'Không Hồ Bơi', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- TENNIS (410e8400-e29b-41d4-a716-446655440014)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440014', 'Có Tennis', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440014', 'Không Tennis', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- PARKING (410e8400-e29b-41d4-a716-446655440015)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440015', '1-2 Chỗ', 1, 2, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440015', '3-5 Chỗ', 3, 5, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440015', '6+ Chỗ', 6, NULL, 3, NOW(), NOW(), FALSE);

-- TOP_FLOOR (410e8400-e29b-41d4-a716-446655440016)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440016', 'Tầng Cao Nhất', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440016', 'Không Phải', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- LARGE_BALCONY (410e8400-e29b-41d4-a716-446655440017)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440017', 'Có Ban Công Lớn', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440017', 'Không Ban Công Lớn', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- VIEW (410e8400-e29b-41d4-a716-446655440018)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440018', 'Nhìn Ra Thành Phố', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440018', 'Nhìn Ra Sân Vườn', NULL, NULL, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440018', 'Nhìn Ra Biển', NULL, NULL, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440018', 'Nhìn Ra Núi', NULL, NULL, 4, NOW(), NOW(), FALSE);

-- GYM (410e8400-e29b-41d4-a716-446655440019)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440019', 'Có Gym', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440019', 'Không Gym', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- ROOMS (Studio) (410e8400-e29b-41d4-a716-446655440020)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440020', 'Studio', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440020', '1-2 Phòng', NULL, NULL, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440020', '3+ Phòng', NULL, NULL, 3, NOW(), NOW(), FALSE);

-- WIFI (410e8400-e29b-41d4-a716-446655440021)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440021', 'Có WiFi Miễn Phí', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440021', 'Không WiFi', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- KITCHEN (410e8400-e29b-41d4-a716-446655440022)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440022', 'Có Dụng Cụ Nhà Bếp', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440022', 'Không Dụng Cụ', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- ============================================================================
-- COMMERCIAL ATTRIBUTES RANGES
-- ============================================================================

-- OFFICE_ROOMS (410e8400-e29b-41d4-a716-446655440101)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440101', '1-2 Phòng', 1, 2, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440101', '3-5 Phòng', 3, 5, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440101', '6+ Phòng', 6, NULL, 3, NOW(), NOW(), FALSE);

-- MEETING_ROOMS (410e8400-e29b-41d4-a716-446655440102)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440102', '1-2 Phòng', 1, 2, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440102', '3+ Phòng', 3, NULL, 2, NOW(), NOW(), FALSE);

-- RESTROOMS (410e8400-e29b-41d4-a716-446655440103)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440103', '1-2 Phòng', 1, 2, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440103', '3+ Phòng', 3, NULL, 2, NOW(), NOW(), FALSE);

-- INDIVIDUAL_AC (410e8400-e29b-41d4-a716-446655440104)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440104', 'Có Điều Hòa Riêng', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440104', 'Không', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- RECEPTION (410e8400-e29b-41d4-a716-446655440105)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440105', 'Có Lễ Tân', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440105', 'Không Lễ Tân', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- WIDTH (410e8400-e29b-41d4-a716-446655440106)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440106', 'Dưới 5m', NULL, 5, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440106', '5-8m', 5, 8, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440106', '8-12m', 8, 12, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440106', '12-20m', 12, 20, 4, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440106', '20m trở lên', 20, NULL, 5, NOW(), NOW(), FALSE);

-- DEPTH (410e8400-e29b-41d4-a716-446655440107)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440107', 'Dưới 10m', NULL, 10, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440107', '10-15m', 10, 15, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440107', '15-25m', 15, 25, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440107', '25-40m', 25, 40, 4, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440107', '40m trở lên', 40, NULL, 5, NOW(), NOW(), FALSE);

-- UPPER_BEDROOMS (410e8400-e29b-41d4-a716-446655440108)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440108', 'Không Có', 0, 0, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440108', '1 Phòng', 1, 1, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440108', '2 Phòng', 2, 2, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440108', '3+ Phòng', 3, NULL, 4, NOW(), NOW(), FALSE);

-- UPPER_BATHROOMS (410e8400-e29b-41d4-a716-446655440109)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440109', 'Không Có', 0, 0, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440109', '1 Phòng', 1, 1, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440109', '2+ Phòng', 2, NULL, 3, NOW(), NOW(), FALSE);

-- SHOP (410e8400-e29b-41d4-a716-446655440110)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440110', 'Có Cửa Hàng', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440110', 'Không Cửa Hàng', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- DISPLAY_WINDOW (410e8400-e29b-41d4-a716-446655440111)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440111', 'Có Cửa Sổ', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440111', 'Không Cửa Sổ', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- HIGH_TRAFFIC (410e8400-e29b-41d4-a716-446655440112)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440112', 'Lưu Thông Cao', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440112', 'Lưu Thông Thấp', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- SHOPS (Mall) (410e8400-e29b-41d4-a716-446655440113)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440113', 'Dưới 30 Cửa', NULL, 30, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440113', '30-50 Cửa', 30, 50, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440113', '50-100 Cửa', 50, 100, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440113', '100-200 Cửa', 100, 200, 4, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440113', '200+ Cửa', 200, NULL, 5, NOW(), NOW(), FALSE);

-- CINEMA (410e8400-e29b-41d4-a716-446655440114)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440114', 'Có Rạp Phim', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440114', 'Không Rạp Phim', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- FOOD_COURT (410e8400-e29b-41d4-a716-446655440115)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440115', 'Có Khu Ăn Uống', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440115', 'Không Khu Ăn Uống', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- TABLES (410e8400-e29b-41d4-a716-446655440116)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440116', 'Dưới 10 Bàn', NULL, 10, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440116', '10-20 Bàn', 10, 20, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440116', '20-50 Bàn', 20, 50, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440116', '50+ Bàn', 50, NULL, 4, NOW(), NOW(), FALSE);

-- RESTAURANT_KITCHEN (410e8400-e29b-41d4-a716-446655440117)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440117', 'Có Bếp', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440117', 'Không Bếp', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- ROOMS_HOTEL (410e8400-e29b-41d4-a716-446655440118)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440118', 'Dưới 50 Phòng', NULL, 50, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440118', '50-100 Phòng', 50, 100, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440118', '100-200 Phòng', 100, 200, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440118', '200-300 Phòng', 200, 300, 4, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440118', '300+ Phòng', 300, NULL, 5, NOW(), NOW(), FALSE);

-- STARS (410e8400-e29b-41d4-a716-446655440119)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440119', '1 Sao', 1, 1, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440119', '2 Sao', 2, 2, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440119', '3 Sao', 3, 3, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440119', '4 Sao', 4, 4, 4, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440119', '5 Sao', 5, 5, 5, NOW(), NOW(), FALSE);

-- RESTAURANT (Hotel) (410e8400-e29b-41d4-a716-446655440120)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440120', 'Có Nhà Hàng', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440120', 'Không Nhà Hàng', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- ============================================================================
-- INDUSTRIAL ATTRIBUTES RANGES
-- ============================================================================

-- HEIGHT (410e8400-e29b-41d4-a716-446655440201)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440201', 'Dưới 5m', NULL, 5, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440201', '5-8m', 5, 8, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440201', '8-12m', 8, 12, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440201', '12m trở lên', 12, NULL, 4, NOW(), NOW(), FALSE);

-- TRUCK_PARKING (410e8400-e29b-41d4-a716-446655440202)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440202', '1-5 Chỗ', 1, 5, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440202', '6-10 Chỗ', 6, 10, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440202', '10+ Chỗ', 10, NULL, 3, NOW(), NOW(), FALSE);

-- GATES (410e8400-e29b-41d4-a716-446655440203)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440203', '1 Cửa', 1, 1, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440203', '2-3 Cửa', 2, 3, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440203', '4+ Cửa', 4, NULL, 3, NOW(), NOW(), FALSE);

-- RAILWAY (410e8400-e29b-41d4-a716-446655440204)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440204', 'Có Đường Sắt', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440204', 'Không Đường Sắt', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- WATER (410e8400-e29b-41d4-a716-446655440205)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440205', 'Có Nguồn Nước', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440205', 'Không Nguồn Nước', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- POWER (410e8400-e29b-41d4-a716-446655440206)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440206', 'Điện Yếu', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440206', 'Điện Trung Bình', NULL, NULL, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440206', 'Điện Mạnh', NULL, NULL, 3, NOW(), NOW(), FALSE);

-- DRAINAGE (410e8400-e29b-41d4-a716-446655440207)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440207', 'Có Thoát Nước', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440207', 'Không Thoát Nước', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- ENTRANCES (410e8400-e29b-41d4-a716-446655440208)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440208', '1-2 Lối', 1, 2, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440208', '3-5 Lối', 3, 5, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440208', '6+ Lối', 6, NULL, 3, NOW(), NOW(), FALSE);

-- SECURITY (410e8400-e29b-41d4-a716-446655440209)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440209', 'Có Bảo Vệ', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440209', 'Không Bảo Vệ', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- COLD_STORAGE (410e8400-e29b-41d4-a716-446655440210)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440210', 'Có Kho Lạnh', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440210', 'Không Kho Lạnh', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- ============================================================================
-- LAND ATTRIBUTES RANGES
-- ============================================================================

-- FRONTAGE (410e8400-e29b-41d4-a716-446655440301)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440301', 'Dưới 5m', NULL, 5, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440301', '5-10m', 5, 10, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440301', '10-20m', 10, 20, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440301', '20-40m', 20, 40, 4, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440301', '40m trở lên', 40, NULL, 5, NOW(), NOW(), FALSE);

-- LAND_DEPTH (410e8400-e29b-41d4-a716-446655440302)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440302', 'Dưới 10m', NULL, 10, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440302', '10-20m', 10, 20, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440302', '20-30m', 20, 30, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440302', '30-50m', 30, 50, 4, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440302', '50m trở lên', 50, NULL, 5, NOW(), NOW(), FALSE);

-- PLANNING (410e8400-e29b-41d4-a716-446655440304)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440304', 'Quy Hoạch Sẵn', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440304', 'Quy Hoạch Giới Hạn', NULL, NULL, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440304', 'Quy Hoạch Hạn Chế', NULL, NULL, 3, NOW(), NOW(), FALSE);

-- PURPOSE (410e8400-e29b-41d4-a716-446655440305)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440305', 'Dân Cư', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440305', 'Thương Mại', NULL, NULL, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440305', 'Hỗn Hợp', NULL, NULL, 3, NOW(), NOW(), FALSE);

-- HEIGHT_PLANNING (410e8400-e29b-41d4-a716-446655440306)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440306', '5m', 5, 5, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440306', '10m', 10, 10, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440306', '15m', 15, 15, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440306', '20m', 20, 20, 4, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440306', '25m trở lên', 25, NULL, 5, NOW(), NOW(), FALSE);

-- ZONE (410e8400-e29b-41d4-a716-446655440307)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440307', 'Khu A', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440307', 'Khu B', NULL, NULL, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440307', 'Khu C', NULL, NULL, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440307', 'Khu D', NULL, NULL, 4, NOW(), NOW(), FALSE);

-- CROP_TYPE (410e8400-e29b-41d4-a716-446655440308)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440308', 'Lúa', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440308', 'Ngô', NULL, NULL, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440308', 'Rau Xanh', NULL, NULL, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440308', 'Trái Cây', NULL, NULL, 4, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440308', 'Khác', NULL, NULL, 5, NOW(), NOW(), FALSE);

-- WATER_SOURCE (410e8400-e29b-41d4-a716-446655440309)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440309', 'Giếng', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440309', 'Kênh', NULL, NULL, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440309', 'Hồ', NULL, NULL, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440309', 'Sông', NULL, NULL, 4, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440309', 'Nước Máy', NULL, NULL, 5, NOW(), NOW(), FALSE);

-- IRRIGATION (410e8400-e29b-41d4-a716-446655440310)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440310', 'Có Hệ Thống Tưới', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440310', 'Không Hệ Thống Tưới', NULL, NULL, 2, NOW(), NOW(), FALSE);

-- ACCESS_ROAD (410e8400-e29b-41d4-a716-446655440311)
INSERT INTO property_attribute_ranges (property_attribute_range_id, property_attribute_id, label, min_value, max_value, display_order, created_at, updated_at, deleted)
VALUES
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440311', 'Đường Yếu', NULL, NULL, 1, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440311', 'Đường Trung Bình', NULL, NULL, 2, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440311', 'Đường Tốt', NULL, NULL, 3, NOW(), NOW(), FALSE),
    (gen_random_uuid(), '410e8400-e29b-41d4-a716-446655440311', 'Đường Rất Tốt', NULL, NULL, 4, NOW(), NOW(), FALSE);
