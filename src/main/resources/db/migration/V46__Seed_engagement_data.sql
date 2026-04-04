-- V43: Seed engagement data for hired-agent feature and general engagement testing
-- Creates realistic engagement records linking owners to agents via properties
--
-- UUID prefix: 550e8400-e29b-41d4-a716-4466554
-- Owners: 440401-440440 (V11), 440601-440692 (V12)
-- Agents: 440101-440140 (V11), 440501-440600 (V12)

-- ============================================================
-- SECTION 1: AGENT_PROPOSAL engagements (agent=initiator, owner=receiver)
-- Status: ACCEPTED — these represent agents hired through proposals
-- ============================================================

INSERT INTO engagements (engagement_id, initiator_id, receiver_id, engagement_type, content, listing_id, property_id, status, created_at, updated_at, deleted) VALUES

-- APARTMENT properties: agents proposed to owners, owners accepted
('e0000000-0001-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440101', '550e8400-e29b-41d4-a716-446655440601', 'AGENT_PROPOSAL', '{"message": "I specialize in apartment sales in District 1. I can help you get the best price for your property.", "proposedCommission": "2.5%"}', NULL, 'a1100000-0000-0000-0000-000000000001', 'ACCEPTED', NOW() - INTERVAL '45 days', NOW() - INTERVAL '40 days', false),

('e0000000-0001-0000-0000-000000000002', '550e8400-e29b-41d4-a716-446655440102', '550e8400-e29b-41d4-a716-446655440602', 'AGENT_PROPOSAL', '{"message": "With 8 years of experience in luxury apartments, I am confident I can find the right buyer.", "proposedCommission": "3.0%"}', NULL, 'a1100000-0000-0000-0000-000000000002', 'ACCEPTED', NOW() - INTERVAL '42 days', NOW() - INTERVAL '38 days', false),

('e0000000-0001-0000-0000-000000000003', '550e8400-e29b-41d4-a716-446655440103', '550e8400-e29b-41d4-a716-446655440603', 'AGENT_PROPOSAL', '{"message": "I have extensive network of buyers looking for apartments in this area.", "proposedCommission": "2.0%"}', NULL, 'a1100000-0000-0000-0000-000000000003', 'ACCEPTED', NOW() - INTERVAL '38 days', NOW() - INTERVAL '35 days', false),

('e0000000-0001-0000-0000-000000000004', '550e8400-e29b-41d4-a716-446655440104', '550e8400-e29b-41d4-a716-446655440604', 'AGENT_PROPOSAL', '{"message": "I can manage showings and negotiations for your apartment listing.", "proposedCommission": "2.5%"}', NULL, 'a1100000-0000-0000-0000-000000000004', 'ACCEPTED', NOW() - INTERVAL '35 days', NOW() - INTERVAL '30 days', false),

('e0000000-0001-0000-0000-000000000005', '550e8400-e29b-41d4-a716-446655440105', '550e8400-e29b-41d4-a716-446655440605', 'AGENT_PROPOSAL', '{"message": "My track record shows 95% of listings sold within 30 days.", "proposedCommission": "3.0%"}', NULL, 'a1100000-0000-0000-0000-000000000005', 'ACCEPTED', NOW() - INTERVAL '30 days', NOW() - INTERVAL '25 days', false),

-- HOUSE properties: agents proposed to owners, owners accepted
('e0000000-0001-0000-0000-000000000006', '550e8400-e29b-41d4-a716-446655440117', '550e8400-e29b-41d4-a716-446655440623', 'AGENT_PROPOSAL', '{"message": "I have deep experience with house sales in this neighborhood.", "proposedCommission": "2.5%"}', NULL, 'a2100000-0000-0000-0000-000000000001', 'ACCEPTED', NOW() - INTERVAL '50 days', NOW() - INTERVAL '45 days', false),

