-- V86__Seed_price_range_for_properties.sql
-- Migration V86: Synchronize property price ranges with realistic HCMC 2024-2026 data
-- Seed price_range JSONB data for all existing properties that have no price yet.
-- Prices are in full VND (e.g. 10000000 = 10 triệu VND).
-- Format: {"rent": {"min": <number>, "max": <number>}, "buy": {"min": <number>, "max": <number>}}
--
-- IMPORTANT: Only rows with price_range IS NULL are updated. Do NOT add UUID pattern
-- predicates (e.g. a7%/c%): demo properties use a7100000-* IDs; re-running or fresh
-- installs would overwrite user-edited / API-set ranges for those properties.

UPDATE properties
SET price_range = CASE property_type_id

    -- =========================================================================
    -- RESIDENTIAL
    -- =========================================================================

    -- APARTMENT (Căn Hộ / Chung Cư)
    WHEN '320e8400-e29b-41d4-a716-446655440001'
        THEN '{"rent": {"min": 25000000, "max": 75000000}, "buy": {"min": 3500000000, "max": 18000000000}}'::jsonb

    -- HOUSE (Nhà Riêng)
    WHEN '320e8400-e29b-41d4-a716-446655440002'
        THEN '{"rent": {"min": 40000000, "max": 130000000}, "buy": {"min": 12000000000, "max": 55000000000}}'::jsonb

    -- VILLA (Biệt Thự)
    WHEN '320e8400-e29b-41d4-a716-446655440003'
        THEN '{"rent": {"min": 120000000, "max": 550000000}, "buy": {"min": 55000000000, "max": 350000000000}}'::jsonb

    -- TOWNHOUSE (Nhà Phố)
    WHEN '320e8400-e29b-41d4-a716-446655440004'
        THEN '{"rent": {"min": 35000000, "max": 120000000}, "buy": {"min": 9500000000, "max": 38000000000}}'::jsonb

    -- PENTHOUSE
    WHEN '320e8400-e29b-41d4-a716-446655440005'
        THEN '{"rent": {"min": 80000000, "max": 300000000}, "buy": {"min": 15000000000, "max": 85000000000}}'::jsonb

    -- STUDIO (Studio / Căn Hộ Dịch Vụ)
    WHEN '320e8400-e29b-41d4-a716-446655440006'
        THEN '{"rent": {"min": 12000000, "max": 35000000}, "buy": {"min": 2500000000, "max": 6500000000}}'::jsonb

    -- =========================================================================
    -- COMMERCIAL
    -- =========================================================================

    -- OFFICE (Văn Phòng)
    WHEN '320e8400-e29b-41d4-a716-446655440007'
        THEN '{"rent": {"min": 50000000, "max": 1500000000}, "buy": {"min": 45000000000, "max": 450000000000}}'::jsonb

    -- SHOPHOUSE (Nhà Cửa Hàng)
    WHEN '320e8400-e29b-41d4-a716-446655440008'
        THEN '{"rent": {"min": 80000000, "max": 350000000}, "buy": {"min": 35000000000, "max": 150000000000}}'::jsonb

    -- RETAIL (Không Gian Bán Lẻ)
    WHEN '320e8400-e29b-41d4-a716-446655440009'
        THEN '{"rent": {"min": 40000000, "max": 250000000}, "buy": {"min": 25000000000, "max": 125000000000}}'::jsonb

    -- MALL (Trung Tâm Thương Mại)
    WHEN '320e8400-e29b-41d4-a716-446655440010'
        THEN '{"rent": {"min": 1500000000, "max": 8500000000}, "buy": {"min": 350000000000, "max": 1200000000000}}'::jsonb

    -- RESTAURANT (Nhà Hàng / Quán Cà Phê)
    WHEN '320e8400-e29b-41d4-a716-446655440011'
        THEN '{"rent": {"min": 50000000, "max": 250000000}, "buy": {"min": 18000000000, "max": 65000000000}}'::jsonb

    -- HOTEL (Khách Sạn / Nhà Khách)
    WHEN '320e8400-e29b-41d4-a716-446655440012'
        THEN '{"rent": {"min": 500000000, "max": 2500000000}, "buy": {"min": 150000000000, "max": 2500000000000}}'::jsonb

    -- =========================================================================
    -- INDUSTRIAL
    -- =========================================================================

    -- WAREHOUSE (Kho Bãi)
    WHEN '320e8400-e29b-41d4-a716-446655440013'
        THEN '{"rent": {"min": 100000000, "max": 650000000}, "buy": {"min": 45000000000, "max": 250000000000}}'::jsonb

    -- FACTORY (Nhà Máy)
    WHEN '320e8400-e29b-41d4-a716-446655440014'
        THEN '{"rent": {"min": 250000000, "max": 1200000000}, "buy": {"min": 85000000000, "max": 450000000000}}'::jsonb

    -- WORKSHOP (Xưởng)
    WHEN '320e8400-e29b-41d4-a716-446655440015'
        THEN '{"rent": {"min": 40000000, "max": 150000000}, "buy": {"min": 15000000000, "max": 65000000000}}'::jsonb

    -- LOGISTICS (Trung Tâm Logistics)
    WHEN '320e8400-e29b-41d4-a716-446655440016'
        THEN '{"rent": {"min": 500000000, "max": 2500000000}, "buy": {"min": 150000000000, "max": 850000000000}}'::jsonb

    -- =========================================================================
    -- LAND
    -- =========================================================================

    -- LAND_RESIDENTIAL (Đất Nhà Ở)
    WHEN '320e8400-e29b-41d4-a716-446655440017'
        THEN '{"rent": {"min": 15000000, "max": 50000000}, "buy": {"min": 8000000000, "max": 65000000000}}'::jsonb

    -- LAND_COMMERCIAL (Đất Thương Mại)
    WHEN '320e8400-e29b-41d4-a716-446655440018'
        THEN '{"rent": {"min": 40000000, "max": 150000000}, "buy": {"min": 25000000000, "max": 150000000000}}'::jsonb

    -- LAND_INDUSTRIAL (Đất Công Nghiệp)
    WHEN '320e8400-e29b-41d4-a716-446655440019'
        THEN '{"rent": {"min": 50000000, "max": 250000000}, "buy": {"min": 20000000000, "max": 150000000000}}'::jsonb

    -- LAND_AGRICULTURAL (Đất Nông Nghiệp)
    WHEN '320e8400-e29b-41d4-a716-446655440020'
        THEN '{"rent": {"min": 5000000, "max": 25000000}, "buy": {"min": 5000000000, "max": 45000000000}}'::jsonb

    ELSE price_range

END
WHERE price_range IS NULL
  AND deleted = FALSE;
