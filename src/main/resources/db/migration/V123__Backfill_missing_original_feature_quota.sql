-- V123__Backfill_missing_original_feature_quota.sql
-- Ensure legacy active feature subscriptions have a stable quota snapshot.
-- Finite packages use remaining_quota as the best available historical value.
-- Null remaining_quota represents unlimited, stored as -1.

UPDATE user_feature_subscriptions
SET original_quota = COALESCE(remaining_quota, -1)
WHERE original_quota IS NULL;
