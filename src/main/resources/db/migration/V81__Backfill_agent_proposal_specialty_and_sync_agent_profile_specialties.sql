-- Backfill seeded proposal specialties (UUID FK) and sync agent profile specialties text.
-- Targets proposals inserted by V68__Final_Professional_Vietnamese_Proposals.sql
--
-- Strategy:
-- 1) Assign a property_type_id to agent_proposals.specialty based on proposal title keywords.
-- 2) Backfill price_range JSONB if missing.
-- 3) Append the related property_types.name to agent_profiles.specialties (comma-separated, deduped).

WITH agent001 AS (
    SELECT user_id
    FROM users
    WHERE email = 'agent001@realvista.com'
),
pt AS (
    SELECT
        (SELECT property_type_id FROM property_types WHERE code = 'APARTMENT' LIMIT 1) AS apartment_id,
        (SELECT property_type_id FROM property_types WHERE code = 'VILLA' LIMIT 1) AS villa_id,
        (SELECT property_type_id FROM property_types WHERE code = 'SHOPHOUSE' LIMIT 1) AS shophouse_id,
        (SELECT property_type_id FROM property_types WHERE code = 'COMMERCIAL' LIMIT 1) AS commercial_id
),
pr AS (
    SELECT '{"rent": {"max": 456, "min": 123}, "sale": {"max": 101112, "min": 7889}}'::jsonb AS default_price_range
),
updated AS (
    UPDATE agent_proposals ap
    SET specialty = CASE
            WHEN ap.specialty IS NULL AND ap.title ILIKE '%Căn hộ%' THEN (SELECT apartment_id FROM pt)
            WHEN ap.specialty IS NULL AND ap.title ILIKE '%Biệt thự%' THEN (SELECT villa_id FROM pt)
            WHEN ap.specialty IS NULL AND (ap.title ILIKE '%Shophouse%' OR ap.title ILIKE '%Mặt bằng%')
                THEN (SELECT shophouse_id FROM pt)
            ELSE ap.specialty
        END,
        price_range = COALESCE(ap.price_range, (SELECT default_price_range FROM pr))
    WHERE ap.user_id = (SELECT user_id FROM agent001)
      AND (ap.specialty IS NULL OR ap.price_range IS NULL)
    RETURNING ap.user_id, ap.specialty
),
specialty_names AS (
    SELECT DISTINCT
        u.user_id,
        ptt.name AS property_type_name
    FROM updated u
    JOIN property_types ptt ON ptt.property_type_id = u.specialty
    WHERE u.specialty IS NOT NULL
),
merged AS (
    SELECT
        ap.user_id,
        sn.property_type_name,
        ap.specialties AS existing_specialties,
        CASE
            WHEN ap.specialties IS NULL OR btrim(ap.specialties) = '' THEN sn.property_type_name
            WHEN EXISTS (
                SELECT 1
                FROM unnest(regexp_split_to_array(ap.specialties, '\s*,\s*')) AS s(item)
                WHERE lower(btrim(s.item)) = lower(btrim(sn.property_type_name))
            ) THEN ap.specialties
            ELSE ap.specialties || ', ' || sn.property_type_name
        END AS new_specialties
    FROM agent_profiles ap
    JOIN specialty_names sn ON sn.user_id = ap.user_id
)
UPDATE agent_profiles ap
SET specialties = m.new_specialties
FROM merged m
WHERE ap.user_id = m.user_id
  AND ap.specialties IS DISTINCT FROM m.new_specialties;

