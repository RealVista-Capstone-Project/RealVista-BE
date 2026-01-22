-- V3__Create_subscription_boost_payment_analytics_tables.sql
-- Creates subscription plans, boost packages, transactions, AI features, reviews, reports, leads, legal documents
-- Compatible with both PostgreSQL and H2 databases

-- ============================================================================
-- SUBSCRIPTION & PLANS TABLES
-- ============================================================================

CREATE TABLE subscription_plans
(
    subscription_plan_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    code                 VARCHAR(50)    NOT NULL UNIQUE,
    name                 VARCHAR(100)   NOT NULL,
    description          TEXT,
    roles                JSON,
    duration_days        INTEGER        NOT NULL,
    price                NUMERIC(12, 2) NOT NULL,
    ai_features          JSON,
    verify_enabled       BOOLEAN   DEFAULT FALSE,
    verify_cycle_days    INTEGER        NOT NULL DEFAULT 0,
    is_active            BOOLEAN   DEFAULT TRUE,
    created_at           TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP      NOT NULL DEFAULT NOW(),
    deleted              BOOLEAN   DEFAULT FALSE
);

CREATE TABLE user_subscriptions
(
    user_subscription_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    subscription_plan_id UUID        NOT NULL,
    user_id              UUID        NOT NULL,
    start_date           DATE        NOT NULL,
    end_date             DATE        NOT NULL,
    status               VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at           TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted              BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (subscription_plan_id) REFERENCES subscription_plans (subscription_plan_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT chk_user_subscription_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED'))
);

CREATE INDEX idx_user_subscription_plan ON user_subscriptions (subscription_plan_id);
CREATE INDEX idx_user_subscription_user ON user_subscriptions (user_id);
CREATE INDEX idx_user_subscription_status ON user_subscriptions (status);

-- ============================================================================
-- BOOST & PROMOTE TABLES
-- ============================================================================

CREATE TABLE boost_packages
(
    boost_package_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    code             VARCHAR(50)    NOT NULL UNIQUE,
    name             VARCHAR(100)   NOT NULL,
    description      TEXT,
    featured_quota   INTEGER   DEFAULT 0,
    hot_badge_quota  INTEGER   DEFAULT 0,
    duration_days    INTEGER        NOT NULL,
    price            NUMERIC(12, 2) NOT NULL,
    is_active        BOOLEAN   DEFAULT TRUE,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    deleted          BOOLEAN   DEFAULT FALSE
);

CREATE TABLE listing_boosts
(
    listing_boost_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    boost_package_id UUID        NOT NULL,
    listing_id       UUID        NOT NULL,
    user_id          UUID        NOT NULL,
    boost_type       VARCHAR(20) NOT NULL,
    start_date       DATE        NOT NULL,
    end_date         DATE        NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted          BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (boost_package_id) REFERENCES boost_packages (boost_package_id),
    FOREIGN KEY (listing_id) REFERENCES listings (listing_id) ON DELETE CASCADE,
    CONSTRAINT chk_boost_type CHECK (boost_type IN ('FEATURED', 'HOT_BADGE')),
    CONSTRAINT chk_listing_boost_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED'))
);

CREATE INDEX idx_listing_boost_package ON listing_boosts (boost_package_id);
CREATE INDEX idx_listing_boost_listing ON listing_boosts (listing_id);
CREATE INDEX idx_listing_boost_user ON listing_boosts (user_id);
CREATE INDEX idx_listing_boost_type ON listing_boosts (boost_type);
CREATE INDEX idx_listing_boost_status ON listing_boosts (status);

-- ============================================================================
-- PAYMENT / TRANSACTION TABLES
-- ============================================================================

CREATE TABLE transactions
(
    transaction_id   UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id          UUID           NOT NULL,
    transaction_type VARCHAR(50)    NOT NULL,
    reference_id     UUID           NOT NULL,
    amount           NUMERIC(12, 2) NOT NULL,
    payment_method   VARCHAR(20)    NOT NULL,
    payment_status   VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    deleted          BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('BOOST', 'SUBSCRIPTION')),
    CONSTRAINT chk_payment_method CHECK (payment_method IN ('PAYOS', 'STRIPE')),
    CONSTRAINT chk_payment_status CHECK (payment_status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED'))
);

CREATE INDEX idx_transaction_user ON transactions (user_id);
CREATE INDEX idx_transaction_type ON transactions (transaction_type);
CREATE INDEX idx_transaction_status ON transactions (payment_status);

-- ============================================================================
-- AI & ANALYTICS TABLES
-- ============================================================================

CREATE TABLE ai_feature_usages
(
    ai_feature_usage_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id             UUID        NOT NULL,
    ai_feature          VARCHAR(50) NOT NULL,
    period_start        DATE        NOT NULL,
    period_end          DATE        NOT NULL,
    usage_count         INTEGER DEFAULT 0,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT chk_ai_feature CHECK (ai_feature IN ('AI_RECOMMEND_LISTING', 'AI_WORTH_BUYING', 'AI_COMPARE', 'AI_PRICE_INSIGHT'))
);

CREATE INDEX idx_ai_feature_usage_user ON ai_feature_usages (user_id);
CREATE INDEX idx_ai_feature_usage_feature ON ai_feature_usages (ai_feature);

CREATE TABLE financial_calculations
(
    calc_id          UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id          UUID        NOT NULL,
    listing_id       UUID        NOT NULL,
    calculation_type VARCHAR(50) NOT NULL,
    input_params     JSON,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message    TEXT,
    result           JSON,
    created_at       TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted          BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (listing_id) REFERENCES listings (listing_id) ON DELETE CASCADE,
    CONSTRAINT chk_calculation_type CHECK (calculation_type IN ('MORTGAGE', 'RENT_VS_BUY', 'COST_BREAKDOWN')),
    CONSTRAINT chk_calculation_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_financial_calc_user ON financial_calculations (user_id);
CREATE INDEX idx_financial_calc_listing ON financial_calculations (listing_id);
CREATE INDEX idx_financial_calc_type ON financial_calculations (calculation_type);
CREATE INDEX idx_financial_calc_status ON financial_calculations (status);

-- ============================================================================
-- REVIEWS & REPORTS TABLES
-- ============================================================================

CREATE TABLE agent_reviews
(
    agent_review_id  UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    agent_profile_id UUID      NOT NULL,
    reviewer_id      UUID      NOT NULL,
    listing_id       UUID      NOT NULL,
    review           TEXT,
    rating           NUMERIC(2, 1),
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted          BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (agent_profile_id) REFERENCES agent_profiles (agent_profile_id)
);

CREATE INDEX idx_agent_review_profile ON agent_reviews (agent_profile_id);
CREATE INDEX idx_agent_review_reviewer ON agent_reviews (reviewer_id);
CREATE INDEX idx_agent_review_listing ON agent_reviews (listing_id);

CREATE TABLE reports
(
    report_id           UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    reporter_user_id    UUID        NOT NULL,
    reported_user_id    UUID,
    reported_listing_id UUID,
    report_target_type  VARCHAR(20) NOT NULL,
    report_reason       VARCHAR(50) NOT NULL,
    description         TEXT,
    evidence_media_url  TEXT,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    admin_note          TEXT,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN DEFAULT FALSE,

    CONSTRAINT chk_report_target_type CHECK (report_target_type IN ('USER', 'LISTING')),
    CONSTRAINT chk_report_status CHECK (status IN ('PENDING', 'REVIEWING', 'RESOLVED', 'DISMISSED'))
);

CREATE INDEX idx_report_reporter ON reports (reporter_user_id);
CREATE INDEX idx_report_reported_user ON reports (reported_user_id);
CREATE INDEX idx_report_reported_listing ON reports (reported_listing_id);
CREATE INDEX idx_report_target_type ON reports (report_target_type);
CREATE INDEX idx_report_status ON reports (status);

-- ============================================================================
-- PROPERTY FEE SERVICES TABLE
-- ============================================================================

CREATE TABLE property_fee_services
(
    property_fee_service_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    property_id             UUID           NOT NULL,
    fee_type                VARCHAR(50)    NOT NULL,
    fee_name                VARCHAR(100)   NOT NULL,
    amount                  NUMERIC(12, 2),
    billing_cycle           VARCHAR(20)    NOT NULL,
    is_optional             BOOLEAN DEFAULT FALSE,
    description             TEXT,
    created_at              TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP      NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (property_id) REFERENCES properties (property_id) ON DELETE CASCADE,
    CONSTRAINT chk_fee_type CHECK (fee_type IN ('MANAGEMENT', 'PARKING', 'INTERNET', 'ELECTRICITY', 'WATER', 'GARBAGE', 'SECURITY', 'OTHER')),
    CONSTRAINT chk_billing_cycle CHECK (billing_cycle IN ('MONTHLY', 'YEARLY', 'ONE_TIME'))
);

CREATE INDEX idx_property_fee_property ON property_fee_services (property_id);
CREATE INDEX idx_property_fee_type ON property_fee_services (fee_type);

-- ============================================================================
-- LEAD / CRM TABLES
-- ============================================================================

CREATE TABLE listing_leads
(
    listing_lead_id   UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    agent_id          UUID        NOT NULL,
    listing_id        UUID        NOT NULL,
    buyer_id          UUID        NOT NULL,
    conversation_id   UUID,
    status            VARCHAR(20) NOT NULL DEFAULT 'NEW',
    priority          VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    last_contacted_at TIMESTAMP,
    next_follow_up_at TIMESTAMP,
    created_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted           BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (listing_id) REFERENCES listings (listing_id) ON DELETE CASCADE,
    CONSTRAINT chk_lead_status CHECK (status IN ('NEW', 'CONTACTED', 'QUALIFIED', 'NEGOTIATING', 'CLOSED_WON', 'CLOSED_LOST')),
    CONSTRAINT chk_lead_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))
);

