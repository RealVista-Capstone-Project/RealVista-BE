-- V26__Insert_property_fee_services.sql
-- Migration V26: Insert Property Fee Services with Type-based Assignment
-- CORRECTED: Using actual property type codes from database
--
-- Fee Strategy by Property Type:
-- RESIDENTIAL (APARTMENT, HOUSE, VILLA, TOWNHOUSE, PENTHOUSE, STUDIO):
--   - MANAGEMENT: Always
--   - WATER: Always
--   - GARBAGE: Always
--   - PARKING: Optional
--   - SECURITY: Optional
--   - INTERNET: Optional
--
-- COMMERCIAL (OFFICE, RETAIL, RESTAURANT, HOTEL, SHOPHOUSE, MALL):
--   - MANAGEMENT: Always
--   - WATER: Always (except OFFICE, RETAIL)
--   - ELECTRICITY: Always
--   - GARBAGE: Always
--   - PARKING: Optional
--   - SECURITY: Optional
--   - INTERNET: Optional
--
-- INDUSTRIAL (WAREHOUSE, FACTORY, WORKSHOP, LOGISTICS):
--   - MANAGEMENT: Always
--   - ELECTRICITY: Always
--   - WATER: Always
--   - PARKING: Optional
--
-- LAND (LAND_*): No recurring fees
--
-- ============================================================================
-- PROPERTY TYPE: APARTMENT
-- ============================================================================
INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'MANAGEMENT',
    'Phí quản lý chung cư',
    250000.00,
    'MONTHLY',
    FALSE,
    'Phí quản lý toà nhà, bảo trì chung'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'APARTMENT')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'MANAGEMENT');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'WATER',
    'Phí cấp nước',
    35000.00,
    'MONTHLY',
    FALSE,
    'Chi phí cấp nước sinh hoạt'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'APARTMENT')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'WATER');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'GARBAGE',
    'Phí xử lý rác thải',
    50000.00,
    'MONTHLY',
    FALSE,
    'Chi phí xử lý rác thải'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'APARTMENT')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'GARBAGE');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'PARKING',
    'Phí đỗ xe',
    100000.00,
    'MONTHLY',
    TRUE,
    'Phí đỗ xe tính theo chỗ'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'APARTMENT')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'PARKING');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'SECURITY',
    'Phí bảo vệ',
    150000.00,
    'MONTHLY',
    FALSE,
    'Dịch vụ bảo vệ 24/7'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'APARTMENT')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'SECURITY');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'INTERNET',
    'Phí internet',
    80000.00,
    'MONTHLY',
    TRUE,
    'Dịch vụ WiFi cao tốc'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'APARTMENT')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'INTERNET');

-- ============================================================================
-- PROPERTY TYPE: HOUSE
-- ============================================================================
INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'MANAGEMENT',
    'Phí quản lý',
    120000.00,
    'MONTHLY',
    FALSE,
    'Phí bảo trì tài sản'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'HOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'MANAGEMENT');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'WATER',
    'Phí cấp nước',
    30000.00,
    'MONTHLY',
    FALSE,
    'Chi phí cấp nước sinh hoạt'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'HOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'WATER');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'GARBAGE',
    'Phí xử lý rác thải',
    40000.00,
    'MONTHLY',
    FALSE,
    'Chi phí xử lý rác thải'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'HOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'GARBAGE');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'SECURITY',
    'Phí bảo vệ',
    80000.00,
    'MONTHLY',
    FALSE,
    'Dịch vụ bảo vệ'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'HOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'SECURITY');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'INTERNET',
    'Phí internet',
    70000.00,
    'MONTHLY',
    TRUE,
    'Dịch vụ internet'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'HOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'INTERNET');

-- ============================================================================
-- PROPERTY TYPE: VILLA
-- ============================================================================
INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'MANAGEMENT',
    'Phí quản lý biệt thự',
    180000.00,
    'MONTHLY',
    FALSE,
    'Phí bảo trì bảo dưỡng'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'VILLA')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'MANAGEMENT');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'WATER',
    'Phí cấp nước biệt thự',
    40000.00,
    'MONTHLY',
    FALSE,
    'Chi phí cấp nước'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'VILLA')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'WATER');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'GARBAGE',
    'Phí xử lý rác thải',
    45000.00,
    'MONTHLY',
    FALSE,
    'Chi phí xử lý rác thải'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'VILLA')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'GARBAGE');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'SECURITY',
    'Phí bảo vệ VIP',
    120000.00,
    'MONTHLY',
    FALSE,
    'Dịch vụ bảo vệ cao cấp'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'VILLA')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'SECURITY');

