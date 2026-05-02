-- Seed additional hired-agent engagements for owner049@realvista.com
-- Owner user_id: 550e8400-e29b-41d4-a716-446655440609
-- Target owner listings from V107:
--   listing b8131cee-fce4-4695-be5e-af746cb75372 (property 3d0324b9-2d85-4b70-a146-b02d147022b6)
--   listing c6fcb14a-275d-4d1c-8fd6-97e41eaf46bd (property d3817ba2-f0b3-4c20-954b-3ad1e721f8e4)

INSERT INTO engagements (
    engagement_id,
    initiator_id,
    receiver_id,
    engagement_type,
    content,
    listing_id,
    property_id,
    status,
    cancellation_reason,
    created_at,
    updated_at,
    deleted
)
VALUES
-- owner invites agent011 for studio listing (active)
('e0000000-0130-0000-0000-000000000001',
 '550e8400-e29b-41d4-a716-446655440609',
 '550e8400-e29b-41d4-a716-446655440111',
 'OWNER_INVITATION',
 '{"message":"Need support to qualify buyers for studio listing.","offeredCommission":"2.5%"}',
 'b8131cee-fce4-4695-be5e-af746cb75372',
 '3d0324b9-2d85-4b70-a146-b02d147022b6',
 'ACCEPTED',
 NULL,
 NOW() - INTERVAL '14 days',
 NOW() - INTERVAL '10 days',
 false),

-- agent012 proposes to owner049 for studio listing (active)
('e0000000-0130-0000-0000-000000000002',
 '550e8400-e29b-41d4-a716-446655440112',
 '550e8400-e29b-41d4-a716-446655440609',
 'AGENT_PROPOSAL',
 '{"message":"I have active buyer network for central district studio units.","proposedCommission":"2.8%"}',
 'b8131cee-fce4-4695-be5e-af746cb75372',
 '3d0324b9-2d85-4b70-a146-b02d147022b6',
 'ACCEPTED',
 NULL,
 NOW() - INTERVAL '9 days',
 NOW() - INTERVAL '7 days',
 false),

-- owner invites agent013 for logistics listing (active)
('e0000000-0130-0000-0000-000000000003',
 '550e8400-e29b-41d4-a716-446655440609',
 '550e8400-e29b-41d4-a716-446655440113',
 'OWNER_INVITATION',
 '{"message":"Looking for agent experienced in logistics and warehouse assets.","offeredCommission":"3.0%"}',
 'c6fcb14a-275d-4d1c-8fd6-97e41eaf46bd',
 'd3817ba2-f0b3-4c20-954b-3ad1e721f8e4',
 'ACCEPTED',
 NULL,
 NOW() - INTERVAL '20 days',
 NOW() - INTERVAL '16 days',
 false),

-- agent114 finished a previous collaboration with owner049 (historical)
('e0000000-0130-0000-0000-000000000004',
 '550e8400-e29b-41d4-a716-446655440114',
 '550e8400-e29b-41d4-a716-446655440609',
 'AGENT_PROPOSAL',
 '{"message":"Completed prior deal cycle for similar logistics property.","proposedCommission":"2.6%"}',
 'c6fcb14a-275d-4d1c-8fd6-97e41eaf46bd',
 'd3817ba2-f0b3-4c20-954b-3ad1e721f8e4',
 'FINISHED',
 NULL,
 NOW() - INTERVAL '75 days',
 NOW() - INTERVAL '30 days',
 false);