CREATE INDEX idx_listing_lead_agent ON listing_leads (agent_id);
CREATE INDEX idx_listing_lead_listing ON listing_leads (listing_id);
CREATE INDEX idx_listing_lead_buyer ON listing_leads (buyer_id);
CREATE INDEX idx_listing_lead_status ON listing_leads (status);
CREATE INDEX idx_listing_lead_priority ON listing_leads (priority);

CREATE TABLE lead_notes
(
    lead_note_id    UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    listing_lead_id UUID        NOT NULL,
    note_type       VARCHAR(20) NOT NULL,
    tag             VARCHAR(50),
    content         TEXT,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (listing_lead_id) REFERENCES listing_leads (listing_lead_id) ON DELETE CASCADE,
    CONSTRAINT chk_note_type CHECK (note_type IN ('NOTE', 'TAG', 'SYSTEM'))
);

CREATE INDEX idx_lead_note_lead ON lead_notes (listing_lead_id);
CREATE INDEX idx_lead_note_type ON lead_notes (note_type);

-- ============================================================================
-- LEASE & LEGAL TABLES
-- ============================================================================

CREATE TABLE lease_agreements
(
    lease_agreement_id    UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    listing_id            UUID           NOT NULL,
    renter_id             UUID           NOT NULL,
    landlord_id           UUID           NOT NULL,
    agent_id              UUID,
    lease_start_date      DATE,
    lease_end_date        DATE,
    lease_duration_months INTEGER        NOT NULL,
    monthly_rent          NUMERIC(12, 2),
    security_deposit      NUMERIC(12, 2),
    lease_document_url    TEXT,
    signed_by_renter_at   TIMESTAMP,
    signed_by_landlord_at TIMESTAMP,
    status                VARCHAR(30)    NOT NULL DEFAULT 'DRAFT',
    reject_reason         TEXT,
    verified_by           UUID,
    created_at            TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP      NOT NULL DEFAULT NOW(),
    deleted               BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (listing_id) REFERENCES listings (listing_id),
    CONSTRAINT chk_lease_status CHECK (status IN ('DRAFT', 'PENDING_RENTER', 'PENDING_LANDLORD', 'ACTIVE', 'EXPIRED', 'TERMINATED', 'REJECTED'))
);

