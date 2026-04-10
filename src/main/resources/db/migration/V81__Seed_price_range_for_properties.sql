-- V81__Seed_price_range_for_properties.sql
-- Seed price_range JSONB data for all existing properties that have no price yet.
-- Prices are in full VND (e.g. 10000000 = 10 triệu VND), realistic for Ho Chi Minh City market.
-- Format: {"rent": {"min": <number>, "max": <number>}, "buy": {"min": <number>, "max": <number>}}
-- Safe: only updates rows where price_range IS NULL and deleted = FALSE.

UPDATE properties
SET price_range = CASE property_type_id

    -- =========================================================================
    -- RESIDENTIAL
    -- =========================================================================

    -- APARTMENT (Căn Hộ / Chung Cư)
    WHEN '320e8400-e29b-41d4-a716-446655440001'
        THEN '{"rent": {"min": 10000000, "max": 30000000}, "buy": {"min": 1000000000, "max": 5000000000}}'::jsonb

    -- HOUSE (Nhà Riêng)
    WHEN '320e8400-e29b-41d4-a716-446655440002'
        THEN '{"rent": {"min": 15000000, "max": 50000000}, "buy": {"min": 2000000000, "max": 10000000000}}'::jsonb

    -- VILLA (Biệt Thự)
    WHEN '320e8400-e29b-41d4-a716-446655440003'
        THEN '{"rent": {"min": 50000000, "max": 200000000}, "buy": {"min": 10000000000, "max": 50000000000}}'::jsonb

    -- TOWNHOUSE (Nhà Phố)
    WHEN '320e8400-e29b-41d4-a716-446655440004'
        THEN '{"rent": {"min": 20000000, "max": 80000000}, "buy": {"min": 3000000000, "max": 15000000000}}'::jsonb

    -- PENTHOUSE
    WHEN '320e8400-e29b-41d4-a716-446655440005'
        THEN '{"rent": {"min": 50000000, "max": 150000000}, "buy": {"min": 5000000000, "max": 30000000000}}'::jsonb

    -- STUDIO (Studio / Căn Hộ Dịch Vụ)
    WHEN '320e8400-e29b-41d4-a716-446655440006'
        THEN '{"rent": {"min": 5000000, "max": 15000000}, "buy": {"min": 500000000, "max": 2000000000}}'::jsonb

    -- =========================================================================
    -- COMMERCIAL
    -- =========================================================================

    -- OFFICE (Văn Phòng)
    WHEN '320e8400-e29b-41d4-a716-446655440007'
        THEN '{"rent": {"min": 30000000, "max": 100000000}, "buy": {"min": 5000000000, "max": 20000000000}}'::jsonb

    -- SHOPHOUSE (Nhà Cửa Hàng)
    WHEN '320e8400-e29b-41d4-a716-446655440008'
        THEN '{"rent": {"min": 30000000, "max": 120000000}, "buy": {"min": 5000000000, "max": 25000000000}}'::jsonb

    -- RETAIL (Không Gian Bán Lẻ)
    WHEN '320e8400-e29b-41d4-a716-446655440009'
        THEN '{"rent": {"min": 20000000, "max": 80000000}, "buy": {"min": 3000000000, "max": 15000000000}}'::jsonb

    -- MALL (Trung Tâm Thương Mại)
    WHEN '320e8400-e29b-41d4-a716-446655440010'
        THEN '{"rent": {"min": 200000000, "max": 1000000000}, "buy": {"min": 50000000000, "max": 200000000000}}'::jsonb

    -- RESTAURANT (Nhà Hàng / Quán Cà Phê)
    WHEN '320e8400-e29b-41d4-a716-446655440011'
        THEN '{"rent": {"min": 20000000, "max": 80000000}, "buy": {"min": 3000000000, "max": 15000000000}}'::jsonb

    -- HOTEL (Khách Sạn / Nhà Khách)
    WHEN '320e8400-e29b-41d4-a716-446655440012'
        THEN '{"rent": {"min": 100000000, "max": 500000000}, "buy": {"min": 20000000000, "max": 100000000000}}'::jsonb

    -- =========================================================================
    -- INDUSTRIAL
    -- =========================================================================

    -- WAREHOUSE (Kho Bãi)
    WHEN '320e8400-e29b-41d4-a716-446655440013'
        THEN '{"rent": {"min": 50000000, "max": 200000000}, "buy": {"min": 5000000000, "max": 30000000000}}'::jsonb

    -- FACTORY (Nhà Máy)
    WHEN '320e8400-e29b-41d4-a716-446655440014'
        THEN '{"rent": {"min": 100000000, "max": 500000000}, "buy": {"min": 10000000000, "max": 50000000000}}'::jsonb

    -- WORKSHOP (Xưởng)
    WHEN '320e8400-e29b-41d4-a716-446655440015'
        THEN '{"rent": {"min": 20000000, "max": 80000000}, "buy": {"min": 2000000000, "max": 10000000000}}'::jsonb

    -- LOGISTICS (Trung Tâm Logistics)
    WHEN '320e8400-e29b-41d4-a716-446655440016'
        THEN '{"rent": {"min": 200000000, "max": 800000000}, "buy": {"min": 20000000000, "max": 80000000000}}'::jsonb

    -- =========================================================================
    -- LAND
    -- =========================================================================

    -- LAND_RESIDENTIAL (Đất Nhà Ở)
    WHEN '320e8400-e29b-41d4-a716-446655440017'
        THEN '{"rent": {"min": 5000000, "max": 20000000}, "buy": {"min": 500000000, "max": 5000000000}}'::jsonb

    -- LAND_COMMERCIAL (Đất Thương Mại)
    WHEN '320e8400-e29b-41d4-a716-446655440018'
        THEN '{"rent": {"min": 20000000, "max": 100000000}, "buy": {"min": 2000000000, "max": 20000000000}}'::jsonb

    -- LAND_INDUSTRIAL (Đất Công Nghiệp)
    WHEN '320e8400-e29b-41d4-a716-446655440019'
        THEN '{"rent": {"min": 30000000, "max": 150000000}, "buy": {"min": 1000000000, "max": 10000000000}}'::jsonb

    -- LAND_AGRICULTURAL (Đất Nông Nghiệp)
    WHEN '320e8400-e29b-41d4-a716-446655440020'
        THEN '{"rent": {"min": 1000000, "max": 5000000}, "buy": {"min": 100000000, "max": 1000000000}}'::jsonb

    ELSE price_range

END
WHERE price_range IS NULL
  AND deleted = FALSE;
