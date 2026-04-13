-- V87__Add_sample_boosts.sql
-- Seed sample boost data for testing the priority-based search and UI badges
-- Featured > Hot > Agent > Date

DO $$
DECLARE
    v_premium_pkg_id UUID;
    v_basic_pkg_id UUID;
    v_listing_id UUID;
    v_user_id UUID;
    v_count INTEGER := 0;
BEGIN
    -- Clear old sample boosts to ensure clean test state
    DELETE FROM listing_boosts WHERE boost_type IN ('FEATURED', 'HOT_BADGE');

    -- Get package IDs
    SELECT boost_package_id INTO v_premium_pkg_id FROM boost_packages WHERE code = 'PREMIUM' LIMIT 1;
    SELECT boost_package_id INTO v_basic_pkg_id FROM boost_packages WHERE code = 'BASIC' LIMIT 1;

    -- 1. Add BOTH FEATURED and HOT_BADGE to 2 listings
    FOR v_listing_id, v_user_id IN 
        SELECT listing_id, user_id FROM listings WHERE status = 'PUBLISHED' LIMIT 2
    LOOP
        -- Add Featured
        INSERT INTO listing_boosts (boost_package_id, listing_id, user_id, boost_type, start_date, end_date, status)
        VALUES (v_premium_pkg_id, v_listing_id, v_user_id, 'FEATURED', CURRENT_DATE - 1, CURRENT_DATE + 29, 'ACTIVE');
        
        -- Add Hot Badge
        INSERT INTO listing_boosts (boost_package_id, listing_id, user_id, boost_type, start_date, end_date, status)
        VALUES (v_basic_pkg_id, v_listing_id, v_user_id, 'HOT_BADGE', CURRENT_DATE - 1, CURRENT_DATE + 29, 'ACTIVE');
    END LOOP;

    -- 2. Add ONLY HOT_BADGE to 3 other listings
    FOR v_listing_id, v_user_id IN 
        SELECT listing_id, user_id FROM listings 
        WHERE status = 'PUBLISHED' 
        AND listing_id NOT IN (SELECT listing_id FROM listing_boosts)
        LIMIT 3
    LOOP
        INSERT INTO listing_boosts (boost_package_id, listing_id, user_id, boost_type, start_date, end_date, status)
        VALUES (v_basic_pkg_id, v_listing_id, v_user_id, 'HOT_BADGE', CURRENT_DATE - 1, CURRENT_DATE + 29, 'ACTIVE');
    END LOOP;

    RAISE NOTICE 'Sample multi-boost data seeded successfully.';
END $$;
