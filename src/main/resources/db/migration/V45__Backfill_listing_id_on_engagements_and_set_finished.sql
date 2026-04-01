-- V45__Backfill_listing_id_on_engagements_and_set_finished.sql
--
-- Problem: V43 seeded all engagements with listing_id = NULL, but agent_reviews.listing_id
-- is NOT NULL. This means the review API will always block with "no associated listing".
--
-- Fix:
--   1. Backfill listing_id for V43 engagements by joining with the listings table.
--      Listings were created in V23 using md5(property_id || agent_id) as listing_id,
--      so the listing for each engagement is: listings WHERE property_id = engagement.property_id
--      AND user_id = <agent_id from engagement>.
--      For AGENT_PROPOSAL: agent = initiator_id
--      For OWNER_INVITATION: agent = receiver_id
--
--   2. Set a representative subset of ACCEPTED engagements to FINISHED so the
--      review API (which requires FINISHED or CANCELLED) can be tested.
--
-- Compatible with PostgreSQL (production/dev) and H2 (testing).

-- ============================================================================
-- 1. BACKFILL listing_id: AGENT_PROPOSAL engagements (agent = initiator_id)
-- ============================================================================

UPDATE engagements e
SET listing_id = (
    SELECT l.listing_id
    FROM listings l
    WHERE l.property_id = e.property_id
      AND l.user_id = e.initiator_id
      AND l.deleted = FALSE
    LIMIT 1
)
WHERE e.listing_id IS NULL
  AND e.property_id IS NOT NULL
  AND e.engagement_type = 'AGENT_PROPOSAL'
  AND e.deleted = FALSE;

-- ============================================================================
-- 2. BACKFILL listing_id: OWNER_INVITATION engagements (agent = receiver_id)
-- ============================================================================

UPDATE engagements e
SET listing_id = (
    SELECT l.listing_id
    FROM listings l
    WHERE l.property_id = e.property_id
      AND l.user_id = e.receiver_id
      AND l.deleted = FALSE
    LIMIT 1
)
WHERE e.listing_id IS NULL
  AND e.property_id IS NOT NULL
  AND e.engagement_type = 'OWNER_INVITATION'
  AND e.deleted = FALSE;

-- ============================================================================
-- 3. SET FINISHED STATUS on a representative subset of ACCEPTED engagements
--
--    These span multiple owners and property types so any owner can test the
--    finish → review flow end-to-end.
--
--    Owner 440601: e01 (Agent 440101, Apt), e17 (Agent 440559, Office),
--                  e0002-015 (Agent 440134, Hotel), e0005-001 (Agent 440551, Villa)
--    Owner 440602: e02 (Agent 440102, Apt), e18 (Agent 440560, Office)
--    Owner 440603: e03 (Agent 440103, Apt)
--    Owner 440623: e06 (Agent 440117, House)
--    Owner 440645: e11 (Agent 440134, Townhouse)
--    Owner 440685: e14 (Agent 440543, Villa)
--    Owner 440606: e0002-001 (Agent 440106, Apt)
--    Owner 440667: e0002-010 (Agent 440510, Penthouse)
-- ============================================================================

UPDATE engagements
SET status = 'FINISHED', updated_at = NOW() - INTERVAL '5 days'
WHERE engagement_id IN (
    'e0000000-0001-0000-0000-000000000001',
    'e0000000-0001-0000-0000-000000000002',
    'e0000000-0001-0000-0000-000000000003',
    'e0000000-0001-0000-0000-000000000006',
    'e0000000-0001-0000-0000-000000000011',
    'e0000000-0001-0000-0000-000000000014',
    'e0000000-0001-0000-0000-000000000017',
    'e0000000-0002-0000-0000-000000000001',
    'e0000000-0002-0000-0000-000000000010',
    'e0000000-0002-0000-0000-000000000015',
    'e0000000-0005-0000-0000-000000000001'
)
  AND deleted = FALSE;
