-- Track which agent/user closed the sale or rental deal on each listing.
-- This allows property owners to identify who should be charged a commission.

ALTER TABLE listings
    ADD COLUMN sold_by_user_id UUID        NULL REFERENCES users(user_id) ON DELETE SET NULL,
    ADD COLUMN sold_at          TIMESTAMP  NULL,
    ADD COLUMN rented_by_user_id UUID      NULL REFERENCES users(user_id) ON DELETE SET NULL,
    ADD COLUMN rented_at         TIMESTAMP NULL;
