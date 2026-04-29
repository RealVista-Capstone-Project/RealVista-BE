-- V118__Reseed_price_range_for_new_properties.sql
-- Properties inserted after V86 (notably by V107__Expand_Properties_And_Align_Media)
-- have price_range = NULL. This migration seeds price_range per property_type for
-- every non-deleted property whose price_range is still NULL.
--
-- Ranges follow the listing pricing convention in V23__Insert_property_listings.sql:
--   * RENT values stay in the millions (triệu) VND scale (max < 1 tỷ).
--   * BUY  values stay in the billions (tỷ) VND scale.
--   * min < max is guaranteed by using hard-coded JSONB literals per type.
--
-- Safety: only rows with price_range IS NULL are updated; existing values
-- (user-edited or API-set) are never overwritten. Re-runnable in theory, but
-- Flyway will only execute this version once.

UPDATE properties
SET price_range = CASE property_type_id

    -- =========================================================================
    -- RESIDENTIAL
    -- =========================================================================

    -- APARTMENT (Căn Hộ / Chung Cư)
    WHEN '320e8400-e29b-41d4-a716-446655440001'
        THEN '{"rent": {"min": 15000000, "max": 50000000}, "buy": {"min": 3000000000, "max": 12000000000}}'::jsonb

    -- HOUSE (Nhà Riêng)
    WHEN '320e8400-e29b-41d4-a716-446655440002'
        THEN '{"rent": {"min": 25000000, "max": 80000000}, "buy": {"min": 8000000000, "max": 30000000000}}'::jsonb

    -- VILLA (Biệt Thự)
    WHEN '320e8400-e29b-41d4-a716-446655440003'
        THEN '{"rent": {"min": 80000000, "max": 300000000}, "buy": {"min": 40000000000, "max": 150000000000}}'::jsonb

    -- TOWNHOUSE (Nhà Phố)
    WHEN '320e8400-e29b-41d4-a716-446655440004'
        THEN '{"rent": {"min": 30000000, "max": 100000000}, "buy": {"min": 10000000000, "max": 40000000000}}'::jsonb

    -- PENTHOUSE
    WHEN '320e8400-e29b-41d4-a716-446655440005'
        THEN '{"rent": {"min": 60000000, "max": 200000000}, "buy": {"min": 20000000000, "max": 70000000000}}'::jsonb

    -- STUDIO (Studio / Căn Hộ Dịch Vụ)
    WHEN '320e8400-e29b-41d4-a716-446655440006'
        THEN '{"rent": {"min": 8000000, "max": 25000000}, "buy": {"min": 2000000000, "max": 5000000000}}'::jsonb

    -- =========================================================================
    -- COMMERCIAL
    -- =========================================================================

    -- OFFICE (Văn Phòng)
    WHEN '320e8400-e29b-41d4-a716-446655440007'
        THEN '{"rent": {"min": 40000000, "max": 250000000}, "buy": {"min": 20000000000, "max": 100000000000}}'::jsonb

    -- SHOPHOUSE (Nhà Cửa Hàng)
    WHEN '320e8400-e29b-41d4-a716-446655440008'
        THEN '{"rent": {"min": 50000000, "max": 180000000}, "buy": {"min": 20000000000, "max": 80000000000}}'::jsonb

    -- RETAIL (Không Gian Bán Lẻ)
    WHEN '320e8400-e29b-41d4-a716-446655440009'
        THEN '{"rent": {"min": 30000000, "max": 150000000}, "buy": {"min": 15000000000, "max": 60000000000}}'::jsonb

    -- MALL (Trung Tâm Thương Mại)
    WHEN '320e8400-e29b-41d4-a716-446655440010'
        THEN '{"rent": {"min": 300000000, "max": 900000000}, "buy": {"min": 200000000000, "max": 800000000000}}'::jsonb

    -- RESTAURANT (Nhà Hàng / Quán Cà Phê)
    WHEN '320e8400-e29b-41d4-a716-446655440011'
        THEN '{"rent": {"min": 40000000, "max": 180000000}, "buy": {"min": 12000000000, "max": 50000000000}}'::jsonb

    -- HOTEL (Khách Sạn / Nhà Khách)
    WHEN '320e8400-e29b-41d4-a716-446655440012'
        THEN '{"rent": {"min": 200000000, "max": 700000000}, "buy": {"min": 100000000000, "max": 400000000000}}'::jsonb

    -- =========================================================================
    -- INDUSTRIAL
    -- =========================================================================

    -- WAREHOUSE (Kho Bãi)
    WHEN '320e8400-e29b-41d4-a716-446655440013'
        THEN '{"rent": {"min": 60000000, "max": 300000000}, "buy": {"min": 25000000000, "max": 100000000000}}'::jsonb

    -- FACTORY (Nhà Máy)
    WHEN '320e8400-e29b-41d4-a716-446655440014'
        THEN '{"rent": {"min": 120000000, "max": 500000000}, "buy": {"min": 60000000000, "max": 200000000000}}'::jsonb

    -- WORKSHOP (Xưởng)
    WHEN '320e8400-e29b-41d4-a716-446655440015'
        THEN '{"rent": {"min": 25000000, "max": 90000000}, "buy": {"min": 10000000000, "max": 35000000000}}'::jsonb

    -- LOGISTICS (Trung Tâm Logistics)
    WHEN '320e8400-e29b-41d4-a716-446655440016'
        THEN '{"rent": {"min": 250000000, "max": 800000000}, "buy": {"min": 100000000000, "max": 400000000000}}'::jsonb

    -- =========================================================================
    -- LAND
    -- =========================================================================

    -- LAND_RESIDENTIAL (Đất Nhà Ở)
    WHEN '320e8400-e29b-41d4-a716-446655440017'
        THEN '{"rent": {"min": 10000000, "max": 40000000}, "buy": {"min": 5000000000, "max": 30000000000}}'::jsonb

    -- LAND_COMMERCIAL (Đất Thương Mại)
    WHEN '320e8400-e29b-41d4-a716-446655440018'
        THEN '{"rent": {"min": 25000000, "max": 100000000}, "buy": {"min": 15000000000, "max": 80000000000}}'::jsonb

    -- LAND_INDUSTRIAL (Đất Công Nghiệp)
    WHEN '320e8400-e29b-41d4-a716-446655440019'
        THEN '{"rent": {"min": 30000000, "max": 120000000}, "buy": {"min": 10000000000, "max": 50000000000}}'::jsonb

    -- LAND_AGRICULTURAL (Đất Nông Nghiệp)
    WHEN '320e8400-e29b-41d4-a716-446655440020'
        THEN '{"rent": {"min": 3000000, "max": 15000000}, "buy": {"min": 2000000000, "max": 15000000000}}'::jsonb

    ELSE price_range

END
WHERE price_range IS NULL
  AND deleted = FALSE;
