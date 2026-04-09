-- V63__Update_boost_packages.sql
-- Update boost packages with corrected quotas (basic 2-2 pattern) and market-competitive pricing
-- Pricing based on Vietnam real estate platforms: batdongsan.com.vn, chotot.com

-- ============================================================================
-- UPDATE BOOST PACKAGES WITH CORRECT QUOTAS AND PRICING
-- ============================================================================

-- BASIC Package: 2 Featured + 2 Hot Badge
-- Entry-level boost for individual users
UPDATE boost_packages
SET featured_quota = 2,
    hot_badge_quota = 2,
    price = 99000.00,
    description = 'Gói boost cơ bản với 2 lượt Featured và 2 lượt Hot Badge trong 30 ngày. Phù hợp cho người dùng cá nhân muốn tăng hiển thị tin đăng.',
    updated_at = NOW()
WHERE code = 'BASIC';

-- PREMIUM Package: 5 Featured + 5 Hot Badge
-- For active agents and property owners
UPDATE boost_packages
SET featured_quota = 5,
    hot_badge_quota = 5,
    price = 199000.00,
    description = 'Gói boost nâng cao với 5 lượt Featured và 5 lượt Hot Badge trong 30 ngày. Lý tưởng cho môi giới bất động sản và chủ nhà có nhiều tin đăng.',
    updated_at = NOW()
WHERE code = 'PREMIUM';

-- PRO Package: 10 Featured + 10 Hot Badge
-- For businesses and agencies
UPDATE boost_packages
SET featured_quota = 10,
    hot_badge_quota = 10,
    price = 349000.00,
    description = 'Gói boost chuyên nghiệp với 10 lượt Featured và 10 lượt Hot Badge trong 30 ngày. Dành cho doanh nghiệp và đại lý có quy mô lớn.',
    updated_at = NOW()
WHERE code = 'PRO';
