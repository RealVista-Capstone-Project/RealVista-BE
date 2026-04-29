-- V121__Add_original_quota_to_user_feature_subscriptions.sql
-- Snapshot the quota at purchase time so admin updates to FeaturePackage
-- do not retroactively affect existing subscriptions' display cap.

ALTER TABLE user_feature_subscriptions
    ADD COLUMN original_quota INTEGER;

-- Backfill: best approximation is remaining_quota (already tracked per-sub)
UPDATE user_feature_subscriptions
SET original_quota = remaining_quota
WHERE original_quota IS NULL;

COMMENT ON COLUMN user_feature_subscriptions.original_quota
    IS 'Quota snapshotted at purchase time — never changes after checkout';