('e0000000-0001-0000-0000-000000000007', '550e8400-e29b-41d4-a716-446655440118', '550e8400-e29b-41d4-a716-446655440624', 'AGENT_PROPOSAL', '{"message": "I recently sold 3 similar houses in this district at above asking price.", "proposedCommission": "2.8%"}', NULL, 'a2100000-0000-0000-0000-000000000002', 'ACCEPTED', NOW() - INTERVAL '48 days', NOW() - INTERVAL '43 days', false),

('e0000000-0001-0000-0000-000000000008', '550e8400-e29b-41d4-a716-446655440119', '550e8400-e29b-41d4-a716-446655440625', 'AGENT_PROPOSAL', '{"message": "Let me handle the marketing and buyer screening for your house.", "proposedCommission": "2.5%"}', NULL, 'a2100000-0000-0000-0000-000000000003', 'ACCEPTED', NOW() - INTERVAL '44 days', NOW() - INTERVAL '40 days', false),

('e0000000-0001-0000-0000-000000000009', '550e8400-e29b-41d4-a716-446655440120', '550e8400-e29b-41d4-a716-446655440626', 'AGENT_PROPOSAL', '{"message": "Experienced in both residential sales and rentals.", "proposedCommission": "3.0%"}', NULL, 'a2100000-0000-0000-0000-000000000004', 'ACCEPTED', NOW() - INTERVAL '40 days', NOW() - INTERVAL '36 days', false),

('e0000000-0001-0000-0000-000000000010', '550e8400-e29b-41d4-a716-446655440121', '550e8400-e29b-41d4-a716-446655440627', 'AGENT_PROPOSAL', '{"message": "I offer comprehensive property management and sales service.", "proposedCommission": "2.0%"}', NULL, 'a2100000-0000-0000-0000-000000000005', 'ACCEPTED', NOW() - INTERVAL '36 days', NOW() - INTERVAL '32 days', false),

-- TOWNHOUSE properties
('e0000000-0001-0000-0000-000000000011', '550e8400-e29b-41d4-a716-446655440134', '550e8400-e29b-41d4-a716-446655440645', 'AGENT_PROPOSAL', '{"message": "Townhouse specialist with excellent market knowledge.", "proposedCommission": "2.5%"}', NULL, 'a3100000-0000-0000-0000-000000000001', 'ACCEPTED', NOW() - INTERVAL '55 days', NOW() - INTERVAL '50 days', false),

('e0000000-0001-0000-0000-000000000012', '550e8400-e29b-41d4-a716-446655440135', '550e8400-e29b-41d4-a716-446655440646', 'AGENT_PROPOSAL', '{"message": "I will create professional marketing materials for your townhouse.", "proposedCommission": "2.8%"}', NULL, 'a3100000-0000-0000-0000-000000000002', 'ACCEPTED', NOW() - INTERVAL '52 days', NOW() - INTERVAL '48 days', false),

('e0000000-0001-0000-0000-000000000013', '550e8400-e29b-41d4-a716-446655440136', '550e8400-e29b-41d4-a716-446655440647', 'AGENT_PROPOSAL', '{"message": "Strong negotiation skills and local market expertise.", "proposedCommission": "3.0%"}', NULL, 'a3100000-0000-0000-0000-000000000003', 'ACCEPTED', NOW() - INTERVAL '47 days', NOW() - INTERVAL '43 days', false),

-- VILLA properties
('e0000000-0001-0000-0000-000000000014', '550e8400-e29b-41d4-a716-446655440543', '550e8400-e29b-41d4-a716-446655440685', 'AGENT_PROPOSAL', '{"message": "I focus on luxury villa sales with high-net-worth clientele.", "proposedCommission": "3.5%"}', NULL, 'a6100000-0000-0000-0000-000000000001', 'ACCEPTED', NOW() - INTERVAL '60 days', NOW() - INTERVAL '55 days', false),

('e0000000-0001-0000-0000-000000000015', '550e8400-e29b-41d4-a716-446655440544', '550e8400-e29b-41d4-a716-446655440686', 'AGENT_PROPOSAL', '{"message": "My international network helps attract premium buyers for villas.", "proposedCommission": "3.0%"}', NULL, 'a6100000-0000-0000-0000-000000000002', 'ACCEPTED', NOW() - INTERVAL '58 days', NOW() - INTERVAL '53 days', false),

