-- V90__Seed_3d_tour_free_for_agent001.sql
-- Seeds a 3D_TOUR_FREE subscription for agent001@realvista.com
-- so the 3D management page has an active quota to display.

INSERT INTO user_feature_subscriptions (
    user_feature_subscription_id,
    user_id,
    feature_package_id,
    start_date,
    end_date,
    remaining_quota,
    status,
    created_at,
    updated_at,
    deleted
)
SELECT
    gen_random_uuid(),
    u.user_id,
    fp.feature_package_id,
    CURRENT_DATE,
    NULL,       -- duration_days = -1 means no expiry
    fp.quota,   -- remaining_quota = 1
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
FROM users u
JOIN feature_packages fp ON fp.code = '3D_TOUR_FREE'
WHERE u.email = 'agent001@realvista.com'
  AND u.deleted = FALSE
  AND fp.deleted = FALSE;
