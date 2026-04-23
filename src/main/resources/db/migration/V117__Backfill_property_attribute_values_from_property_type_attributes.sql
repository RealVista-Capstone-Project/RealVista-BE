-- V117__Backfill_property_attribute_values_from_property_type_attributes.sql
-- Ensures every non-deleted property has a property_attribute_values row for each
-- attribute linked to its type in property_type_attributes (V15 metadata).
-- Only inserts when (property_id, property_attribute_id) is missing; existing values are untouched.

INSERT INTO property_attribute_values (property_id, property_attribute_id, value_number, value_text, value_boolean)
SELECT
    p.property_id,
    pa.property_attribute_id,
    CASE pa.data_type
        WHEN 'NUMBER' THEN
            CASE pa.code
                WHEN 'BEDROOMS' THEN (1 + RANDOM() * 5)::NUMERIC(12, 2)
                WHEN 'BATHROOMS' THEN (1 + RANDOM() * 4)::NUMERIC(12, 2)
                WHEN 'FLOOR' THEN (1 + RANDOM() * 39)::NUMERIC(12, 2)
                WHEN 'FLOORS' THEN (1 + RANDOM() * 19)::NUMERIC(12, 2)
                WHEN 'TOTAL_FLOORS' THEN (15 + RANDOM() * 25)::NUMERIC(12, 2)
                WHEN 'GARAGE_AREA' THEN (10 + RANDOM() * 50)::NUMERIC(12, 2)
                WHEN 'PARKING' THEN (2 + RANDOM() * 30)::NUMERIC(12, 2)
                WHEN 'OFFICE_ROOMS' THEN (1 + RANDOM() * 20)::NUMERIC(12, 2)
                WHEN 'MEETING_ROOMS' THEN (1 + RANDOM() * 8)::NUMERIC(12, 2)
                WHEN 'RESTROOMS' THEN (1 + RANDOM() * 8)::NUMERIC(12, 2)
                WHEN 'WIDTH' THEN (4 + RANDOM() * 25)::NUMERIC(12, 2)
                WHEN 'DEPTH' THEN (8 + RANDOM() * 40)::NUMERIC(12, 2)
                WHEN 'UPPER_BEDROOMS' THEN (RANDOM() * 4)::NUMERIC(12, 2)
                WHEN 'UPPER_BATHROOMS' THEN (RANDOM() * 3)::NUMERIC(12, 2)
                WHEN 'SHOPS' THEN (30 + RANDOM() * 220)::NUMERIC(12, 2)
                WHEN 'TABLES' THEN (8 + RANDOM() * 52)::NUMERIC(12, 2)
                WHEN 'ROOMS_HOTEL' THEN (20 + RANDOM() * 180)::NUMERIC(12, 2)
                WHEN 'STARS' THEN (3 + (RANDOM() * 2.99)::INTEGER)::NUMERIC(12, 2)
                WHEN 'HEIGHT' THEN (4 + RANDOM() * 12)::NUMERIC(12, 2)
                WHEN 'TRUCK_PARKING' THEN (2 + RANDOM() * 18)::NUMERIC(12, 2)
                WHEN 'GATES' THEN (1 + RANDOM() * 5)::NUMERIC(12, 2)
                WHEN 'ENTRANCES' THEN (1 + RANDOM() * 6)::NUMERIC(12, 2)
                WHEN 'LOADING_DOCKS' THEN (1 + RANDOM() * 10)::NUMERIC(12, 2)
                WHEN 'FRONTAGE' THEN (5 + RANDOM() * 35)::NUMERIC(12, 2)
                WHEN 'LAND_DEPTH' THEN (12 + RANDOM() * 80)::NUMERIC(12, 2)
                ELSE (10 + RANDOM() * 90)::NUMERIC(12, 2)
            END
        ELSE NULL
    END AS value_number,
    CASE pa.data_type
        WHEN 'TEXT' THEN
            CASE pa.code
                WHEN 'DIRECTION' THEN
                    CASE (RANDOM() * 8)::INTEGER
                        WHEN 0 THEN 'Đông'
                        WHEN 1 THEN 'Tây'
                        WHEN 2 THEN 'Nam'
                        WHEN 3 THEN 'Bắc'
                        WHEN 4 THEN 'Đông Nam'
                        WHEN 5 THEN 'Đông Bắc'
                        WHEN 6 THEN 'Tây Nam'
                        ELSE 'Tây Bắc'
                    END
                WHEN 'BALCONY_TYPE' THEN
                    CASE (RANDOM() * 4)::INTEGER
                        WHEN 0 THEN 'Thoáng'
                        WHEN 1 THEN 'Kín'
                        WHEN 2 THEN 'Kính'
                        ELSE 'Lưới an toàn'
                    END
                WHEN 'VIEW' THEN
                    CASE (RANDOM() * 5)::INTEGER
                        WHEN 0 THEN 'Biển'
                        WHEN 1 THEN 'Thành phố'
                        WHEN 2 THEN 'Công viên'
                        WHEN 3 THEN 'Sông'
                        ELSE 'Nội khu'
                    END
                WHEN 'ROOMS' THEN
                    CASE (RANDOM() * 4)::INTEGER
                        WHEN 0 THEN 'Studio tiêu chuẩn'
                        WHEN 1 THEN 'Căn góc'
                        WHEN 2 THEN 'Duplex mini'
                        ELSE 'Không gian mở'
                    END
                WHEN 'POWER' THEN
                    CASE (RANDOM() * 4)::INTEGER
                        WHEN 0 THEN '1 pha'
                        WHEN 1 THEN '3 pha'
                        WHEN 2 THEN '380V'
                        ELSE '22kV'
                    END
                WHEN 'PLANNING' THEN
                    CASE (RANDOM() * 3)::INTEGER
                        WHEN 0 THEN 'Đất ở'
                        WHEN 1 THEN 'Dự án'
                        ELSE 'Thương mại dịch vụ'
                    END
                WHEN 'PURPOSE' THEN
                    CASE (RANDOM() * 4)::INTEGER
                        WHEN 0 THEN 'Văn phòng'
                        WHEN 1 THEN 'Kinh doanh'
                        WHEN 2 THEN 'Dịch vụ'
                        ELSE 'Tổng hợp'
                    END
                WHEN 'HEIGHT_PLANNING' THEN
                    CASE (RANDOM() * 3)::INTEGER
                        WHEN 0 THEN '≤5 tầng'
                        WHEN 1 THEN '6–10 tầng'
                        ELSE 'Theo quy hoạch phân khu'
                    END
                WHEN 'ZONE' THEN
                    CASE (RANDOM() * 3)::INTEGER
                        WHEN 0 THEN 'KCN tập trung'
                        WHEN 1 THEN 'Cụm công nghiệp'
                        ELSE 'Khu kinh tế'
                    END
                WHEN 'CROP_TYPE' THEN
                    CASE (RANDOM() * 4)::INTEGER
                        WHEN 0 THEN 'Lúa'
                        WHEN 1 THEN 'Cây ăn trái'
                        WHEN 2 THEN 'Rau màu'
                        ELSE 'Công nghiệp'
                    END
                WHEN 'WATER_SOURCE' THEN
                    CASE (RANDOM() * 3)::INTEGER
                        WHEN 0 THEN 'Giếng khoan'
                        WHEN 1 THEN 'Kênh mương'
                        ELSE 'Trữ mưa'
                    END
                WHEN 'ACCESS_ROAD' THEN
                    CASE (RANDOM() * 3)::INTEGER
                        WHEN 0 THEN 'Nhựa'
                        WHEN 1 THEN 'Bê tông'
                        ELSE 'Đất'
                    END
                ELSE 'Không xác định'
            END
        ELSE NULL
    END AS value_text,
    CASE pa.data_type
        WHEN 'BOOLEAN' THEN RANDOM() > 0.35
        ELSE NULL
    END AS value_boolean
FROM properties p
JOIN property_types pt ON pt.property_type_id = p.property_type_id AND pt.deleted = FALSE
JOIN property_type_attributes pta
    ON pta.property_type_id = pt.property_type_id
    AND pta.deleted = FALSE
JOIN property_attributes pa
    ON pa.property_attribute_id = pta.property_attribute_id
    AND pa.deleted = FALSE
WHERE p.deleted = FALSE
  AND NOT EXISTS (
    SELECT 1
    FROM property_attribute_values e
    WHERE e.property_id = p.property_id
      AND e.property_attribute_id = pa.property_attribute_id
);
