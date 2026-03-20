-- V44__Add_finished_status_cancellation_reason_engagement_review.sql
-- Adds FINISHED status to engagements, cancellation_reason column,
-- and engagement_id column to agent_reviews for linking reviews to engagements.
-- Compatible with both PostgreSQL and H2 databases.

-- ============================================================================
-- 1. UPDATE ENGAGEMENTS: Add FINISHED to status CHECK constraint
-- ============================================================================

ALTER TABLE engagements DROP CONSTRAINT chk_engagement_status;
ALTER TABLE engagements ADD CONSTRAINT chk_engagement_status
    CHECK (status IN ('SUBMITTED', 'ACCEPTED', 'REJECTED', 'CANCELLED', 'FINISHED'));

-- ============================================================================
-- 2. ADD CANCELLATION_REASON COLUMN TO ENGAGEMENTS
-- ============================================================================

ALTER TABLE engagements ADD COLUMN cancellation_reason TEXT;

-- ============================================================================
-- 3. ADD ENGAGEMENT_ID TO AGENT_REVIEWS (links review to specific engagement)
-- ============================================================================

ALTER TABLE agent_reviews ADD COLUMN engagement_id UUID;

-- Unique constraint: one review per engagement
ALTER TABLE agent_reviews ADD CONSTRAINT uq_agent_review_engagement UNIQUE (engagement_id);

-- Index for fast lookup
CREATE INDEX idx_agent_review_engagement ON agent_reviews (engagement_id);
