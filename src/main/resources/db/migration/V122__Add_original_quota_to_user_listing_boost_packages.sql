-- V122__Add_original_quota_to_user_listing_boost_packages.sql
-- Snapshot featured/hot-badge quotas at purchase time so admin updates to
-- BoostPackage do not retroactively affect existing active boosts.

ALTER TABLE user_listing_boost_packages
    ADD COLUMN original_featured_quota  INTEGER,
    ADD COLUMN original_hot_badge_quota INTEGER;

-- Backfill: best approximation from remaining quotas
UPDATE user_listing_boost_packages
SET original_featured_quota  = remaining_featured_quota,
    original_hot_badge_quota = remaining_hot_badge_quota
WHERE original_featured_quota IS NULL;

COMMENT ON COLUMN user_listing_boost_packages.original_featured_quota
    IS 'Featured quota snapshotted at purchase time — never changes after checkout';
COMMENT ON COLUMN user_listing_boost_packages.original_hot_badge_quota
    IS 'Hot badge quota snapshotted at purchase time — never changes after checkout';
