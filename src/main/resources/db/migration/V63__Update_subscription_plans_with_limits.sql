-- V62__Update_subscription_plans_with_limits.sql
-- Insert all subscription plans with feature limits
-- Pricing based on Vietnam real estate market research
-- Note: ai_features column not used - only daily_ai_requests limits AI assistant usage
-- All other AI features (recommend, compare, price insight) are FREE for all users

-- ============================================================================
-- INSERT ALL SUBSCRIPTION PLANS
-- ============================================================================

INSERT INTO subscription_plans (
    code,
    name,
    description,
    roles,
    duration_days,
    price,
    verify_enabled,
    verify_cycle_days,
    max_active_listings,
    max_3d_tours,
    daily_ai_requests,
    is_active
) VALUES
-- FREE Plan (default for all users)
(
    'FREE',
    'Gói Miễn phí',
    'Gói cơ bản miễn phí dành cho người dùng cá nhân. Giới hạn 5 tin đăng, 1 tour 3D và 20 lượt AI assistant mỗi ngày.',
    '["ROLE_USER"]',
    -1,      -- No expiration for free plan
    0.00,
    FALSE,
    0,
    5,       -- Max 5 active listings
    1,       -- 1 3D tour
    20,      -- 20 AI requests/day
    TRUE
),
-- BASIC Plan: Entry-level paid tier
(
    'BASIC',
    'Gói Basic',
    'Gói dành cho cá nhân và môi giới nhỏ. 10 tin đăng, 3 tour 3D và 50 lượt AI assistant mỗi ngày.',
    '["ROLE_USER", "ROLE_AGENT"]',
    30,      -- 30 days
    99000.00,
    TRUE,
    30,
    10,      -- Max 10 active listings
    3,       -- 3 3D tours
    50,      -- 50 AI requests/day
    TRUE
),
-- PRO Plan: Most popular tier for professional agents
(
    'PRO',
    'Gói Pro',
    'Gói dành cho môi giới chuyên nghiệp. 25 tin đăng, 10 tour 3D và 150 lượt AI assistant mỗi ngày.',
    '["ROLE_USER", "ROLE_AGENT"]',
    30,      -- 30 days
    199000.00,
    TRUE,
    15,
    25,      -- Max 25 active listings
    10,      -- 10 3D tours
    150,     -- 150 AI requests/day
    TRUE
),
-- PREMIUM Plan: Unlimited tier for businesses
(
    'PREMIUM',
    'Gói Premium',
    'Gói cao cấp dành cho doanh nghiệp và đại lý lớn. Không giới hạn tin đăng, tour 3D và AI assistant.',
    '["ROLE_USER", "ROLE_AGENT", "ROLE_AGENCY"]',
    30,      -- 30 days
    349000.00,
    TRUE,
    7,
    -1,      -- Unlimited active listings
    -1,      -- Unlimited 3D tours
    -1,      -- Unlimited AI requests
    TRUE
),

-- ============================================================================
-- GÓI 3D TOURS LẺ (CHỈ Tour 3D - Listings giữ mặc định FREE = 5)
-- ============================================================================

-- 3D Tours Basic
(
    '3D_BASIC',
    'Gói 3D Basic',
    'Gói Tour 3D cơ bản. 5 tour 3D, 30 lượt AI assistant mỗi ngày.',
    '["ROLE_USER", "ROLE_AGENT"]',
    30,
    59000.00,
    FALSE,
    0,
    5,       -- Giữ mặc định FREE
    5,       -- 5 3D tours
    30,      -- 30 AI requests/day
    TRUE
),
(
    '3D_PRO',
    'Gói 3D Pro',
    'Gói Tour 3D chuyên nghiệp. 15 tour 3D, 80 lượt AI assistant mỗi ngày.',
    '["ROLE_USER", "ROLE_AGENT"]',
    30,
    119000.00,
    FALSE,
    0,
    5,       -- Giữ mặc định FREE
    15,      -- 15 3D tours
    80,      -- 80 AI requests/day
    TRUE
),
(
    '3D_PREMIUM',
    'Gói 3D Premium',
    'Gói Tour 3D cao cấp. Không giới hạn tour 3D, 200 lượt AI assistant mỗi ngày.',
    '["ROLE_USER", "ROLE_AGENT", "ROLE_AGENCY"]',
    30,
    199000.00,
    FALSE,
    0,
    5,       -- Giữ mặc định FREE
    -1,      -- Unlimited 3D tours
    200,     -- 200 AI requests/day
    TRUE
),

-- ============================================================================
-- GÓI ACTIVE LISTINGS LẺ (CHỈ Tin đăng - 3D Tours giữ mặc định FREE = 1)
-- ============================================================================

(
    'LISTING_BASIC',
    'Gói Tin đăng Basic',
    'Gói tin đăng cơ bản. 15 tin đăng hoạt động, 30 lượt AI assistant mỗi ngày.',
    '["ROLE_USER", "ROLE_AGENT"]',
    30,
    69000.00,
    TRUE,
    30,
    15,      -- 15 active listings
    1,       -- Giữ mặc định FREE
    30,      -- 30 AI requests/day
    TRUE
),
(
    'LISTING_PRO',
    'Gói Tin đăng Pro',
    'Gói tin đăng chuyên nghiệp. 40 tin đăng hoạt động, 80 lượt AI assistant mỗi ngày.',
    '["ROLE_USER", "ROLE_AGENT"]',
    30,
    139000.00,
    TRUE,
    15,
    40,      -- 40 active listings
    1,       -- Giữ mặc định FREE
    80,      -- 80 AI requests/day
    TRUE
),
(
    'LISTING_PREMIUM',
    'Gói Tin đăng Premium',
    'Gói tin đăng cao cấp. Không giới hạn tin đăng, 200 lượt AI assistant mỗi ngày.',
    '["ROLE_USER", "ROLE_AGENT", "ROLE_AGENCY"]',
    30,
    249000.00,
    TRUE,
    7,
    -1,      -- Unlimited active listings
    1,       -- Giữ mặc định FREE
    200,     -- 200 AI requests/day
    TRUE
)
ON CONFLICT (code) DO NOTHING;