('e0000000-0001-0000-0000-000000000016', '550e8400-e29b-41d4-a716-446655440545', '550e8400-e29b-41d4-a716-446655440687', 'AGENT_PROPOSAL', '{"message": "Villa specialist with over 10 years in luxury real estate.", "proposedCommission": "3.0%"}', NULL, 'a6100000-0000-0000-0000-000000000003', 'ACCEPTED', NOW() - INTERVAL '55 days', NOW() - INTERVAL '50 days', false),

-- COMMERCIAL: OFFICE properties
('e0000000-0001-0000-0000-000000000017', '550e8400-e29b-41d4-a716-446655440559', '550e8400-e29b-41d4-a716-446655440601', 'AGENT_PROPOSAL', '{"message": "I specialize in commercial office leasing and sales.", "proposedCommission": "2.0%"}', NULL, 'c1100000-0000-0000-0000-000000000001', 'ACCEPTED', NOW() - INTERVAL '40 days', NOW() - INTERVAL '35 days', false),

('e0000000-0001-0000-0000-000000000018', '550e8400-e29b-41d4-a716-446655440560', '550e8400-e29b-41d4-a716-446655440602', 'AGENT_PROPOSAL', '{"message": "Expert in Grade A office spaces with corporate tenant connections.", "proposedCommission": "2.5%"}', NULL, 'c1100000-0000-0000-0000-000000000002', 'ACCEPTED', NOW() - INTERVAL '38 days', NOW() - INTERVAL '33 days', false),

-- COMMERCIAL: SHOPHOUSE properties
('e0000000-0001-0000-0000-000000000019', '550e8400-e29b-41d4-a716-446655440574', '550e8400-e29b-41d4-a716-446655440621', 'AGENT_PROPOSAL', '{"message": "Shophouse specialist in the old quarter area.", "proposedCommission": "2.5%"}', NULL, 'c2100000-0000-0000-0000-000000000001', 'ACCEPTED', NOW() - INTERVAL '35 days', NOW() - INTERVAL '30 days', false),

('e0000000-0001-0000-0000-000000000020', '550e8400-e29b-41d4-a716-446655440575', '550e8400-e29b-41d4-a716-446655440622', 'AGENT_PROPOSAL', '{"message": "I help investors find the best shophouse deals.", "proposedCommission": "2.8%"}', NULL, 'c2100000-0000-0000-0000-000000000002', 'ACCEPTED', NOW() - INTERVAL '32 days', NOW() - INTERVAL '28 days', false);


-- ============================================================
-- SECTION 2: OWNER_INVITATION engagements (owner=initiator, agent=receiver)
-- Status: ACCEPTED — these represent agents invited by owners
-- ============================================================

INSERT INTO engagements (engagement_id, initiator_id, receiver_id, engagement_type, content, listing_id, property_id, status, created_at, updated_at, deleted) VALUES

-- APARTMENT: owners invited agents
('e0000000-0002-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440606', '550e8400-e29b-41d4-a716-446655440106', 'OWNER_INVITATION', '{"message": "I saw your profile and would like you to represent my apartment listing.", "offeredCommission": "2.5%"}', NULL, 'a1100000-0000-0000-0000-000000000006', 'ACCEPTED', NOW() - INTERVAL '43 days', NOW() - INTERVAL '39 days', false),

('e0000000-0002-0000-0000-000000000002', '550e8400-e29b-41d4-a716-446655440607', '550e8400-e29b-41d4-a716-446655440107', 'OWNER_INVITATION', '{"message": "Your reviews are excellent. Please help me sell my apartment.", "offeredCommission": "3.0%"}', NULL, 'a1100000-0000-0000-0000-000000000007', 'ACCEPTED', NOW() - INTERVAL '41 days', NOW() - INTERVAL '37 days', false),

