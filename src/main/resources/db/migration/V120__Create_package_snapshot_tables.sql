-- V120__Create_package_snapshot_tables.sql
-- Snapshot tables for FeaturePackage and BoostPackage audit history
-- Records full state of a package BEFORE any update, deactivation, or deletion

-- ============================================================================
-- FEATURE PACKAGE SNAPSHOTS
-- ============================================================================

CREATE TABLE feature_package_snapshots
(
    snapshot_id         UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    feature_package_id  UUID           NOT NULL,
    code                VARCHAR(50)    NOT NULL,
    name                VARCHAR(100)   NOT NULL,
    description         TEXT,
    feature_type        VARCHAR(30)    NOT NULL,
    quota               INTEGER        NOT NULL,
    duration_days       INTEGER        NOT NULL,
    price               NUMERIC(12, 2) NOT NULL,
    is_active           BOOLEAN        NOT NULL,
    snapshot_reason     VARCHAR(20)    NOT NULL,
    changed_by_user_id  UUID,
    created_at          TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_fp_snapshot_reason CHECK (snapshot_reason IN ('UPDATE', 'DEACTIVATE', 'DELETE'))
);

CREATE INDEX idx_fp_snapshot_package ON feature_package_snapshots (feature_package_id);
CREATE INDEX idx_fp_snapshot_created ON feature_package_snapshots (created_at);
CREATE INDEX idx_fp_snapshot_reason  ON feature_package_snapshots (snapshot_reason);

COMMENT ON TABLE feature_package_snapshots IS 'Audit log: full state of FeaturePackage before UPDATE, DEACTIVATE, or DELETE';
COMMENT ON COLUMN feature_package_snapshots.snapshot_reason IS 'UPDATE | DEACTIVATE | DELETE';
COMMENT ON COLUMN feature_package_snapshots.changed_by_user_id IS 'Admin user who triggered the change';

-- ============================================================================
-- BOOST PACKAGE SNAPSHOTS
-- ============================================================================

CREATE TABLE boost_package_snapshots
(
    snapshot_id         UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    boost_package_id    UUID           NOT NULL,
    code                VARCHAR(50)    NOT NULL,
    name                VARCHAR(100)   NOT NULL,
    description         TEXT,
    featured_quota      INTEGER        NOT NULL,
    hot_badge_quota     INTEGER        NOT NULL,
    duration_days       INTEGER        NOT NULL,
    price               NUMERIC(12, 2) NOT NULL,
    is_active           BOOLEAN        NOT NULL,
    snapshot_reason     VARCHAR(20)    NOT NULL,
    changed_by_user_id  UUID,
    created_at          TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_bp_snapshot_reason CHECK (snapshot_reason IN ('UPDATE', 'DEACTIVATE', 'DELETE'))
);

CREATE INDEX idx_bp_snapshot_package ON boost_package_snapshots (boost_package_id);
CREATE INDEX idx_bp_snapshot_created ON boost_package_snapshots (created_at);
CREATE INDEX idx_bp_snapshot_reason  ON boost_package_snapshots (snapshot_reason);

COMMENT ON TABLE boost_package_snapshots IS 'Audit log: full state of BoostPackage before UPDATE, DEACTIVATE, or DELETE';
COMMENT ON COLUMN boost_package_snapshots.snapshot_reason IS 'UPDATE | DEACTIVATE | DELETE';
COMMENT ON COLUMN boost_package_snapshots.changed_by_user_id IS 'Admin user who triggered the change';
