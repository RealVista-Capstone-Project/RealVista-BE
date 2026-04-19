-- V98__Seed_paid_subscription_and_boost_for_buyer001.sql
-- Seeds a paid AI_50 subscription for buyertenantuser001,
-- along with related checkout_order and transaction records.

-- ============================================================================
-- 1. Paid feature subscription: AI_50 (39,000 VND, 30 days, 50 requests/day)
-- ============================================================================
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
    '00000000-0000-0000-0000-000000980001',
    u.user_id,
    fp.feature_package_id,
    CURRENT_DATE,
    CURRENT_DATE + 30,
    fp.quota,
    'ACTIVE',
    NOW(),
    NOW(),
    FALSE
FROM users u
JOIN feature_packages fp ON fp.code = 'AI_50'
WHERE u.email = 'buyertenantuser001@realvista.com'
  AND u.deleted = FALSE
  AND fp.deleted = FALSE
  AND NOT EXISTS (
      SELECT 1 FROM user_feature_subscriptions ufs
      WHERE ufs.user_id = u.user_id
        AND ufs.feature_package_id = fp.feature_package_id
        AND ufs.status = 'ACTIVE'
        AND ufs.deleted = FALSE
  );

-- ============================================================================
-- 2. Checkout order for the AI subscription purchase
-- ============================================================================
INSERT INTO checkout_orders (
    checkout_order_id,
    user_id,
    transaction_type,
    plan_code,
    amount,
    payment_method,
    order_code,
    created_at,
    updated_at,
    deleted
)
SELECT
    '00000000-0000-0000-0000-000000980003',
    u.user_id,
    'SUBSCRIPTION',
    'AI_50',
    39000.00,
    'VNPAY',
    980001,
    NOW(),
    NOW(),
    FALSE
FROM users u
WHERE u.email = 'buyertenantuser001@realvista.com'
  AND u.deleted = FALSE
  AND NOT EXISTS (
      SELECT 1 FROM checkout_orders co WHERE co.order_code = 980001
  );

-- ============================================================================
-- 3. Transaction for the AI subscription (COMPLETED)
-- ============================================================================
INSERT INTO transactions (
    transaction_id,
    user_id,
    transaction_type,
    reference_id,
    order_code,
    plan_code,
    amount,
    payment_method,
    payment_status,
    created_at,
    updated_at,
    deleted
)
SELECT
    '00000000-0000-0000-0000-000000980005',
    u.user_id,
    'SUBSCRIPTION',
    '00000000-0000-0000-0000-000000980001',
    980001,
    'AI_50',
    39000.00,
    'VNPAY',
    'COMPLETED',
    NOW(),
    NOW(),
    FALSE
FROM users u
WHERE u.email = 'buyertenantuser001@realvista.com'
  AND u.deleted = FALSE
  AND NOT EXISTS (
      SELECT 1 FROM transactions t WHERE t.order_code = 980001
  );