-- ============================================================================
-- PROPERTY TYPE: TOWNHOUSE
-- ============================================================================
INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'MANAGEMENT',
    'Phí quản lý dãy nhà phố',
    100000.00,
    'MONTHLY',
    FALSE,
    'Phí bảo trì chung'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'TOWNHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'MANAGEMENT');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'WATER',
    'Phí cấp nước',
    25000.00,
    'MONTHLY',
    FALSE,
    'Chi phí cấp nước'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'TOWNHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'WATER');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'GARBAGE',
    'Phí xử lý rác thải',
    35000.00,
    'MONTHLY',
    FALSE,
    'Chi phí xử lý rác thải'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'TOWNHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'GARBAGE');

-- ============================================================================
-- PROPERTY TYPE: PENTHOUSE
-- ============================================================================
INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'MANAGEMENT',
    'Phí quản lý penthouse',
    400000.00,
    'MONTHLY',
    FALSE,
    'Phí quản lý cao cấp'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'PENTHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'MANAGEMENT');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'WATER',
    'Phí cấp nước',
    50000.00,
    'MONTHLY',
    FALSE,
    'Chi phí cấp nước'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'PENTHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'WATER');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'GARBAGE',
    'Phí xử lý rác thải',
    60000.00,
    'MONTHLY',
    FALSE,
    'Chi phí xử lý rác thải'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'PENTHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'GARBAGE');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'SECURITY',
    'Phí bảo vệ VIP',
    200000.00,
    'MONTHLY',
    FALSE,
    'Dịch vụ bảo vệ độc quyền'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'PENTHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'SECURITY');

-- ============================================================================
-- PROPERTY TYPE: STUDIO
-- ============================================================================
INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'MANAGEMENT',
    'Phí quản lý studio',
    150000.00,
    'MONTHLY',
    FALSE,
    'Phí quản lý studio'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'STUDIO')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'MANAGEMENT');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'WATER',
    'Phí cấp nước',
    20000.00,
    'MONTHLY',
    FALSE,
    'Chi phí cấp nước'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'STUDIO')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'WATER');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'GARBAGE',
    'Phí xử lý rác thải',
    30000.00,
    'MONTHLY',
    FALSE,
    'Chi phí xử lý rác thải'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'STUDIO')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'GARBAGE');

-- ============================================================================
-- PROPERTY TYPE: OFFICE
-- ============================================================================
INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'MANAGEMENT',
    'Phí quản lý tòa nhà',
    350000.00,
    'MONTHLY',
    FALSE,
    'Phí bảo trì toà nhà, điều hòa, thang máy'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'OFFICE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'MANAGEMENT');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'ELECTRICITY',
    'Phí điện chung',
    80000.00,
    'MONTHLY',
    FALSE,
    'Chi phí điện khu vực chung'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'OFFICE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'ELECTRICITY');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'GARBAGE',
    'Phí xử lý rác thải',
    55000.00,
    'MONTHLY',
    FALSE,
    'Chi phí xử lý rác thải'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'OFFICE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'GARBAGE');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'PARKING',
    'Phí đỗ xe',
    150000.00,
    'MONTHLY',
    TRUE,
    'Phí đỗ xe tính theo chỗ'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'OFFICE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'PARKING');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'SECURITY',
    'Phí bảo vệ',
    200000.00,
    'MONTHLY',
    FALSE,
    'Dịch vụ bảo vệ 24/7'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'OFFICE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'SECURITY');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'INTERNET',
    'Phí internet',
    120000.00,
    'MONTHLY',
    TRUE,
    'Dịch vụ internet cao tốc'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'OFFICE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'INTERNET');

-- ============================================================================
-- PROPERTY TYPE: RETAIL
-- ============================================================================
INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'MANAGEMENT',
    'Phí quản lý trung tâm thương mại',
    280000.00,
    'MONTHLY',
    FALSE,
    'Phí bảo trì khu vực chung'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'RETAIL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'MANAGEMENT');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'ELECTRICITY',
    'Phí điện chung',
    90000.00,
    'MONTHLY',
    FALSE,
    'Chi phí điện khu vực chung'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'RETAIL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'ELECTRICITY');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'GARBAGE',
    'Phí xử lý rác thải',
    50000.00,
    'MONTHLY',
    FALSE,
    'Chi phí xử lý rác thải'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'RETAIL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'GARBAGE');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'PARKING',
    'Phí đỗ xe',
    120000.00,
    'MONTHLY',
    TRUE,
    'Phí đỗ xe khách hàng'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'RETAIL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'PARKING');

