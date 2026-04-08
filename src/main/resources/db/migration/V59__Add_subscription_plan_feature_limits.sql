-- V59__Add_subscription_plan_feature_limits.sql
-- Adds feature limit columns and removes unused ai_features JSON column
-- Only daily_ai_requests is used to limit AI assistant usage (other AI features are free)

-- ============================================================================
-- DROP UNUSED ai_features JSON COLUMN
-- ============================================================================

ALTER TABLE subscription_plans
    DROP COLUMN IF EXISTS ai_features;

-- ============================================================================
-- ADD FEATURE LIMIT COLUMNS TO SUBSCRIPTION_PLANS
-- ============================================================================

ALTER TABLE subscription_plans
    ADD COLUMN max_active_listings INTEGER NOT NULL DEFAULT 5;

ALTER TABLE subscription_plans
    ADD COLUMN max_3d_tours INTEGER NOT NULL DEFAULT 1;

ALTER TABLE subscription_plans
    ADD COLUMN daily_ai_requests INTEGER NOT NULL DEFAULT 20;

-- Note: -1 means unlimited for all limit columns
-- PostgreSQL supports COMMENT ON COLUMN, H2 ignores it (compatible)
COMMENT ON COLUMN subscription_plans.max_active_listings IS 'Maximum active listings allowed. -1 = unlimited';
COMMENT ON COLUMN subscription_plans.max_3d_tours IS 'Maximum 3D virtual tours allowed. -1 = unlimited';
COMMENT ON COLUMN subscription_plans.daily_ai_requests IS 'Maximum AI assistant requests per day. -1 = unlimited';
