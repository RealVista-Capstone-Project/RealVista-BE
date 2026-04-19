-- V96__Seed_free_subscriptions_for_sample_users.sql
-- Seeds free subscription packages for existing sample users based on their roles.
-- Idempotent: uses WHERE NOT EXISTS to skip users who already have the subscription.
--
-- Business rules:
--   AGENT role                       → AI_FREE + LISTING_FREE + 3D_TOUR_FREE
--   BUYER + TENANT + OWNER roles     → AI_FREE + LISTING_FREE + 3D_TOUR_FREE
--   BUYER + TENANT only              → AI_FREE only
--   ADMIN / VERIFIER / suspended / banned → skipped

-- ============================================================================
-- Helper: CTE to classify users by role group
-- ============================================================================

-- 1. AGENTS → all 3 free packages
INSERT INTO user_feature_subscriptions (user_feature_subscription_id, user_id, feature_package_id, start_date, end_date, remaining_quota, status, created_at, updated_at, deleted)
SELECT gen_random_uuid(), u.user_id, fp.feature_package_id, CURRENT_DATE, NULL, fp.quota, 'ACTIVE', NOW(), NOW(), FALSE
FROM users u
JOIN user_roles ur ON ur.user_id = u.user_id
JOIN roles r ON r.role_id = ur.role_id AND r.role_code = 'AGENT'
CROSS JOIN feature_packages fp
WHERE fp.code IN ('AI_FREE', 'LISTING_FREE', '3D_TOUR_FREE')
  AND u.deleted = FALSE
  AND fp.deleted = FALSE
  AND u.status = 'ACTIVE'
  AND NOT EXISTS (
      SELECT 1 FROM user_feature_subscriptions ufs
      JOIN feature_packages fp2 ON fp2.feature_package_id = ufs.feature_package_id
      WHERE ufs.user_id = u.user_id
        AND fp2.code = fp.code
        AND ufs.status = 'ACTIVE'
        AND ufs.deleted = FALSE
  );

-- 2. OWNERS (users with OWNER role, excluding agents) → all 3 free packages
INSERT INTO user_feature_subscriptions (user_feature_subscription_id, user_id, feature_package_id, start_date, end_date, remaining_quota, status, created_at, updated_at, deleted)
SELECT gen_random_uuid(), u.user_id, fp.feature_package_id, CURRENT_DATE, NULL, fp.quota, 'ACTIVE', NOW(), NOW(), FALSE
FROM users u
JOIN user_roles ur ON ur.user_id = u.user_id
JOIN roles r ON r.role_id = ur.role_id AND r.role_code = 'OWNER'
CROSS JOIN feature_packages fp
WHERE fp.code IN ('AI_FREE', 'LISTING_FREE', '3D_TOUR_FREE')
  AND u.deleted = FALSE
  AND fp.deleted = FALSE
  AND u.status = 'ACTIVE'
  AND NOT EXISTS (
      SELECT 1 FROM user_feature_subscriptions ufs
      JOIN feature_packages fp2 ON fp2.feature_package_id = ufs.feature_package_id
      WHERE ufs.user_id = u.user_id
        AND fp2.code = fp.code
        AND ufs.status = 'ACTIVE'
        AND ufs.deleted = FALSE
  );

-- 3. BUYER+TENANT only (no OWNER, no AGENT) → AI_FREE only
INSERT INTO user_feature_subscriptions (user_feature_subscription_id, user_id, feature_package_id, start_date, end_date, remaining_quota, status, created_at, updated_at, deleted)
SELECT gen_random_uuid(), u.user_id, fp.feature_package_id, CURRENT_DATE, NULL, fp.quota, 'ACTIVE', NOW(), NOW(), FALSE
FROM users u
JOIN user_roles ur ON ur.user_id = u.user_id
JOIN roles r ON r.role_id = ur.role_id AND r.role_code = 'BUYER'
CROSS JOIN feature_packages fp
WHERE fp.code = 'AI_FREE'
  AND u.deleted = FALSE
  AND fp.deleted = FALSE
  AND u.status = 'ACTIVE'
  -- Exclude users who also have AGENT or OWNER roles (they were handled above)
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur2
      JOIN roles r2 ON r2.role_id = ur2.role_id
      WHERE ur2.user_id = u.user_id AND r2.role_code IN ('AGENT', 'OWNER')
  )
  AND NOT EXISTS (
      SELECT 1 FROM user_feature_subscriptions ufs
      JOIN feature_packages fp2 ON fp2.feature_package_id = ufs.feature_package_id
      WHERE ufs.user_id = u.user_id
        AND fp2.code = fp.code
        AND ufs.status = 'ACTIVE'
        AND ufs.deleted = FALSE
  );
