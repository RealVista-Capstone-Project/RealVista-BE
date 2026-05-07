-- Daily view buckets per listing (Asia/Ho_Chi_Minh calendar date; incremented on each recorded view).

CREATE TABLE listing_daily_view_buckets
(
    listing_id UUID         NOT NULL,
    bucket_date DATE        NOT NULL,
    view_count  BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (listing_id, bucket_date),
    CONSTRAINT fk_listing_daily_views_listing
        FOREIGN KEY (listing_id)
            REFERENCES listings (listing_id)
            ON DELETE CASCADE
);

CREATE INDEX idx_listing_daily_view_bucket_listing
    ON listing_daily_view_buckets (listing_id);