-- ============================================================================
-- PROPERTY TYPE: RESTAURANT
-- ============================================================================
INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'MANAGEMENT',
    'Phí quản lý nhà hàng',
    200000.00,
    'MONTHLY',
    FALSE,
    'Phí bảo trì bếp, hệ thống'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'RESTAURANT')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'MANAGEMENT');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'ELECTRICITY',
    'Phí điện nhà hàng',
    250000.00,
    'MONTHLY',
    FALSE,
    'Chi phí điện bếp, điều hòa'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'RESTAURANT')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'ELECTRICITY');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'WATER',
    'Phí cấp nước nhà hàng',
    120000.00,
    'MONTHLY',
    FALSE,
    'Chi phí nước nấu ăn, sinh hoạt'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'RESTAURANT')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'WATER');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'GARBAGE',
    'Phí xử lý rác thải',
    70000.00,
    'MONTHLY',
    FALSE,
    'Chi phí xử lý rác thải'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'RESTAURANT')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'GARBAGE');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'SECURITY',
    'Phí bảo vệ',
    100000.00,
    'MONTHLY',
    FALSE,
    'Dịch vụ bảo vệ'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'RESTAURANT')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'SECURITY');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'PARKING',
    'Phí đỗ xe',
    80000.00,
    'MONTHLY',
    TRUE,
    'Phí đỗ xe khách'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'RESTAURANT')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'PARKING');

-- ============================================================================
-- PROPERTY TYPE: HOTEL
-- ============================================================================
INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'MANAGEMENT',
    'Phí quản lý khách sạn',
    400000.00,
    'MONTHLY',
    FALSE,
    'Phí bảo trì tòa nhà, các tiện nghi'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'HOTEL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'MANAGEMENT');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'ELECTRICITY',
    'Phí điện khách sạn',
    350000.00,
    'MONTHLY',
    FALSE,
    'Chi phí điện phòng, điều hòa, chiếu sáng'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'HOTEL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'ELECTRICITY');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'WATER',
    'Phí cấp nước khách sạn',
    180000.00,
    'MONTHLY',
    FALSE,
    'Chi phí nước phòng, bếp, vệ sinh'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'HOTEL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'WATER');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'GARBAGE',
    'Phí xử lý rác thải',
    80000.00,
    'MONTHLY',
    FALSE,
    'Chi phí xử lý rác thải'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'HOTEL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'GARBAGE');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'SECURITY',
    'Phí bảo vệ',
    250000.00,
    'MONTHLY',
    FALSE,
    'Dịch vụ bảo vệ 24/7'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'HOTEL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'SECURITY');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'INTERNET',
    'Phí internet',
    180000.00,
    'MONTHLY',
    FALSE,
    'Dịch vụ WiFi cao tốc miễn phí'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'HOTEL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'INTERNET');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'PARKING',
    'Phí đỗ xe khách hàng',
    150000.00,
    'MONTHLY',
    TRUE,
    'Phí đỗ xe'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'HOTEL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'PARKING');

-- ============================================================================
-- PROPERTY TYPE: SHOPHOUSE
-- ============================================================================
INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'MANAGEMENT',
    'Phí quản lý shophouse',
    200000.00,
    'MONTHLY',
    FALSE,
    'Phí bảo trì chung'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'SHOPHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'MANAGEMENT');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'ELECTRICITY',
    'Phí điện shophouse',
    180000.00,
    'MONTHLY',
    FALSE,
    'Chi phí điện khu vực thương mại'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'SHOPHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'ELECTRICITY');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'WATER',
    'Phí cấp nước shophouse',
    65000.00,
    'MONTHLY',
    FALSE,
    'Chi phí cấp nước'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'SHOPHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'WATER');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'GARBAGE',
    'Phí xử lý rác thải',
    50000.00,
    'MONTHLY',
    FALSE,
    'Chi phí xử lý rác thải'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'SHOPHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'GARBAGE');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'PARKING',
    'Phí đỗ xe',
    100000.00,
    'MONTHLY',
    TRUE,
    'Phí đỗ xe'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'SHOPHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'PARKING');