('e0000000-0002-0000-0000-000000000003', '550e8400-e29b-41d4-a716-446655440608', '550e8400-e29b-41d4-a716-446655440108', 'OWNER_INVITATION', '{"message": "A friend recommended you. Can you handle my apartment sale?", "offeredCommission": "2.5%"}', NULL, 'a1100000-0000-0000-0000-000000000008', 'ACCEPTED', NOW() - INTERVAL '39 days', NOW() - INTERVAL '35 days', false),

('e0000000-0002-0000-0000-000000000004', '550e8400-e29b-41d4-a716-446655440609', '550e8400-e29b-41d4-a716-446655440109', 'OWNER_INVITATION', '{"message": "Looking for an experienced agent for my premium apartment.", "offeredCommission": "2.8%"}', NULL, 'a1100000-0000-0000-0000-000000000009', 'ACCEPTED', NOW() - INTERVAL '37 days', NOW() - INTERVAL '33 days', false),

('e0000000-0002-0000-0000-000000000005', '550e8400-e29b-41d4-a716-446655440610', '550e8400-e29b-41d4-a716-446655440110', 'OWNER_INVITATION', '{"message": "I need an agent who knows the apartment market well.", "offeredCommission": "2.5%"}', NULL, 'a1100000-0000-0000-0000-000000000010', 'ACCEPTED', NOW() - INTERVAL '34 days', NOW() - INTERVAL '30 days', false),

-- HOUSE: owners invited agents
('e0000000-0002-0000-0000-000000000006', '550e8400-e29b-41d4-a716-446655440628', '550e8400-e29b-41d4-a716-446655440122', 'OWNER_INVITATION', '{"message": "Please help me find a buyer for my house.", "offeredCommission": "2.5%"}', NULL, 'a2100000-0000-0000-0000-000000000006', 'ACCEPTED', NOW() - INTERVAL '46 days', NOW() - INTERVAL '42 days', false),

('e0000000-0002-0000-0000-000000000007', '550e8400-e29b-41d4-a716-446655440629', '550e8400-e29b-41d4-a716-446655440123', 'OWNER_INVITATION', '{"message": "Your experience in this area matches my needs perfectly.", "offeredCommission": "3.0%"}', NULL, 'a2100000-0000-0000-0000-000000000007', 'ACCEPTED', NOW() - INTERVAL '44 days', NOW() - INTERVAL '40 days', false),

('e0000000-0002-0000-0000-000000000008', '550e8400-e29b-41d4-a716-446655440630', '550e8400-e29b-41d4-a716-446655440124', 'OWNER_INVITATION', '{"message": "Interested in your services for selling my house.", "offeredCommission": "2.5%"}', NULL, 'a2100000-0000-0000-0000-000000000008', 'ACCEPTED', NOW() - INTERVAL '42 days', NOW() - INTERVAL '38 days', false),

('e0000000-0002-0000-0000-000000000009', '550e8400-e29b-41d4-a716-446655440631', '550e8400-e29b-41d4-a716-446655440125', 'OWNER_INVITATION', '{"message": "Need a reliable agent for my property.", "offeredCommission": "2.8%"}', NULL, 'a2100000-0000-0000-0000-000000000009', 'ACCEPTED', NOW() - INTERVAL '39 days', NOW() - INTERVAL '35 days', false),

-- PENTHOUSE: owners invited agents
('e0000000-0002-0000-0000-000000000010', '550e8400-e29b-41d4-a716-446655440667', '550e8400-e29b-41d4-a716-446655440510', 'OWNER_INVITATION', '{"message": "I need a luxury property specialist for my penthouse.", "offeredCommission": "3.5%"}', NULL, 'a4100000-0000-0000-0000-000000000001', 'ACCEPTED', NOW() - INTERVAL '50 days', NOW() - INTERVAL '45 days', false),

('e0000000-0002-0000-0000-000000000011', '550e8400-e29b-41d4-a716-446655440668', '550e8400-e29b-41d4-a716-446655440511', 'OWNER_INVITATION', '{"message": "Your luxury portfolio is impressive. Please manage my penthouse sale.", "offeredCommission": "3.0%"}', NULL, 'a4100000-0000-0000-0000-000000000002', 'ACCEPTED', NOW() - INTERVAL '48 days', NOW() - INTERVAL '44 days', false),