CREATE INDEX idx_lease_listing ON lease_agreements (listing_id);
CREATE INDEX idx_lease_renter ON lease_agreements (renter_id);
CREATE INDEX idx_lease_landlord ON lease_agreements (landlord_id);
CREATE INDEX idx_lease_agent ON lease_agreements (agent_id);
CREATE INDEX idx_lease_status ON lease_agreements (status);

CREATE TABLE property_legal_documents
(
    property_legal_document_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    property_id                UUID         NOT NULL,
    document_type              VARCHAR(50)  NOT NULL,
    document_number            VARCHAR(100) NOT NULL,
    issued_date                DATE,
    issuing_authority          VARCHAR(255) NOT NULL,
    expiry_date                DATE,
    document_url               TEXT         NOT NULL,
    notes                      TEXT,
    is_primary                 BOOLEAN DEFAULT FALSE,
    verification_status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    reject_reason              TEXT,
    verified_by                UUID,
    created_at                 TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at                 TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted                    BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (property_id) REFERENCES properties (property_id) ON DELETE CASCADE,
    CONSTRAINT chk_document_type CHECK (document_type IN ('RED_BOOK', 'PINK_BOOK', 'CONSTRUCTION_PERMIT', 'OWNERSHIP_CERT', 'OTHER')),
    CONSTRAINT chk_verification_status CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED'))
);