-- ============================================================================
-- PROPERTY TYPE: MALL
-- ============================================================================
INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'MANAGEMENT',
    'Phí quản lý trung tâm thương mại',
    500000.00,
    'MONTHLY',
    FALSE,
    'Phí bảo trì tòa nhà, điều hòa, thang máy'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'MALL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'MANAGEMENT');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'ELECTRICITY',
    'Phí điện trung tâm thương mại',
    450000.00,
    'MONTHLY',
    FALSE,
    'Chi phí điện chiếu sáng, điều hòa, cơ sở hạ tầng'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'MALL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'ELECTRICITY');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'WATER',
    'Phí cấp nước trung tâm',
    220000.00,
    'MONTHLY',
    FALSE,
    'Chi phí nước khu vực chung, nhà vệ sinh'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'MALL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'WATER');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'GARBAGE',
    'Phí xử lý rác thải',
    90000.00,
    'MONTHLY',
    FALSE,
    'Chi phí xử lý rác thải'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'MALL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'GARBAGE');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'SECURITY',
    'Phí bảo vệ',
    300000.00,
    'MONTHLY',
    FALSE,
    'Dịch vụ bảo vệ 24/7'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'MALL')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'SECURITY');

-- ============================================================================
-- PROPERTY TYPE: WAREHOUSE
-- ============================================================================
INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'MANAGEMENT',
    'Phí quản lý kho bãi',
    150000.00,
    'MONTHLY',
    FALSE,
    'Phí bảo trì, vệ sinh khu vực'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'WAREHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'MANAGEMENT');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'ELECTRICITY',
    'Phí điện 3 pha',
    200000.00,
    'MONTHLY',
    FALSE,
    'Chi phí điện 3 pha'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'WAREHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'ELECTRICITY');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'WATER',
    'Phí cấp nước',
    100000.00,
    'MONTHLY',
    FALSE,
    'Chi phí cấp nước công nghiệp'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'WAREHOUSE')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'WATER');

-- ============================================================================
-- PROPERTY TYPE: FACTORY
-- ============================================================================
INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'MANAGEMENT',
    'Phí quản lý nhà máy',
    200000.00,
    'MONTHLY',
    FALSE,
    'Phí bảo trì, vệ sinh khu vực'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'FACTORY')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'MANAGEMENT');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'ELECTRICITY',
    'Phí điện 3 pha',
    300000.00,
    'MONTHLY',
    FALSE,
    'Chi phí điện 3 pha công suất cao'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'FACTORY')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'ELECTRICITY');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'WATER',
    'Phí cấp nước',
    150000.00,
    'MONTHLY',
    FALSE,
    'Chi phí cấp nước công nghiệp'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'FACTORY')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'WATER');

-- ============================================================================
-- PROPERTY TYPE: WORKSHOP
-- ============================================================================
INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'MANAGEMENT',
    'Phí quản lý xưởng',
    120000.00,
    'MONTHLY',
    FALSE,
    'Phí bảo trì, vệ sinh'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'WORKSHOP')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'MANAGEMENT');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'ELECTRICITY',
    'Phí điện 3 pha',
    180000.00,
    'MONTHLY',
    FALSE,
    'Chi phí điện 3 pha'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'WORKSHOP')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'ELECTRICITY');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'WATER',
    'Phí cấp nước xưởng',
    110000.00,
    'MONTHLY',
    FALSE,
    'Chi phí cấp nước công nghiệp'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'WORKSHOP')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'WATER');

-- ============================================================================
-- PROPERTY TYPE: LOGISTICS
-- ============================================================================
INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'MANAGEMENT',
    'Phí quản lý logistics',
    250000.00,
    'MONTHLY',
    FALSE,
    'Phí bảo trì, vệ sinh khu vực lớn'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'LOGISTICS')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'MANAGEMENT');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'ELECTRICITY',
    'Phí điện 3 pha',
    250000.00,
    'MONTHLY',
    FALSE,
    'Chi phí điện công suất lớn'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'LOGISTICS')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'ELECTRICITY');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'WATER',
    'Phí cấp nước logistics',
    180000.00,
    'MONTHLY',
    FALSE,
    'Chi phí cấp nước khu vực lớn'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'LOGISTICS')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'WATER');

INSERT INTO property_fee_services (property_id, fee_type, fee_name, amount, billing_cycle, is_optional, description)
SELECT 
    p.property_id,
    'PARKING',
    'Phí đỗ xe tải',
    120000.00,
    'MONTHLY',
    FALSE,
    'Phí đỗ xe tải'
FROM properties p
WHERE p.property_type_id = (SELECT property_type_id FROM property_types WHERE code = 'LOGISTICS')
  AND p.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM property_fee_services pfs WHERE pfs.property_id = p.property_id AND pfs.fee_type = 'PARKING');

-- ============================================================================
-- NO FEES FOR LAND TYPES
-- ============================================================================
-- Land properties have no recurring management fees
-- LAND_RESIDENTIAL, LAND_COMMERCIAL, LAND_INDUSTRIAL, LAND_AGRICULTURAL
