-- V46: Add diverse engagement samples for owner041 (user_id: 550e8400-e29b-41d4-a716-446655440601)
--
-- owner041 owns:
--   a1100000-0000-0000-0000-000000000001  Apartment,  101 Bến Nghé, Q1
--   a6100000-0000-0000-0000-000000000009  Villa,      609 Phường 26, Q7
--   c1100000-0000-0000-0000-000000000001  Office,     1001 Phạm Ngũ Lão, Q1
--   c6100000-0000-0000-0000-000000000001  Hotel,      1001 Phường An Phú, Q2
--
-- Agents used (V11: 440101-440140 / V12: 440501-440600) — all new pairs, no duplicates with V43.
-- Engagement ID prefix: e0000000-0006-0000-0000-00000000000X

INSERT INTO engagements (
    engagement_id, initiator_id, receiver_id, engagement_type, content,
    listing_id, property_id, status, cancellation_reason,
    created_at, updated_at, deleted
) VALUES

-- 1. SUBMITTED — agent 440512 proposed to owner041 for the Apartment (pending review)
('e0000000-0006-0000-0000-000000000001',
 '550e8400-e29b-41d4-a716-446655440512',
 '550e8400-e29b-41d4-a716-446655440601',
 'AGENT_PROPOSAL',
 '{"message": "I have recent sales experience in Bến Nghé, Q1. Happy to discuss commission.", "proposedCommission": "2.5%"}',
 NULL, 'a1100000-0000-0000-0000-000000000001',
 'SUBMITTED', NULL,
 NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', false),

-- 2. ACCEPTED — agent 440525 proposed to owner041 for the Villa (active contract)
('e0000000-0006-0000-0000-000000000002',
 '550e8400-e29b-41d4-a716-446655440525',
 '550e8400-e29b-41d4-a716-446655440601',
 'AGENT_PROPOSAL',
 '{"message": "Luxury villa specialist with international buyer network. Commission negotiable.", "proposedCommission": "3.0%"}',
 NULL, 'a6100000-0000-0000-0000-000000000009',
 'ACCEPTED', NULL,
 NOW() - INTERVAL '20 days', NOW() - INTERVAL '18 days', false),

-- 3. FINISHED — owner041 invited agent 440535 for the Office (contract completed, reviewable)
('e0000000-0006-0000-0000-000000000003',
 '550e8400-e29b-41d4-a716-446655440601',
 '550e8400-e29b-41d4-a716-446655440535',
 'OWNER_INVITATION',
 '{"message": "Looking for a commercial real estate expert to represent our office space in Q1.", "offeredCommission": "2.0%"}',
 NULL, 'c1100000-0000-0000-0000-000000000001',
 'FINISHED', NULL,
 NOW() - INTERVAL '60 days', NOW() - INTERVAL '10 days', false),

-- 4. CANCELLED (with reason) — owner041 invited agent 440545 for the Hotel, then cancelled
('e0000000-0006-0000-0000-000000000004',
 '550e8400-e29b-41d4-a716-446655440601',
 '550e8400-e29b-41d4-a716-446655440545',
 'OWNER_INVITATION',
 '{"message": "Need experienced hospitality property agent for hotel sale.", "offeredCommission": "2.5%"}',
 NULL, 'c6100000-0000-0000-0000-000000000001',
 'CANCELLED', 'Agent was unresponsive and missed multiple scheduled property viewings.',
 NOW() - INTERVAL '50 days', NOW() - INTERVAL '35 days', false),

-- 5. FINISHED — agent 440538 proposed to owner041 for the Hotel (contract completed, reviewable)
('e0000000-0006-0000-0000-000000000005',
 '550e8400-e29b-41d4-a716-446655440538',
 '550e8400-e29b-41d4-a716-446655440601',
 'AGENT_PROPOSAL',
 '{"message": "I specialize in high-value commercial and hospitality properties.", "proposedCommission": "3.5%"}',
 NULL, 'c6100000-0000-0000-0000-000000000001',
 'FINISHED', NULL,
 NOW() - INTERVAL '90 days', NOW() - INTERVAL '45 days', false);

-- Backfill listing_id for the new rows using the same formula as V45
UPDATE engagements e
SET listing_id = (
    SELECT md5(p.property_id::text || a.agent_id::text)::uuid
    FROM properties p
    JOIN property_agents a ON a.property_id = p.property_id
    WHERE p.property_id = e.property_id
    LIMIT 1
)
WHERE e.engagement_id IN (
    'e0000000-0006-0000-0000-000000000001',
    'e0000000-0006-0000-0000-000000000002',
    'e0000000-0006-0000-0000-000000000003',
    'e0000000-0006-0000-0000-000000000004',
    'e0000000-0006-0000-0000-000000000005'
)
AND listing_id IS NULL
AND property_id IS NOT NULL;
