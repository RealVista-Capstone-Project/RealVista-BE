-- V57__Insert_default_settings_and_customer_profiles.sql
-- 1. Insert default setting_preferences (all TRUE) for any remaining users without one
-- 2. Insert default customer_profiles for BUYER and TENANT users without one

-- ============================================================================
-- PART 1: Setting preferences for all non-admin users (idempotent)
-- ============================================================================
INSERT INTO setting_preferences (
    user_id,
    in_app_enabled,
    email_enabled,
    push_enabled,
    event_preference,
    contact_via_email,
    contact_via_phone,
    hide_phone_number,
    hide_email
)
SELECT
    u.user_id,
    TRUE,
    TRUE,
    TRUE,
    NULL::json,
    TRUE,
    TRUE,
    TRUE,
    TRUE
FROM users u
WHERE u.deleted = FALSE
  AND u.user_id NOT IN (
      SELECT ur.user_id
      FROM user_roles ur
      JOIN roles r ON ur.role_id = r.role_id
      WHERE r.role_code = 'ADMIN'
        AND ur.deleted = FALSE
  )
  AND NOT EXISTS (
      SELECT 1
      FROM setting_preferences sp
      WHERE sp.user_id = u.user_id
        AND sp.deleted = FALSE
  );

-- ============================================================================
-- PART 2: Default customer_profiles for BUYER and TENANT users (idempotent)
-- ============================================================================
INSERT INTO customer_profiles (
    user_id,
    profile_name,
    is_active
)
SELECT DISTINCT
    u.user_id,
    COALESCE(
        NULLIF(TRIM(COALESCE(u.first_name, '') || ' ' || COALESCE(u.last_name, '')), ''),
        u.business_name
    ),
    TRUE
FROM users u
JOIN user_roles ur ON ur.user_id = u.user_id AND ur.deleted = FALSE
JOIN roles r ON r.role_id = ur.role_id
WHERE u.deleted = FALSE
  AND r.role_code IN ('BUYER', 'TENANT')
  AND NOT EXISTS (
      SELECT 1
      FROM customer_profiles cp
      WHERE cp.user_id = u.user_id
        AND cp.deleted = FALSE
  );
