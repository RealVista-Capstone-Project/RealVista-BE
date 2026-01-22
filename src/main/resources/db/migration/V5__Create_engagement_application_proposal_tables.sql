-- V5__Create_engagement_application_proposal_tables.sql
-- Creates engagements, tenant applications, agent proposals tables
-- Compatible with both PostgreSQL and H2 databases

-- ============================================================================
-- ENGAGEMENTS TABLE (All interactions: agent proposal, tenant app, owner invite)
-- ============================================================================

CREATE TABLE engagements
(
    engagement_id   UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    initiator_id    UUID        NOT NULL,
    receiver_id     UUID        NOT NULL,
    engagement_type VARCHAR(30) NOT NULL,
    content         JSON,
    listing_id      UUID,
    property_id     UUID,
    status          VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN DEFAULT FALSE,

    CONSTRAINT chk_engagement_type CHECK (engagement_type IN ('AGENT_PROPOSAL', 'TENANT_APPLICATION', 'OWNER_INVITATION')),
    CONSTRAINT chk_engagement_status CHECK (status IN ('SUBMITTED', 'ACCEPTED', 'REJECTED', 'CANCELLED'))
);

CREATE INDEX idx_engagement_initiator ON engagements (initiator_id);
CREATE INDEX idx_engagement_receiver ON engagements (receiver_id);
CREATE INDEX idx_engagement_type ON engagements (engagement_type);
CREATE INDEX idx_engagement_listing ON engagements (listing_id);
CREATE INDEX idx_engagement_property ON engagements (property_id);
CREATE INDEX idx_engagement_status ON engagements (status);

-- ============================================================================
-- TENANT APPLICATIONS TABLE (Tenant/buyer apply for listing)
-- ============================================================================

CREATE TABLE tenant_applications
(
    tenant_application_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id               UUID           NOT NULL,
    listing_id            UUID           NOT NULL,
    title                 VARCHAR(100)   NOT NULL,
    monthly_income        NUMERIC(15, 2),
    move_in_date          DATE,
    lease_term_months     INTEGER,
    status                VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    note                  TEXT,
    created_at            TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP      NOT NULL DEFAULT NOW(),
    deleted               BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (listing_id) REFERENCES listings (listing_id) ON DELETE CASCADE,
    CONSTRAINT chk_tenant_app_status CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED'))
);

CREATE INDEX idx_tenant_app_user ON tenant_applications (user_id);
CREATE INDEX idx_tenant_app_listing ON tenant_applications (listing_id);
CREATE INDEX idx_tenant_app_status ON tenant_applications (status);

-- ============================================================================
-- AGENT PROPOSALS TABLE (Agent propose to owner for selling/renting property)
-- ============================================================================

CREATE TABLE agent_proposals
(
    agent_proposal_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id           UUID          NOT NULL,
    property_id       UUID          NOT NULL,
    title             VARCHAR(100)  NOT NULL,
    commission_rate   NUMERIC(5, 2),
    experience_years  INTEGER,
    status            VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    pitch_content     TEXT,
    created_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
    deleted           BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (property_id) REFERENCES properties (property_id) ON DELETE CASCADE,
    CONSTRAINT chk_agent_proposal_status CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED'))
);

CREATE INDEX idx_agent_proposal_user ON agent_proposals (user_id);
CREATE INDEX idx_agent_proposal_property ON agent_proposals (property_id);
CREATE INDEX idx_agent_proposal_status ON agent_proposals (status);
