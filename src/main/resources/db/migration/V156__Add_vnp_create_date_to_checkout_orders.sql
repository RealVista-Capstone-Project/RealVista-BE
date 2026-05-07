-- VNPay QueryDR: vnp_TransactionDate must match vnp_CreateDate from original pay request; persisted per checkout.
ALTER TABLE checkout_orders
    ADD COLUMN IF NOT EXISTS vnp_create_date VARCHAR(14);
