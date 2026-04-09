-- V65__Allow_vnpay_payment_method.sql
-- Extend transactions.payment_method CHECK to include VNPAY (was PAYOS, STRIPE only)

ALTER TABLE transactions DROP CONSTRAINT IF EXISTS chk_payment_method;

ALTER TABLE transactions ADD CONSTRAINT chk_payment_method
    CHECK (payment_method IN ('PAYOS', 'STRIPE', 'VNPAY'));
