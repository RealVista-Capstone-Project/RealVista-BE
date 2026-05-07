-- Seed deterministic data for testing rented-property repost and lease-expiry reminders.
-- Accounts used:
--   owner049@realvista.com
--   buyertenantuser001@realvista.com

-- Ensure the two test accounts exist and are active. The password hash matches existing sample users.
INSERT INTO users (user_id, first_name, last_name, business_name, password_hash, email, phone, status,
                   created_at, updated_at, deleted)
VALUES
    ('550e8400-e29b-41d4-a716-446655440609', 'Sơn', 'Ngô Quang', 'Ngô Quang Sơn Properties',
     '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner049@realvista.com',
     '+84901000609', 'ACTIVE', NOW(), NOW(), FALSE),
    ('550e8400-e29b-41d4-a716-446655440301', 'Buyer', 'Tenant', 'BT User One',
     '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser001@realvista.com',
     '+84901000301', 'ACTIVE', NOW(), NOW(), FALSE)
ON CONFLICT (email) DO UPDATE
SET status = 'ACTIVE',
    deleted = FALSE,
    updated_at = NOW();

-- Ensure roles needed for frontend testing.
INSERT INTO user_roles (user_id, role_id, assigned_at, created_at, updated_at, deleted)
SELECT u.user_id, r.role_id, NOW(), NOW(), NOW(), FALSE
FROM users u
JOIN roles r ON r.role_code IN ('OWNER', 'BUYER', 'TENANT')
WHERE u.email = 'owner049@realvista.com'
ON CONFLICT (user_id, role_id) DO UPDATE
SET deleted = FALSE,
    updated_at = NOW();

INSERT INTO user_roles (user_id, role_id, assigned_at, created_at, updated_at, deleted)
SELECT u.user_id, r.role_id, NOW(), NOW(), NOW(), FALSE
FROM users u
JOIN roles r ON r.role_code IN ('BUYER', 'TENANT')
WHERE u.email = 'buyertenantuser001@realvista.com'
ON CONFLICT (user_id, role_id) DO UPDATE
SET deleted = FALSE,
    updated_at = NOW();

-- Rented property owned by owner049. It is intentionally still RENTED, but rent reposting is enabled.
INSERT INTO properties (property_id, owner_id, location_id, property_type_id, street_address, latitude, longitude,
                        land_size_m2, usable_size_m2, width_m, length_m, status, descriptions, slug,
                        extra_attributes, price_range, allow_rent_listing_when_rented,
                        created_at, updated_at, deleted)
SELECT 'f0490000-0000-0000-0000-000000000001', owner_user.user_id,
       '510e8400-e29b-41d4-a716-446655441002',
       '320e8400-e29b-41d4-a716-446655440001',
       'Lease Repost Test Apartment - Owner049',
       10.786500, 106.704500,
       85.00, 72.00, NULL, NULL,
       'RENTED',
       'Test property for rented-property repost flow. Current lease ends in 30 days.',
       'lease-repost-test-owner049',
       '{"bedrooms":2,"bathrooms":2}'::jsonb,
       '{"rent":{"min":18000000,"max":22000000},"buy":{"min":3000000000,"max":5000000000}}'::jsonb,
       TRUE,
       NOW(), NOW(), FALSE
FROM users owner_user
WHERE owner_user.email = 'owner049@realvista.com'
ON CONFLICT (property_id) DO UPDATE
SET owner_id = EXCLUDED.owner_id,
    status = 'RENTED',
    allow_rent_listing_when_rented = TRUE,
    descriptions = EXCLUDED.descriptions,
    price_range = EXCLUDED.price_range,
    deleted = FALSE,
    updated_at = NOW();

-- Historical/current rented listing. This should not block creating a new PUBLISHED repost listing.
INSERT INTO listings (listing_id, property_id, user_id, listing_type, status, price, min_price, max_price,
                      is_negotiable, available_from, published_at, slug, name, content,
                      has_been_published, created_at, updated_at, deleted)
SELECT 'f0491000-0000-0000-0000-000000000001',
       'f0490000-0000-0000-0000-000000000001',
       owner_user.user_id,
       'RENT', 'RENTED', 20000000.00, NULL, NULL,
       FALSE, (CURRENT_DATE - INTERVAL '11 months')::date, NOW() - INTERVAL '11 months',
       'lease-repost-test-owner049-current-rented',
       'Current rented apartment for repost testing',
       'Current rented listing seeded for lease repost frontend testing.',
       TRUE, NOW() - INTERVAL '11 months', NOW(), FALSE
FROM users owner_user
WHERE owner_user.email = 'owner049@realvista.com'
ON CONFLICT (listing_id) DO UPDATE
SET status = 'RENTED',
    price = EXCLUDED.price,
    available_from = EXCLUDED.available_from,
    has_been_published = TRUE,
    deleted = FALSE,
    updated_at = NOW();

-- Active lease ending in 30 days. Frontend should repost with availableFrom >= lease_end_date + 1.
INSERT INTO lease_agreements (lease_agreement_id, property_id, renter_id, landlord_id, agent_id,
                              lease_start_date, lease_end_date, lease_duration_months,
                              monthly_rent, security_deposit, lease_document_url,
                              signed_by_renter_at, signed_by_landlord_at,
                              status, reject_reason, termination_reason, terminated_at,
                              cancel_reason, cancelled_at, cancelled_by, verified_by,
                              docusign_envelope_id, docusign_status,
                              signed_document_url, signed_document_status, signed_document_error,
                              signed_document_processed_at,
                              expiry_reminder_30_sent_at, expiry_reminder_7_sent_at,
                              expiry_reminder_due_sent_at,
                              created_at, updated_at, deleted)
SELECT 'f0492000-0000-0000-0000-000000000001',
       'f0490000-0000-0000-0000-000000000001',
       tenant_user.user_id,
       owner_user.user_id,
       NULL,
       (CURRENT_DATE - INTERVAL '11 months')::date,
       (CURRENT_DATE + INTERVAL '30 days')::date,
       12,
       20000000.00, 40000000.00,
       'https://example.com/lease-repost-test-owner049.pdf',
       NOW() - INTERVAL '11 months', NOW() - INTERVAL '11 months',
       'ACTIVE', NULL, NULL, NULL,
       NULL, NULL, NULL, NULL,
       'seed-owner049-active-lease', 'completed',
       NULL, 'NOT_REQUESTED', NULL, NULL,
       NULL, NULL, NULL,
       NOW() - INTERVAL '11 months', NOW(), FALSE
FROM users owner_user
JOIN users tenant_user ON tenant_user.email = 'buyertenantuser001@realvista.com'
WHERE owner_user.email = 'owner049@realvista.com'
ON CONFLICT (lease_agreement_id) DO UPDATE
SET renter_id = EXCLUDED.renter_id,
    landlord_id = EXCLUDED.landlord_id,
    lease_start_date = EXCLUDED.lease_start_date,
    lease_end_date = EXCLUDED.lease_end_date,
    status = 'ACTIVE',
    expiry_reminder_30_sent_at = NULL,
    expiry_reminder_7_sent_at = NULL,
    expiry_reminder_due_sent_at = NULL,
    deleted = FALSE,
    updated_at = NOW();
