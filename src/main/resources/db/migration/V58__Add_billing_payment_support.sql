-- V58__Add_billing_payment_support.sql
-- Adds order_code, plan_code to transactions for PayOS integration; makes reference_id nullable

-- ============================================================================
-- TRANSACTIONS: add gateway reference columns
-- ============================================================================

ALTER TABLE transactions
    ALTER COLUMN reference_id DROP NOT NULL;

ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS order_code BIGINT UNIQUE;

ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS plan_code VARCHAR(50);