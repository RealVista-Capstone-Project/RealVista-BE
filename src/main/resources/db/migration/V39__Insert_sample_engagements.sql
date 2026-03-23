-- V39__Insert_sample_engagements.sql
-- Insert sample engagements (TENANT_APPLICATION type) for Tenant 1
-- initiator_id: 550e8400-e29b-41d4-a716-446655440301 (Buyer/Tenant User One)
-- receiver_id:  owner users from V11 (550e8400-e29b-41d4-a716-446655440401..410)

-- ============================================================================
-- Use a CTE to pick up to 10 RENT listings with their property_ids
-- ============================================================================
WITH rent_listings AS (
    SELECT listing_id,
           property_id,
           name,
           ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM listings
    WHERE listing_type = 'RENT'
      AND status = 'PUBLISHED'
      AND deleted = FALSE
    LIMIT 10
)

INSERT INTO engagements (
    engagement_id,
    initiator_id,
    receiver_id,
    engagement_type,
    content,
    listing_id,
    property_id,
    status,
    created_at,
    updated_at,
    deleted
)
SELECT
    gen_random_uuid(),
    '550e8400-e29b-41d4-a716-446655440301',
    CASE rl.rn
        WHEN 1  THEN '550e8400-e29b-41d4-a716-446655440401'::UUID
        WHEN 2  THEN '550e8400-e29b-41d4-a716-446655440402'::UUID
        WHEN 3  THEN '550e8400-e29b-41d4-a716-446655440403'::UUID
        WHEN 4  THEN '550e8400-e29b-41d4-a716-446655440404'::UUID
        WHEN 5  THEN '550e8400-e29b-41d4-a716-446655440405'::UUID
        WHEN 6  THEN '550e8400-e29b-41d4-a716-446655440406'::UUID
        WHEN 7  THEN '550e8400-e29b-41d4-a716-446655440407'::UUID
        WHEN 8  THEN '550e8400-e29b-41d4-a716-446655440408'::UUID
        WHEN 9  THEN '550e8400-e29b-41d4-a716-446655440409'::UUID
        ELSE         '550e8400-e29b-41d4-a716-446655440410'::UUID
    END,
    'TENANT_APPLICATION',
    json_build_object(
        'title',            'Đơn thuê: ' || rl.name,
        'monthlyIncome',    CASE rl.rn % 3
                                WHEN 0 THEN 25000000
                                WHEN 1 THEN 18000000
                                ELSE        12000000
                            END,
        'moveInDate',       TO_CHAR(CURRENT_DATE + (rl.rn * 15 || ' days')::INTERVAL, 'YYYY-MM-DD'),
        'leaseTermMonths',  CASE rl.rn % 2 WHEN 0 THEN 12 ELSE 6 END,
        'note',             CASE rl.rn % 4
                                WHEN 0 THEN 'Tôi rất quan tâm đến căn hộ này. Công việc ổn định, có thể chuyển vào bất cứ lúc nào.'
                                WHEN 1 THEN 'Gia đình 3 người, cần chỗ ở gần trường học và siêu thị.'
                                WHEN 2 THEN 'Đang làm việc tại quận 1, cần thuê gần nơi làm việc.'
                                ELSE        'Thu nhập ổn định, sẵn sàng ký hợp đồng dài hạn.'
                            END
    ),
    rl.listing_id,
    rl.property_id,
    CASE rl.rn
        WHEN 1 THEN 'SUBMITTED'
        WHEN 2 THEN 'SUBMITTED'
        WHEN 3 THEN 'SUBMITTED'
        WHEN 4 THEN 'ACCEPTED'
        WHEN 5 THEN 'ACCEPTED'
        WHEN 6 THEN 'REJECTED'
        WHEN 7 THEN 'REJECTED'
        WHEN 8 THEN 'CANCELLED'
        WHEN 9 THEN 'CANCELLED'
        ELSE        'CANCELLED'
    END,
    NOW() - (rl.rn * 3 || ' days')::INTERVAL,
    NOW() - (rl.rn || ' days')::INTERVAL,
    FALSE
FROM rent_listings rl;
