-- V103__Seed_3d_media_for_listing2_and_listing3.sql
-- Fixes two issues for the 3 listings of property a1100000-0000-0000-0000-000000000001:
--
-- Issue 1: V48 set listing2 (agent 440601) and listing3 (agent 440602) to DRAFT because
--          those agents have other properties → other listings with smaller UUIDs.
--
-- Issue 2: V23 uses RANDOM() for listing_type, so listing1 (agent 440101) could be RENT,
--          making it invisible on the buy page has3D filter.
--
-- Fix: Force all 3 listings to status=PUBLISHED, listing_type=SALE, then link 3D media.
--
-- Listing IDs (deterministic via V23: md5(property_id || agent_id)::uuid):
--   27199eda-c29e-7a94-c7cc-93959e8115cc  = agent 440101  (already has 3D from V80)
--   aba6e5ee-bfa7-6959-eeab-35a5b64abe7a  = agent 440601
--   c0f2ed49-b258-5038-55e8-ea5241823f0e  = agent 440602
--
-- 3D property_medias (seeded in V80):
--   a0e1034f-c798-4bbd-aea9-22c4b0b64861  (room: Okelahi)
--   534c6c9e-be09-4d43-a52e-ca6f2698ebd8  (room: Test2)

DO $$
DECLARE
    v_listing1_id   UUID := '27199eda-c29e-7a94-c7cc-93959e8115cc'; -- agent 440101
    v_listing2_id   UUID := 'aba6e5ee-bfa7-6959-eeab-35a5b64abe7a'; -- agent 440601
    v_listing3_id   UUID := 'c0f2ed49-b258-5038-55e8-ea5241823f0e'; -- agent 440602
    v_media_okelahi UUID := 'a0e1034f-c798-4bbd-aea9-22c4b0b64861';
    v_media_test2   UUID := '534c6c9e-be09-4d43-a52e-ca6f2698ebd8';
BEGIN

    -- =========================================================================
    -- Fix listing status and type so all 3 appear on the buy-page has3D filter
    -- =========================================================================
    UPDATE listings
    SET status       = 'PUBLISHED',
        listing_type = 'SALE',
        updated_at   = NOW()
    WHERE listing_id IN (v_listing1_id, v_listing2_id, v_listing3_id)
      AND deleted = FALSE;

    -- =========================================================================
    -- Listing 2 (agent 440601) — link 3D media
    -- =========================================================================
    INSERT INTO listing_medias (
        listing_id, property_media_id,
        display_order, is_primary,
        created_at, updated_at, deleted
    )
    VALUES
        (v_listing2_id, v_media_okelahi, 100, FALSE, NOW(), NOW(), FALSE),
        (v_listing2_id, v_media_test2,   101, FALSE, NOW(), NOW(), FALSE)
    ON CONFLICT (listing_id, property_media_id) DO NOTHING;

    -- =========================================================================
    -- Listing 3 (agent 440602) — link 3D media
    -- =========================================================================
    INSERT INTO listing_medias (
        listing_id, property_media_id,
        display_order, is_primary,
        created_at, updated_at, deleted
    )
    VALUES
        (v_listing3_id, v_media_okelahi, 100, FALSE, NOW(), NOW(), FALSE),
        (v_listing3_id, v_media_test2,   101, FALSE, NOW(), NOW(), FALSE)
    ON CONFLICT (listing_id, property_media_id) DO NOTHING;

END $$;
