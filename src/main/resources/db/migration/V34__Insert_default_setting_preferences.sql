-- V34__Insert_default_setting_preferences.sql
-- Migration V34: Insert default setting preferences for all non-admin users
-- All options set to TRUE except event_preference which is NULL

-- ============================================================================
-- Insert default setting preferences for non-admin users
-- ============================================================================
WITH non_admin_users AS (
    SELECT DISTINCT u.user_id
    FROM users u
    WHERE u.deleted = FALSE
      AND u.user_id NOT IN (
          -- Exclude users who have ADMIN role
          SELECT DISTINCT u2.user_id
          FROM users u2
          JOIN user_roles ur2 ON u2.user_id = ur2.user_id
          JOIN roles r2 ON ur2.role_id = r2.role_id
          WHERE r2.role_code = 'ADMIN'
      )
      AND u.user_id NOT IN (
          -- Exclude users who already have preferences
          SELECT sp.user_id 
          FROM setting_preferences sp 
          WHERE sp.deleted = FALSE
      )
)
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
    nau.user_id,
    TRUE as in_app_enabled,
    TRUE as email_enabled,
    TRUE as push_enabled,
    NULL::json as event_preference, -- Set to NULL as requested
    TRUE as contact_via_email,
    TRUE as contact_via_phone,  -- Override default FALSE to TRUE
    TRUE as hide_phone_number,
    TRUE as hide_email
FROM non_admin_users nau;