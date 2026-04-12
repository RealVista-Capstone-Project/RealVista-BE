-- V81: Add diverse engagement samples for owner001 with different engagement statuses
--
-- owner001 (user_id: 550e8400-e29b-41d4-a716-446655440401)
--
-- Properties owned by owner001:
--   a7100000-0000-0000-0000-000000000001  (Warehouse 701 Bến Nghé, Q1)
--   a7700000-0000-0000-0000-000000000001  (Industrial Land 1301 Tân Định, Q1)
--
-- Engagements where owner001 hires agents (OWNER_INVITATION type - owner is initiator):
--   Agent 001: ACCEPTED
--   Agent 002: SUBMITTED (pending)
--   Agent 003: REJECTED
--   Agent 007: CANCELLED
--   Agent 008: FINISHED
--
-- Engagements where agents propose to owner001 (AGENT_PROPOSAL type - agents are initiators):
--   Agent 004: SUBMITTED (pending)
--   Agent 005: ACCEPTED
--   Agent 006: REJECTED
--   Agent 009: CANCELLED
--   Agent 010: FINISHED

INSERT INTO engagements (
    engagement_id, initiator_id, receiver_id, engagement_type, content,
    listing_id, property_id, status, cancellation_reason,
    created_at, updated_at, deleted
) VALUES

-- ===== OWNER INITIATES (OWNER_INVITATION) =====

-- 1. ACCEPTED — owner001 hired agent 001 (active contract) for warehouse
('e0000000-0081-0000-0000-000000000001',
 '550e8400-e29b-41d4-a716-446655440401',
 '550e8400-e29b-41d4-a716-446655440101',
 'OWNER_INVITATION',
 '{"message": "Looking for a professional agent to represent our warehouse property.", "offeredCommission": "2.5%"}',
 NULL, 'a7100000-0000-0000-0000-000000000001',
 'ACCEPTED', NULL,
 NOW() - INTERVAL '30 days', NOW() - INTERVAL '25 days', false),

-- 2. SUBMITTED (pending) — owner001 offered to agent 002 (waiting for agent response)
('e0000000-0081-0000-0000-000000000002',
 '550e8400-e29b-41d4-a716-446655440401',
 '550e8400-e29b-41d4-a716-446655440102',
 'OWNER_INVITATION',
 '{"message": "We have an industrial property that needs professional marketing.", "offeredCommission": "3.0%"}',
 NULL, 'a7700000-0000-0000-0000-000000000001',
 'SUBMITTED', NULL,
 NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days', false),

-- 3. REJECTED — owner001 offered to agent 003 (agent rejected)
('e0000000-0081-0000-0000-000000000003',
 '550e8400-e29b-41d4-a716-446655440401',
 '550e8400-e29b-41d4-a716-446655440103',
 'OWNER_INVITATION',
 '{"message": "Interested in listing our industrial warehouse property.", "offeredCommission": "2.0%"}',
 NULL, 'a7100000-0000-0000-0000-000000000001',
 'REJECTED', NULL,
 NOW() - INTERVAL '45 days', NOW() - INTERVAL '40 days', false),

-- 4. CANCELLED — owner001 hired then cancelled with agent 007
('e0000000-0081-0000-0000-000000000004',
 '550e8400-e29b-41d4-a716-446655440401',
 '550e8400-e29b-41d4-a716-446655440107',
 'OWNER_INVITATION',
 '{"message": "Contract for industrial property sale.", "offeredCommission": "3.5%"}',
 NULL, 'a7700000-0000-0000-0000-000000000001',
 'CANCELLED', 'Changed strategy and decided to use different agent.',
 NOW() - INTERVAL '55 days', NOW() - INTERVAL '20 days', false),

-- 5. FINISHED — owner001 hired agent 008 (contract completed)
('e0000000-0081-0000-0000-000000000005',
 '550e8400-e29b-41d4-a716-446655440401',
 '550e8400-e29b-41d4-a716-446655440108',
 'OWNER_INVITATION',
 '{"message": "Professional representation needed for warehouse property listing.", "offeredCommission": "2.8%"}',
 NULL, 'a7100000-0000-0000-0000-000000000001',
 'FINISHED', NULL,
 NOW() - INTERVAL '120 days', NOW() - INTERVAL '30 days', false),

-- ===== AGENTS INITIATE (AGENT_PROPOSAL) =====

-- 6. SUBMITTED (pending) — agent 004 proposed (waiting for owner response)
('e0000000-0081-0000-0000-000000000006',
 '550e8400-e29b-41d4-a716-446655440104',
 '550e8400-e29b-41d4-a716-446655440401',
 'AGENT_PROPOSAL',
 '{"message": "I have extensive experience with industrial properties. Happy to discuss terms.", "proposedCommission": "2.5%"}',
 NULL, 'a7100000-0000-0000-0000-000000000001',
 'SUBMITTED', NULL,
 NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', false),

-- 7. ACCEPTED — agent 005 proposal accepted by owner001
('e0000000-0081-0000-0000-000000000007',
 '550e8400-e29b-41d4-a716-446655440105',
 '550e8400-e29b-41d4-a716-446655440401',
 'AGENT_PROPOSAL',
 '{"message": "Specializing in industrial property transactions and marketing. I believe I can provide excellent service.", "proposedCommission": "3.0%"}',
 NULL, 'a7700000-0000-0000-0000-000000000001',
 'ACCEPTED', NULL,
 NOW() - INTERVAL '35 days', NOW() - INTERVAL '28 days', false),

-- 8. REJECTED — agent 006 proposal rejected by owner001
('e0000000-0081-0000-0000-000000000008',
 '550e8400-e29b-41d4-a716-446655440106',
 '550e8400-e29b-41d4-a716-446655440401',
 'AGENT_PROPOSAL',
 '{"message": "Licensed agent with strong track record in commercial real estate.", "proposedCommission": "3.5%"}',
 NULL, 'a7100000-0000-0000-0000-000000000001',
 'REJECTED', NULL,
 NOW() - INTERVAL '50 days', NOW() - INTERVAL '45 days', false),

-- 9. CANCELLED — agent 009 proposal cancelled
('e0000000-0081-0000-0000-000000000009',
 '550e8400-e29b-41d4-a716-446655440109',
 '550e8400-e29b-41d4-a716-446655440401',
 'AGENT_PROPOSAL',
 '{"message": "Professional with years of experience serving industrial property owners.", "proposedCommission": "2.2%"}',
 NULL, 'a7700000-0000-0000-0000-000000000001',
 'CANCELLED', 'Agent withdrew proposal due to business change.',
 NOW() - INTERVAL '60 days', NOW() - INTERVAL '15 days', false),

-- 10. FINISHED — agent 010 proposal accepted and contract finished
('e0000000-0081-0000-0000-000000000010',
 '550e8400-e29b-41d4-a716-446655440110',
 '550e8400-e29b-41d4-a716-446655440401',
 'AGENT_PROPOSAL',
 '{"message": "Expert in industrial property sales with extensive buyer network.", "proposedCommission": "3.2%"}',
 NULL, 'a7100000-0000-0000-0000-000000000001',
 'FINISHED', NULL,
 NOW() - INTERVAL '100 days', NOW() - INTERVAL '35 days', false);
