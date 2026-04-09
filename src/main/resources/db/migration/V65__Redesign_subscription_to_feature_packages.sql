-- V64__Redesign_subscription_to_feature_packages.sql
-- Redesign subscription system from bundle-based to à la carte (feature packages)
-- Users can now purchase individual features: Listings, 3D Tours, AI Requests

-- ============================================================================
-- DROP OLD SUBSCRIPTION TABLES
-- ============================================================================

DROP TABLE IF EXISTS user_subscriptions CASCADE;
DROP TABLE IF EXISTS subscription_plans CASCADE;

-- ============================================================================
-- CREATE NEW FEATURE PACKAGES TABLE
-- ============================================================================

CREATE TABLE feature_packages
(
    feature_package_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    code               VARCHAR(50)    NOT NULL UNIQUE,
    name               VARCHAR(100)   NOT NULL,
    description        TEXT,
    feature_type       VARCHAR(30)    NOT NULL,
    quota              INTEGER        NOT NULL,
    duration_days      INTEGER        NOT NULL,
    price              NUMERIC(12, 2) NOT NULL,
    is_active          BOOLEAN   DEFAULT TRUE,
    created_at         TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP      NOT NULL DEFAULT NOW(),
    deleted            BOOLEAN   DEFAULT FALSE,

    CONSTRAINT chk_feature_type CHECK (feature_type IN ('LISTING', '3D_TOUR', 'AI_REQUEST'))
);

CREATE INDEX idx_feature_package_code ON feature_packages (code);
CREATE INDEX idx_feature_package_type ON feature_packages (feature_type);
CREATE INDEX idx_feature_package_active ON feature_packages (is_active);

COMMENT ON TABLE feature_packages IS 'Individual feature packages that users can purchase separately';
COMMENT ON COLUMN feature_packages.feature_type IS 'Type of feature: LISTING, 3D_TOUR, AI_REQUEST';
COMMENT ON COLUMN feature_packages.quota IS 'Number of units. -1 means unlimited';
COMMENT ON COLUMN feature_packages.duration_days IS 'Validity period in days. -1 means no expiration';

-- ============================================================================
-- CREATE USER FEATURE SUBSCRIPTIONS TABLE
-- ============================================================================

CREATE TABLE user_feature_subscriptions
(
    user_feature_subscription_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id                      UUID        NOT NULL,
    feature_package_id           UUID        NOT NULL,
    start_date                   DATE        NOT NULL,
    end_date                     DATE,
    remaining_quota              INTEGER,
    status                       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at                   TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at                   TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted                      BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (feature_package_id) REFERENCES feature_packages (feature_package_id),
    CONSTRAINT chk_user_feature_subscription_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED', 'EXHAUSTED'))
);

CREATE INDEX idx_user_feature_sub_user ON user_feature_subscriptions (user_id);
CREATE INDEX idx_user_feature_sub_package ON user_feature_subscriptions (feature_package_id);
CREATE INDEX idx_user_feature_sub_status ON user_feature_subscriptions (status);
CREATE INDEX idx_user_feature_sub_user_status ON user_feature_subscriptions (user_id, status);

COMMENT ON TABLE user_feature_subscriptions IS 'User purchased feature packages';
COMMENT ON COLUMN user_feature_subscriptions.remaining_quota IS 'Remaining quota for countable features. NULL for unlimited packages';
COMMENT ON COLUMN user_feature_subscriptions.status IS 'ACTIVE, EXPIRED (time-based), CANCELLED, EXHAUSTED (quota depleted)';

-- ============================================================================
-- INSERT FEATURE PACKAGES DATA
-- ============================================================================

-- ============================================================================
-- LISTING PACKAGES (Active Listings)
-- ============================================================================

INSERT INTO feature_packages (code, name, description, feature_type, quota, duration_days, price) VALUES
-- Free tier (given to all new users)
('LISTING_FREE', 'Tin đăng Miễn phí', 'Gói miễn phí cho người dùng mới. 3 tin đăng hoạt động.', 'LISTING', 3, -1, 0.00),
-- Paid tiers
('LISTING_10', 'Gói 10 Tin đăng', 'Thêm 10 tin đăng hoạt động trong 30 ngày.', 'LISTING', 10, 30, 49000.00),
('LISTING_25', 'Gói 25 Tin đăng', 'Thêm 25 tin đăng hoạt động trong 30 ngày.', 'LISTING', 25, 30, 99000.00),
('LISTING_50', 'Gói 50 Tin đăng', 'Thêm 50 tin đăng hoạt động trong 30 ngày.', 'LISTING', 50, 30, 179000.00),
('LISTING_UNLIMITED', 'Tin đăng Không giới hạn', 'Không giới hạn số lượng tin đăng trong 30 ngày.', 'LISTING', -1, 30, 299000.00);

-- ============================================================================
-- 3D TOUR PACKAGES
-- ============================================================================

INSERT INTO feature_packages (code, name, description, feature_type, quota, duration_days, price) VALUES
-- Free tier
('3D_TOUR_FREE', 'Tour 3D Miễn phí', 'Gói miễn phí. 1 tour 3D.', '3D_TOUR', 1, -1, 0.00),
-- Paid tiers
('3D_TOUR_5', 'Gói 5 Tour 3D', 'Thêm 5 tour 3D trong 30 ngày.', '3D_TOUR', 5, 30, 59000.00),
('3D_TOUR_15', 'Gói 15 Tour 3D', 'Thêm 15 tour 3D trong 30 ngày.', '3D_TOUR', 15, 30, 129000.00),
('3D_TOUR_30', 'Gói 30 Tour 3D', 'Thêm 30 tour 3D trong 30 ngày.', '3D_TOUR', 30, 30, 229000.00),
('3D_TOUR_UNLIMITED', 'Tour 3D Không giới hạn', 'Không giới hạn số lượng tour 3D trong 30 ngày.', '3D_TOUR', -1, 30, 349000.00);

-- ============================================================================
-- AI REQUEST PACKAGES (Daily AI Assistant requests)
-- ============================================================================

INSERT INTO feature_packages (code, name, description, feature_type, quota, duration_days, price) VALUES
-- Free tier
('AI_FREE', 'AI Miễn phí', 'Gói miễn phí. 10 lượt AI assistant mỗi ngày.', 'AI_REQUEST', 10, -1, 0.00),
-- Paid tiers
('AI_50', 'Gói 50 AI/ngày', 'Tăng lên 50 lượt AI assistant mỗi ngày trong 30 ngày.', 'AI_REQUEST', 50, 30, 39000.00),
('AI_100', 'Gói 100 AI/ngày', 'Tăng lên 100 lượt AI assistant mỗi ngày trong 30 ngày.', 'AI_REQUEST', 100, 30, 69000.00),
('AI_200', 'Gói 200 AI/ngày', 'Tăng lên 200 lượt AI assistant mỗi ngày trong 30 ngày.', 'AI_REQUEST', 200, 30, 119000.00),
('AI_UNLIMITED', 'AI Không giới hạn', 'Không giới hạn lượt AI assistant trong 30 ngày.', 'AI_REQUEST', -1, 30, 199000.00);

-- ============================================================================
-- COMBO PACKAGES (Optional - for users who want bundles)
-- These are still individual feature purchases but marketed as combos
-- ============================================================================

-- Note: If you want combo packages, create them as separate feature_type or 
-- handle in application layer by allowing users to add multiple packages to cart
