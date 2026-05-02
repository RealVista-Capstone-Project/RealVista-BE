-- V128__Update_engagement_type_constraint_add_agent_created_property_link.sql
-- Updates engagements.engagement_type CHECK constraint to include
-- AGENT_CREATED_PROPERTY_LINK.
-- Compatible with both PostgreSQL and H2 databases.

ALTER TABLE engagements
    DROP CONSTRAINT IF EXISTS chk_engagement_type;

ALTER TABLE engagements
    ADD CONSTRAINT chk_engagement_type
        CHECK (engagement_type IN (
            'AGENT_PROPOSAL',
            'TENANT_APPLICATION',
            'OWNER_INVITATION',
            'AGENT_CREATED_PROPERTY_LINK'
        ));
