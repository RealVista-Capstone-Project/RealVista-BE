-- V40__Insert_fake_tenant_rental_profiles.sql
-- Insert fake rental profiles to act as CV/Templates for renting for Tenant 1
-- user_id: 550e8400-e29b-41d4-a716-446655440301

INSERT INTO tenant_rental_profiles (
    profile_id, user_id, title, monthly_income, move_in_date, lease_term_months, note
) VALUES 
('1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d', '550e8400-e29b-41d4-a716-446655440301', 'Default Rental Profile', 5000.00, CURRENT_DATE, 12, 'Looking for a quiet place.'),
('e1f2a3b4-c5d6-e7f8-a9b0-c1d2e3f4a5b6', '550e8400-e29b-41d4-a716-446655440301', 'Long Term Family Stay', 8000.00, CURRENT_DATE + INTERVAL '10' DAY, 24, 'Moving with my family.');

UPDATE tenant_applications
SET rental_profile_id = '1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d'
WHERE user_id = '550e8400-e29b-41d4-a716-446655440301';
