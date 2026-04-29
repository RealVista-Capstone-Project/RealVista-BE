-- V119__Add_Unique_Avatar_To_Agents_And_Owners.sql
-- Assigns unique Unsplash portrait images to all OWNER and AGENT users.

WITH photo_list AS (
  SELECT unnest(ARRAY[
    'https://images.unsplash.com/photo-1611983512040-b8e48141cf4a',
    'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04',
    'https://images.unsplash.com/photo-1531384441138-2736e62e0919',
    'https://images.unsplash.com/photo-1566753323558-f4e0952af115',
    'https://images.unsplash.com/photo-1552234994-66ba234fd567',
    'https://images.unsplash.com/photo-1492288991661-058aa541ff43',
    'https://images.unsplash.com/photo-1524504388940-b1c1722653e1',
    'https://images.unsplash.com/photo-1610276198568-eb6d0ff53e48',
    'https://images.unsplash.com/photo-1602077422495-c8733eb58c34',
    'https://images.unsplash.com/photo-1568602471122-7832951cc4c5',
    'https://images.unsplash.com/photo-1632765854612-9b02b6ec2b15',
    'https://images.unsplash.com/photo-1534528741775-53994a69daeb',
    'https://images.unsplash.com/photo-1618151313441-bc79b11e5090',
    'https://images.unsplash.com/photo-1438761681033-6461ffad8d80',
    'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d'
  ]) as base_url,
  generate_series(1, array_length(ARRAY[
    '1611983512040-b8e48141cf4a',
    '1531746020798-e6953c6e8e04',
    '1531384441138-2736e62e0919',
    '1566753323558-f4e0952af115',
    '1552234994-66ba234fd567',
    '1492288991661-058aa541ff43',
    '1524504388940-b1c1722653e1',
    '1610276198568-eb6d0ff53e48',
    '1602077422495-c8733eb58c34',
    '1568602471122-7832951cc4c5',
    '1632765854612-9b02b6ec2b15',
    '1534528741775-53994a69daeb',
    '1618151313441-bc79b11e5090',
    '1438761681033-6461ffad8d80',
    '1506794778202-cad84cf45f1d'
  ], 1)) as idx
),
target_users AS (
  SELECT u.user_id,
         ROW_NUMBER() OVER(ORDER BY u.user_id) as rn
  FROM users u
  JOIN user_roles ur ON ur.user_id = u.user_id
  JOIN roles r ON r.role_id = ur.role_id
  WHERE r.role_code IN ('OWNER', 'AGENT')
)
UPDATE users
SET avatar_url = (SELECT base_url || '?q=80&w=256&h=256&auto=format&fit=crop&crop=faces,entropy' 
                  FROM photo_list 
                  WHERE idx = ((target_users.rn - 1) % (SELECT count(*) FROM photo_list)) + 1),
    updated_at = NOW()
FROM target_users
WHERE users.user_id = target_users.user_id;
