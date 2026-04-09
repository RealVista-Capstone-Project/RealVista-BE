-- V75__Add_specialty_and_price_range_to_agent_proposals.sql
-- Add specialty and price_range columns to agent_proposals table

ALTER TABLE agent_proposals
    ADD COLUMN specialty UUID,
    ADD COLUMN price_range JSONB;

-- Add foreign key constraint for specialty (property_type_id)
ALTER TABLE agent_proposals
    ADD CONSTRAINT fk_agent_proposal_specialty
    FOREIGN KEY (specialty) REFERENCES property_types (property_type_id);
