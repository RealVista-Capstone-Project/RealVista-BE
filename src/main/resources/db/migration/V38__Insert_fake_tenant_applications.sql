-- V38__Insert_fake_tenant_applications.sql
-- Insert sample tenant applications for Tenant 1 (Buyer/Tenant User One)
-- user_id: 550e8400-e29b-41d4-a716-446655440301

INSERT INTO tenant_applications (
    tenant_application_id, user_id, listing_id, title, monthly_income, move_in_date, lease_term_months, status, note, created_at, updated_at
)
SELECT 
    gen_random_uuid(),
    '550e8400-e29b-41d4-a716-446655440301',
    l.listing_id,
    'Application for ' || l.name,
    5000.00,
    CURRENT_DATE,
    12,
    'ACTIVE',
    'I am very interested in this property. I have a stable job and can move in anytime.',
    NOW(),
    NOW()
FROM listings l
WHERE l.listing_type = 'RENT' AND l.status = 'PUBLISHED'
LIMIT 2;

INSERT INTO tenant_applications (
    tenant_application_id, user_id, listing_id, title, monthly_income, move_in_date, lease_term_months, status, note, created_at, updated_at
)
SELECT 
    gen_random_uuid(),
    '550e8400-e29b-41d4-a716-446655440301',
    l.listing_id,
    'Draft application for ' || l.name,
    4500.00,
    CURRENT_DATE,
    6,
    'DRAFT',
    'Still considering this property with my family.',
    NOW(),
    NOW()
FROM listings l
WHERE l.listing_type = 'RENT' AND l.status = 'PUBLISHED'
LIMIT 1 OFFSET 2;

INSERT INTO tenant_applications (
    tenant_application_id, user_id, listing_id, title, monthly_income, move_in_date, lease_term_months, status, note, created_at, updated_at
)
SELECT 
    gen_random_uuid(),
    '550e8400-e29b-41d4-a716-446655440301',
    l.listing_id,
    'Archived application for ' || l.name,
    5500.00,
    CURRENT_DATE,
    12,
    'ARCHIVED',
    'I found another property that suits my needs better.',
    NOW(),
    NOW()
FROM listings l
WHERE l.listing_type = 'RENT' AND l.status = 'PUBLISHED'
LIMIT 1 OFFSET 3;