CREATE INDEX idx_property_legal_doc_property ON property_legal_documents (property_id);
CREATE INDEX idx_property_legal_doc_type ON property_legal_documents (document_type);
CREATE INDEX idx_property_legal_doc_status ON property_legal_documents (verification_status);

-- ============================================================================
-- LISTING VIEWS TABLE (Track unique visitors)
-- ============================================================================

-- view_count = SELECT COUNT(*) FROM listing_views WHERE listing_id = ?
CREATE TABLE listing_views
(
    listing_id UUID      NOT NULL,
    user_id    UUID      NOT NULL,
    view_count INTEGER   NOT NULL DEFAULT 1,
    viewed_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted    BOOLEAN DEFAULT FALSE,

    PRIMARY KEY (listing_id, user_id),
    FOREIGN KEY (listing_id) REFERENCES listings (listing_id) ON DELETE CASCADE
);

CREATE INDEX idx_listing_view_listing ON listing_views (listing_id);
CREATE INDEX idx_listing_view_user ON listing_views (user_id);

CREATE TABLE listing_price_histories
(
    listing_price_history_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    listing_id               UUID           NOT NULL,
    price                    NUMERIC(14, 2) NOT NULL,
    min_price                NUMERIC(14, 2),
    max_price                NUMERIC(14, 2),
    changed_by               UUID           NOT NULL,
    created_at               TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP      NOT NULL DEFAULT NOW(),
    deleted                  BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (listing_id) REFERENCES listings (listing_id) ON DELETE CASCADE
);

CREATE INDEX idx_listing_price_history_listing ON listing_price_histories (listing_id);
CREATE INDEX idx_listing_price_history_changed_by ON listing_price_histories (changed_by);

-- ============================================================================
-- BOOKMARKS TABLE (Composite Primary Key)
-- ============================================================================

CREATE TABLE bookmarks
(
    user_id    UUID      NOT NULL,
    listing_id UUID      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted    BOOLEAN DEFAULT FALSE,

    PRIMARY KEY (user_id, listing_id),
    FOREIGN KEY (listing_id) REFERENCES listings (listing_id) ON DELETE CASCADE
);

CREATE INDEX idx_bookmark_user ON bookmarks (user_id);
CREATE INDEX idx_bookmark_listing ON bookmarks (listing_id);
