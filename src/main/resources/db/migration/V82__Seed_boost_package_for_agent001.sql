-- V81__Seed_boost_package_for_agent001.sql
-- Seeds a PREMIUM boost package for agent001@realvista.com so the boost section
-- in the listing management dashboard has data to display.

INSERT INTO user_listing_boost_packages (
    user_listing_boost_package_id,
    user_id,
    boost_package_id,
    start_date,
    end_date,
    remaining_featured_quota,
    remaining_hot_badge_quota,
    status,
    created_at,
    updated_at,
    deleted
)
SELECT
    gen_random_uuid(),
    u.user_id,
    bp.boost_package_id,
    CURRENT_DATE,
    CURRENT_DATE + INTERVAL '30 days',
    bp.featured_quota,
    bp.hot_badge_quota,
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
FROM users u
JOIN boost_packages bp ON bp.code = 'PREMIUM'
WHERE u.email = 'agent001@realvista.com'
  AND u.deleted = FALSE
  AND bp.deleted = FALSE;
