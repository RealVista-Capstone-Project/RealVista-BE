-- V66__Create_checkout_orders_table.sql
-- Temporary checkout orders before payment confirmation
-- Once payment succeeds, a transaction is created from this order

CREATE TABLE checkout_orders
(
    checkout_order_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id           UUID           NOT NULL,
    transaction_type  VARCHAR(50)    NOT NULL,
    plan_code         VARCHAR(50)    NOT NULL,
    amount            NUMERIC(12, 2) NOT NULL,
    payment_method    VARCHAR(20)    NOT NULL,
    order_code        BIGINT UNIQUE  NOT NULL,
    created_at        TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP      NOT NULL DEFAULT NOW(),
    deleted           BOOLEAN        DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT chk_checkout_transaction_type CHECK (transaction_type IN ('BOOST', 'SUBSCRIPTION')),
    CONSTRAINT chk_checkout_payment_method CHECK (payment_method IN ('PAYOS', 'VNPAY'))
);

CREATE INDEX idx_checkout_user ON checkout_orders (user_id);
CREATE INDEX idx_checkout_order_code ON checkout_orders (order_code);
