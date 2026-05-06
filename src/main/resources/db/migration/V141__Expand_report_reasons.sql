-- Expand report_reason CHECK constraint to support new report types
-- Migration V3 originally defined: SCAM, FAKE_INFO, DUPLICATE, HARASSMENT, OTHER
-- Now adding: SPAM, MISLEADING, INAPPROPRIATE, WRONG_INFO, FAKE_PROFILE, VIOLATES_TERMS

ALTER TABLE reports DROP CONSTRAINT chk_report_reason;

ALTER TABLE reports ADD CONSTRAINT chk_report_reason CHECK (
    report_reason IN (
        'SCAM',
        'FAKE_INFO',
        'DUPLICATE',
        'HARASSMENT',
        'SPAM',
        'MISLEADING',
        'INAPPROPRIATE',
        'WRONG_INFO',
        'FAKE_PROFILE',
        'VIOLATES_TERMS',
        'OTHER'
    )
);