('e0000000-0002-0000-0000-000000000012', '550e8400-e29b-41d4-a716-446655440669', '550e8400-e29b-41d4-a716-446655440512', 'OWNER_INVITATION', '{"message": "Looking for an agent with high-end property experience.", "offeredCommission": "3.5%"}', NULL, 'a4100000-0000-0000-0000-000000000003', 'ACCEPTED', NOW() - INTERVAL '45 days', NOW() - INTERVAL '41 days', false),

-- COMMERCIAL: RETAIL — owners invited agents
('e0000000-0002-0000-0000-000000000013', '550e8400-e29b-41d4-a716-446655440641', '550e8400-e29b-41d4-a716-446655440589', 'OWNER_INVITATION', '{"message": "I need help leasing my retail space in a prime location.", "offeredCommission": "2.0%"}', NULL, 'c3100000-0000-0000-0000-000000000001', 'ACCEPTED', NOW() - INTERVAL '33 days', NOW() - INTERVAL '29 days', false),

('e0000000-0002-0000-0000-000000000014', '550e8400-e29b-41d4-a716-446655440642', '550e8400-e29b-41d4-a716-446655440590', 'OWNER_INVITATION', '{"message": "Your commercial property expertise is what I need.", "offeredCommission": "2.5%"}', NULL, 'c3100000-0000-0000-0000-000000000002', 'ACCEPTED', NOW() - INTERVAL '31 days', NOW() - INTERVAL '27 days', false),

-- COMMERCIAL: HOTEL — owners invited agents
('e0000000-0002-0000-0000-000000000015', '550e8400-e29b-41d4-a716-446655440601', '550e8400-e29b-41d4-a716-446655440134', 'OWNER_INVITATION', '{"message": "I am looking to sell my hotel property and need expert representation.", "offeredCommission": "2.0%"}', NULL, 'c6100000-0000-0000-0000-000000000001', 'ACCEPTED', NOW() - INTERVAL '28 days', NOW() - INTERVAL '24 days', false),

-- INDUSTRIAL: WAREHOUSE — owners invited agents
('e0000000-0002-0000-0000-000000000016', '550e8400-e29b-41d4-a716-446655440401', '550e8400-e29b-41d4-a716-446655440509', 'OWNER_INVITATION', '{"message": "Need an industrial property agent for my warehouse.", "offeredCommission": "2.0%"}', NULL, 'a7100000-0000-0000-0000-000000000001', 'ACCEPTED', NOW() - INTERVAL '25 days', NOW() - INTERVAL '21 days', false),

('e0000000-0002-0000-0000-000000000017', '550e8400-e29b-41d4-a716-446655440402', '550e8400-e29b-41d4-a716-446655440510', 'OWNER_INVITATION', '{"message": "Looking for an agent experienced in warehouse properties.", "offeredCommission": "2.5%"}', NULL, 'a7100000-0000-0000-0000-000000000002', 'ACCEPTED', NOW() - INTERVAL '22 days', NOW() - INTERVAL '18 days', false),

-- LAND: LAND_RESIDENTIAL — owners invited agents
('e0000000-0002-0000-0000-000000000018', '550e8400-e29b-41d4-a716-446655440649', '550e8400-e29b-41d4-a716-446655440575', 'OWNER_INVITATION', '{"message": "I want to sell my land plot. Can you help with the marketing?", "offeredCommission": "2.0%"}', NULL, 'a7500000-0000-0000-0000-000000000001', 'ACCEPTED', NOW() - INTERVAL '20 days', NOW() - INTERVAL '16 days', false),

('e0000000-0002-0000-0000-000000000019', '550e8400-e29b-41d4-a716-446655440650', '550e8400-e29b-41d4-a716-446655440576', 'OWNER_INVITATION', '{"message": "Need an agent for land sale with development potential.", "offeredCommission": "2.5%"}', NULL, 'a7500000-0000-0000-0000-000000000002', 'ACCEPTED', NOW() - INTERVAL '18 days', NOW() - INTERVAL '14 days', false),

