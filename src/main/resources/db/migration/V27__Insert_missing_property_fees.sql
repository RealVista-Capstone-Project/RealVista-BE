-- V27__Insert_missing_property_fees.sql
-- Migration V27: Insert Missing Property Fees (WATER, ELECTRICITY, GARBAGE, SECURITY, PARKING, INTERNET)
-- Reason: V26 only inserted MANAGEMENT fees. This migration adds the remaining fee types.
--
-- ============================================================================
-- RESIDENTIAL - WATER FEES
-- ============================================================================
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

-- ============================================================================
-- RESIDENTIAL - GARBAGE FEES
-- ============================================================================
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
-- RESIDENTIAL - SECURITY FEES (Optional)
-- ============================================================================
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
-- RESIDENTIAL - PARKING FEES (Optional)
-- ============================================================================
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

-- ============================================================================
-- RESIDENTIAL - INTERNET FEES (Optional)
-- ============================================================================
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
-- COMMERCIAL - ELECTRICITY FEES
-- ============================================================================
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

-- ============================================================================
-- COMMERCIAL - WATER FEES
-- ============================================================================
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

-- ============================================================================
-- COMMERCIAL - GARBAGE FEES
-- ============================================================================
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

-- ============================================================================
-- COMMERCIAL - SECURITY FEES (Optional)
-- ============================================================================
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
-- COMMERCIAL - INTERNET FEES (Optional)
-- ============================================================================
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

-- ============================================================================
-- COMMERCIAL - PARKING FEES (Optional)
-- ============================================================================
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
-- INDUSTRIAL - ELECTRICITY FEES
-- ============================================================================
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

-- ============================================================================
-- INDUSTRIAL - WATER FEES
-- ============================================================================
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

-- ============================================================================
-- INDUSTRIAL - PARKING FEES (Optional)
-- ============================================================================
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
