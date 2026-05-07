-- V153: Seed realistic listing view data for the past month
-- Seeds both listing_views (per-user) and listing_daily_view_buckets (daily aggregates)
-- Uses dynamic listing/user IDs from existing data — safe to re-run via ON CONFLICT

DO $$
DECLARE
    v_listing_ids   UUID[];
    v_user_ids      UUID[];
    v_listing_id    UUID;
    v_user_id       UUID;
    v_view_count    INT;
    v_days_ago      INT;
    v_viewed_at     TIMESTAMP;
    v_viewer_count  INT;
    v_total_users   INT;
    v_total_listings INT;
    i               INT;
    d               INT;
    daily_views     INT;
    -- Popularity tier weights per listing index (mod 5)
    -- 0=hot, 1=active, 2=average, 3=low, 4=very-low
    tier            INT;
BEGIN
    -- ──────────────────────────────────────────
    -- Collect IDs
    -- ──────────────────────────────────────────
    SELECT ARRAY_AGG(listing_id ORDER BY created_at)
    INTO   v_listing_ids
    FROM   listings
    WHERE  deleted = FALSE
      AND  status IN ('PUBLISHED', 'PENDING_APPROVAL', 'EXPIRED', 'SOLD', 'RENTED');

    SELECT ARRAY_AGG(user_id ORDER BY created_at)
    INTO   v_user_ids
    FROM   users
    WHERE  deleted = FALSE;

    v_total_listings := COALESCE(array_length(v_listing_ids, 1), 0);
    v_total_users    := COALESCE(array_length(v_user_ids,    1), 0);

    IF v_total_listings = 0 OR v_total_users = 0 THEN
        RAISE NOTICE 'No listings or users found – skipping view seed.';
        RETURN;
    END IF;

    -- ──────────────────────────────────────────
    -- 1. Seed listing_views  (per-user totals)
    -- ──────────────────────────────────────────
    FOR i IN 1..v_total_listings LOOP
        v_listing_id := v_listing_ids[i];

        -- Assign popularity tier based on position
        tier := (i - 1) % 5;

        -- Number of unique viewers
        v_viewer_count := CASE tier
            WHEN 0 THEN 18 + floor(random() * 12)::INT   -- hot:    18–30
            WHEN 1 THEN 12 + floor(random() * 8)::INT    -- active: 12–20
            WHEN 2 THEN  7 + floor(random() * 6)::INT    -- avg:     7–13
            WHEN 3 THEN  4 + floor(random() * 4)::INT    -- low:     4–8
            ELSE         2 + floor(random() * 3)::INT    -- very-low:2–5
        END;

        -- Clamp to available users
        v_viewer_count := LEAST(v_viewer_count, v_total_users);

        FOR j IN 1..v_viewer_count LOOP
            -- Pick a pseudo-random user (spread across the pool)
            v_user_id := v_user_ids[
                1 + ((i * 37 + j * 13 + floor(random() * 7)::INT) % v_total_users)
            ];

            -- Per-user view count (heavier for popular listings)
            v_view_count := CASE tier
                WHEN 0 THEN 3 + floor(random() * 18)::INT
                WHEN 1 THEN 2 + floor(random() * 10)::INT
                WHEN 2 THEN 1 + floor(random() * 6)::INT
                WHEN 3 THEN 1 + floor(random() * 4)::INT
                ELSE         1 + floor(random() * 2)::INT
            END;

            -- viewed_at: random moment within last 30 days
            v_days_ago  := floor(random() * 30)::INT;
            v_viewed_at := (NOW() - (v_days_ago || ' days')::INTERVAL
                                  - (floor(random() * 86400) || ' seconds')::INTERVAL)::TIMESTAMP;

            INSERT INTO listing_views
                (listing_id, user_id, view_count, viewed_at, created_at, updated_at, deleted)
            VALUES
                (v_listing_id, v_user_id, v_view_count, v_viewed_at, v_viewed_at, v_viewed_at, FALSE)
            ON CONFLICT (listing_id, user_id) DO UPDATE
                SET view_count = listing_views.view_count + EXCLUDED.view_count,
                    viewed_at  = GREATEST(listing_views.viewed_at, EXCLUDED.viewed_at),
                    updated_at = NOW();
        END LOOP;
    END LOOP;

    -- ──────────────────────────────────────────
    -- 2. Seed listing_daily_view_buckets (30 days)
    -- ──────────────────────────────────────────
    FOR i IN 1..v_total_listings LOOP
        v_listing_id := v_listing_ids[i];
        tier := (i - 1) % 5;

        FOR d IN 0..29 LOOP
            -- Recency weight: recent days get more views
            -- Days 0-6 (last week) ~2x the traffic of days 21-29
            daily_views := CASE tier
                WHEN 0 THEN 20 + floor(random() * 50)::INT  -- hot
                WHEN 1 THEN 10 + floor(random() * 25)::INT  -- active
                WHEN 2 THEN  4 + floor(random() * 12)::INT  -- avg
                WHEN 3 THEN  2 + floor(random() * 6)::INT   -- low
                ELSE          0 + floor(random() * 3)::INT  -- very-low
            END;

            -- Apply recency boost for last 7 days
            IF d <= 6 THEN
                daily_views := daily_views + floor(daily_views * 0.5 * random())::INT;
            END IF;

            -- Skip zero-view days for very-low tier (realistic sparse activity)
            IF daily_views = 0 AND tier = 4 THEN
                CONTINUE;
            END IF;

            INSERT INTO listing_daily_view_buckets
                (listing_id, bucket_date, view_count)
            VALUES
                (v_listing_id,
                 (CURRENT_DATE - d)::DATE,
                 daily_views)
            ON CONFLICT (listing_id, bucket_date) DO UPDATE
                SET view_count = listing_daily_view_buckets.view_count + EXCLUDED.view_count;
        END LOOP;
    END LOOP;

    RAISE NOTICE 'View seed complete: % listings processed.', v_total_listings;
END $$;
