-- V35__Insert_boost_packages.sql
-- Migration V35: Insert boost package data
-- Creates Basic, Premium, and Pro boost packages with different quotas and pricing

-- ============================================================================
-- Insert Boost Packages
-- ============================================================================
INSERT INTO boost_packages (
    code,
    name,
    description,
    featured_quota,
    hot_badge_quota,
    duration_days,
    price,
    is_active
) VALUES 
-- Basic Package
(
    'BASIC',
    'Gói Basic',
    'Gói boost cơ bản với 2 lượt Featured và 3 lượt Hot Badge trong 30 ngày. Phù hợp cho người dùng cá nhân muốn tăng hiển thị tin đăng.',
    2,  -- 2 Featured quota
    3,  -- 3 Hot Badge quota  
    30, -- 30 days duration
    149000.00, -- 149,000 VND
    TRUE
),

-- Premium Package  
(
    'PREMIUM',
    'Gói Premium', 
    'Gói boost nâng cao với 5 lượt Featured và 7 lượt Hot Badge trong 30 ngày. Lý tưởng cho môi giới bất động sản và chủ nhà có nhiều tin đăng.',
    5,  -- 5 Featured quota
    7,  -- 7 Hot Badge quota
    30, -- 30 days duration  
    299000.00, -- 299,000 VND
    TRUE
),

-- Pro Package
(
    'PRO',
    'Gói Pro',
    'Gói boost chuyên nghiệp với 10 lượt Featured và 15 lượt Hot Badge trong 30 ngày. Dành cho doanh nghiệp và đại lý có quy mô lớn.',
    10, -- 10 Featured quota
    15, -- 15 Hot Badge quota  
    30, -- 30 days duration
    499000.00, -- 499,000 VND
    TRUE
);