-- LAND: LAND_COMMERCIAL — owners invited agents
('e0000000-0002-0000-0000-000000000020', '550e8400-e29b-41d4-a716-446655440671', '550e8400-e29b-41d4-a716-446655440591', 'OWNER_INVITATION', '{"message": "Commercial land for sale — need experienced agent.", "offeredCommission": "2.0%"}', NULL, 'a7600000-0000-0000-0000-000000000001', 'ACCEPTED', NOW() - INTERVAL '15 days', NOW() - INTERVAL '11 days', false);


-- ============================================================
-- SECTION 3: AGENT_PROPOSAL engagements with OTHER statuses (for realism)
-- These are NOT accepted — they show pending/rejected/cancelled proposals
-- ============================================================

INSERT INTO engagements (engagement_id, initiator_id, receiver_id, engagement_type, content, listing_id, property_id, status, created_at, updated_at, deleted) VALUES

-- SUBMITTED proposals (pending review by owner)
('e0000000-0003-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440106', '550e8400-e29b-41d4-a716-446655440611', 'AGENT_PROPOSAL', '{"message": "I would love to help sell your apartment. I have 5 years of experience.", "proposedCommission": "2.5%"}', NULL, 'a1100000-0000-0000-0000-000000000011', 'SUBMITTED', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days', false),

('e0000000-0003-0000-0000-000000000002', '550e8400-e29b-41d4-a716-446655440107', '550e8400-e29b-41d4-a716-446655440612', 'AGENT_PROPOSAL', '{"message": "Looking to represent your property. Competitive commission rate.", "proposedCommission": "2.0%"}', NULL, 'a1100000-0000-0000-0000-000000000012', 'SUBMITTED', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days', false),

('e0000000-0003-0000-0000-000000000003', '550e8400-e29b-41d4-a716-446655440126', '550e8400-e29b-41d4-a716-446655440632', 'AGENT_PROPOSAL', '{"message": "Experienced house agent available for your property.", "proposedCommission": "2.5%"}', NULL, 'a2100000-0000-0000-0000-000000000010', 'SUBMITTED', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', false),

('e0000000-0003-0000-0000-000000000004', '550e8400-e29b-41d4-a716-446655440546', '550e8400-e29b-41d4-a716-446655440688', 'AGENT_PROPOSAL', '{"message": "Villa specialist seeking to help you find the perfect buyer.", "proposedCommission": "3.5%"}', NULL, 'a6100000-0000-0000-0000-000000000004', 'SUBMITTED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', false),

('e0000000-0003-0000-0000-000000000005', '550e8400-e29b-41d4-a716-446655440561', '550e8400-e29b-41d4-a716-446655440603', 'AGENT_PROPOSAL', '{"message": "Office space specialist ready to manage your commercial property.", "proposedCommission": "2.0%"}', NULL, 'c1100000-0000-0000-0000-000000000003', 'SUBMITTED', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', false),

-- REJECTED proposals
('e0000000-0003-0000-0000-000000000006', '550e8400-e29b-41d4-a716-446655440108', '550e8400-e29b-41d4-a716-446655440613', 'AGENT_PROPOSAL', '{"message": "I can handle your apartment sale efficiently.", "proposedCommission": "4.0%"}', NULL, 'a1100000-0000-0000-0000-000000000013', 'REJECTED', NOW() - INTERVAL '30 days', NOW() - INTERVAL '25 days', false),

('e0000000-0003-0000-0000-000000000007', '550e8400-e29b-41d4-a716-446655440127', '550e8400-e29b-41d4-a716-446655440633', 'AGENT_PROPOSAL', '{"message": "Eager to help sell your house.", "proposedCommission": "3.5%"}', NULL, 'a2100000-0000-0000-0000-000000000011', 'REJECTED', NOW() - INTERVAL '28 days', NOW() - INTERVAL '23 days', false),

('e0000000-0003-0000-0000-000000000008', '550e8400-e29b-41d4-a716-446655440547', '550e8400-e29b-41d4-a716-446655440689', 'AGENT_PROPOSAL', '{"message": "I specialize in luxury villas.", "proposedCommission": "4.0%"}', NULL, 'a6100000-0000-0000-0000-000000000005', 'REJECTED', NOW() - INTERVAL '25 days', NOW() - INTERVAL '20 days', false),

-- CANCELLED proposals (agent withdrew)
('e0000000-0003-0000-0000-000000000009', '550e8400-e29b-41d4-a716-446655440109', '550e8400-e29b-41d4-a716-446655440614', 'AGENT_PROPOSAL', '{"message": "I would like to represent your property.", "proposedCommission": "2.5%"}', NULL, 'a1100000-0000-0000-0000-000000000014', 'CANCELLED', NOW() - INTERVAL '20 days', NOW() - INTERVAL '15 days', false),

('e0000000-0003-0000-0000-000000000010', '550e8400-e29b-41d4-a716-446655440562', '550e8400-e29b-41d4-a716-446655440604', 'AGENT_PROPOSAL', '{"message": "Available for commercial office sales.", "proposedCommission": "2.0%"}', NULL, 'c1100000-0000-0000-0000-000000000004', 'CANCELLED', NOW() - INTERVAL '18 days', NOW() - INTERVAL '13 days', false);


-- ============================================================
-- SECTION 4: OWNER_INVITATION engagements with OTHER statuses
-- ============================================================

INSERT INTO engagements (engagement_id, initiator_id, receiver_id, engagement_type, content, listing_id, property_id, status, created_at, updated_at, deleted) VALUES

-- SUBMITTED invitations (pending agent response)
('e0000000-0004-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440611', '550e8400-e29b-41d4-a716-446655440111', 'OWNER_INVITATION', '{"message": "Would you be interested in selling my apartment?", "offeredCommission": "2.5%"}', NULL, 'a1100000-0000-0000-0000-000000000011', 'SUBMITTED', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', false),

('e0000000-0004-0000-0000-000000000002', '550e8400-e29b-41d4-a716-446655440632', '550e8400-e29b-41d4-a716-446655440126', 'OWNER_INVITATION', '{"message": "I noticed your experience in house sales. Would you like to help me?", "offeredCommission": "2.8%"}', NULL, 'a2100000-0000-0000-0000-000000000010', 'SUBMITTED', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', false),

('e0000000-0004-0000-0000-000000000003', '550e8400-e29b-41d4-a716-446655440670', '550e8400-e29b-41d4-a716-446655440513', 'OWNER_INVITATION', '{"message": "Seeking an agent for my penthouse in the premium building.", "offeredCommission": "3.0%"}', NULL, 'a4100000-0000-0000-0000-000000000004', 'SUBMITTED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', false),

-- REJECTED invitations (agent declined)
('e0000000-0004-0000-0000-000000000004', '550e8400-e29b-41d4-a716-446655440614', '550e8400-e29b-41d4-a716-446655440114', 'OWNER_INVITATION', '{"message": "Please consider representing my apartment.", "offeredCommission": "1.5%"}', NULL, 'a1100000-0000-0000-0000-000000000014', 'REJECTED', NOW() - INTERVAL '22 days', NOW() - INTERVAL '18 days', false),

('e0000000-0004-0000-0000-000000000005', '550e8400-e29b-41d4-a716-446655440634', '550e8400-e29b-41d4-a716-446655440128', 'OWNER_INVITATION', '{"message": "Would you be available to sell my house?", "offeredCommission": "1.5%"}', NULL, 'a2100000-0000-0000-0000-000000000012', 'REJECTED', NOW() - INTERVAL '20 days', NOW() - INTERVAL '16 days', false),

-- CANCELLED invitations (owner withdrew)
('e0000000-0004-0000-0000-000000000006', '550e8400-e29b-41d4-a716-446655440615', '550e8400-e29b-41d4-a716-446655440115', 'OWNER_INVITATION', '{"message": "I need an agent for my apartment listing.", "offeredCommission": "2.5%"}', NULL, 'a1100000-0000-0000-0000-000000000015', 'CANCELLED', NOW() - INTERVAL '15 days', NOW() - INTERVAL '10 days', false),

('e0000000-0004-0000-0000-000000000007', '550e8400-e29b-41d4-a716-446655440653', '550e8400-e29b-41d4-a716-446655440505', 'OWNER_INVITATION', '{"message": "Looking for representation for my townhouse sale.", "offeredCommission": "2.5%"}', NULL, 'a3100000-0000-0000-0000-000000000009', 'CANCELLED', NOW() - INTERVAL '12 days', NOW() - INTERVAL '8 days', false);


-- ============================================================
-- SECTION 5: Extra ACCEPTED engagements for owner 440601 (to test pagination)
-- Owner 440601 has multiple properties — let's give them many hired agents
-- ============================================================

INSERT INTO engagements (engagement_id, initiator_id, receiver_id, engagement_type, content, listing_id, property_id, status, created_at, updated_at, deleted) VALUES

-- Owner 440601 owns: a1100000-001, c1100000-001, c6100000-001, a6100000-009
-- Already has engagements for a1100000-001 and c1100000-001 and c6100000-001
-- Add more for a6100000-009 via AGENT_PROPOSAL
('e0000000-0005-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440551', '550e8400-e29b-41d4-a716-446655440601', 'AGENT_PROPOSAL', '{"message": "I can help sell your villa with premium marketing.", "proposedCommission": "3.0%"}', NULL, 'a6100000-0000-0000-0000-000000000009', 'ACCEPTED', NOW() - INTERVAL '33 days', NOW() - INTERVAL '29 days', false),

-- Also add OWNER_INVITATION for the same owner to different property
-- Owner 440602 also has properties: a1100000-002, c1100000-002, a6100000-010
('e0000000-0005-0000-0000-000000000002', '550e8400-e29b-41d4-a716-446655440602', '550e8400-e29b-41d4-a716-446655440556', 'OWNER_INVITATION', '{"message": "I need an agent for my villa property.", "offeredCommission": "2.5%"}', NULL, 'a6100000-0000-0000-0000-000000000010', 'ACCEPTED', NOW() - INTERVAL '30 days', NOW() - INTERVAL '26 days', false),

-- More for owner 440603: a1100000-003, c1100000-003, a6100000-011
('e0000000-0005-0000-0000-000000000003', '550e8400-e29b-41d4-a716-446655440553', '550e8400-e29b-41d4-a716-446655440603', 'AGENT_PROPOSAL', '{"message": "Expert in villa properties, ready to assist.", "proposedCommission": "3.0%"}', NULL, 'a6100000-0000-0000-0000-000000000011', 'ACCEPTED', NOW() - INTERVAL '27 days', NOW() - INTERVAL '23 days', false),

-- Owner 440604: a1100000-004, c1100000-004, a6100000-012
('e0000000-0005-0000-0000-000000000004', '550e8400-e29b-41d4-a716-446655440604', '550e8400-e29b-41d4-a716-446655440558', 'OWNER_INVITATION', '{"message": "Please represent my villa sale.", "offeredCommission": "3.0%"}', NULL, 'a6100000-0000-0000-0000-000000000012', 'ACCEPTED', NOW() - INTERVAL '24 days', NOW() - INTERVAL '20 days', false),

-- Owner 440605: a1100000-005, c1100000-005, a6100000-013
('e0000000-0005-0000-0000-000000000005', '550e8400-e29b-41d4-a716-446655440555', '550e8400-e29b-41d4-a716-446655440605', 'AGENT_PROPOSAL', '{"message": "I specialize in villa sales and rentals.", "proposedCommission": "2.5%"}', NULL, 'a6100000-0000-0000-0000-000000000013', 'ACCEPTED', NOW() - INTERVAL '21 days', NOW() - INTERVAL '17 days', false);
