-- V33__Insert_listing_price_histories.sql
-- Migration V33: Create 6 months of listing price history data
-- Each listing will have 1 price change record per month for the last 6 months

-- ============================================================================
-- Insert listing price histories for the last 6 months
-- ============================================================================
WITH date_series AS (
    SELECT 
        generate_series(
            DATE_TRUNC('month', CURRENT_DATE - INTERVAL '6 months'),
            DATE_TRUNC('month', CURRENT_DATE - INTERVAL '1 month'),
            INTERVAL '1 month'
        )::date as month_date
),
listing_monthly_prices AS (
    SELECT 
        l.listing_id,
        l.user_id as changed_by, -- Use listing creator as the one who changed prices
        l.price as current_price,
        l.min_price as current_min_price,
        l.max_price as current_max_price,
        ds.month_date,
        -- Generate price variations for each month (±5% to ±15% from current price)
        CASE 
            WHEN ds.month_date = DATE_TRUNC('month', CURRENT_DATE - INTERVAL '6 months') THEN
                -- 6 months ago: higher price (10-20% higher)
                ROUND((l.price * (1.10 + RANDOM() * 0.10))::numeric, 0)
            WHEN ds.month_date = DATE_TRUNC('month', CURRENT_DATE - INTERVAL '5 months') THEN
                -- 5 months ago: slightly higher (5-15% higher)
                ROUND((l.price * (1.05 + RANDOM() * 0.10))::numeric, 0)
            WHEN ds.month_date = DATE_TRUNC('month', CURRENT_DATE - INTERVAL '4 months') THEN
                -- 4 months ago: similar price (0-10% variation)
                ROUND((l.price * (0.95 + RANDOM() * 0.15))::numeric, 0)
            WHEN ds.month_date = DATE_TRUNC('month', CURRENT_DATE - INTERVAL '3 months') THEN
                -- 3 months ago: price reduction started (5-10% lower)
                ROUND((l.price * (0.90 + RANDOM() * 0.15))::numeric, 0)
            WHEN ds.month_date = DATE_TRUNC('month', CURRENT_DATE - INTERVAL '2 months') THEN
                -- 2 months ago: further reduction (2-8% higher than current)
                ROUND((l.price * (1.02 + RANDOM() * 0.06))::numeric, 0)
            ELSE
                -- 1 month ago: close to current price (0-5% higher)
                ROUND((l.price * (1.00 + RANDOM() * 0.05))::numeric, 0)
        END as historical_price
    FROM listings l
    CROSS JOIN date_series ds
    WHERE l.deleted = FALSE
      AND l.status IN ('PUBLISHED', 'SOLD', 'RENTED', 'EXPIRED') -- Only listings that were active
),
price_ranges AS (
    SELECT 
        lmp.*,
        -- Calculate min and max prices based on historical price (±10% range)
        ROUND((lmp.historical_price * 0.90)::numeric, 0) as historical_min_price,
        ROUND((lmp.historical_price * 1.10)::numeric, 0) as historical_max_price,
        -- Add some realistic timestamp within the month (random day between 1-28)
        (lmp.month_date + (RANDOM() * 27)::integer * INTERVAL '1 day')::timestamp as price_change_timestamp
    FROM listing_monthly_prices lmp
)
INSERT INTO listing_price_histories (listing_id, price, min_price, max_price, changed_by, created_at, updated_at)
SELECT 
    pr.listing_id,
    pr.historical_price as price,
    pr.historical_min_price as min_price,
    pr.historical_max_price as max_price,
    pr.changed_by,
    pr.price_change_timestamp as created_at,
    pr.price_change_timestamp as updated_at
FROM price_ranges pr
ORDER BY pr.listing_id, pr.month_date;

-- ============================================================================
-- Add current price as the most recent history record
-- ============================================================================
INSERT INTO listing_price_histories (listing_id, price, min_price, max_price, changed_by, created_at, updated_at)
SELECT 
    l.listing_id,
    l.price,
    l.min_price,
    l.max_price,
    l.user_id as changed_by,
    l.updated_at as created_at, -- Use listing's updated_at as the current price change time
    l.updated_at as updated_at
FROM listings l
WHERE l.deleted = FALSE
  AND l.status IN ('PUBLISHED', 'SOLD', 'RENTED', 'EXPIRED');