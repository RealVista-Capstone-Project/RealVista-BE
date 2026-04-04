-- V48__Enforce_single_published_listing_per_user_type.sql
-- Ensure that each user has only one PUBLISHED listing per listing_type (SALE, RENT)
-- Any extra published listings mapped to the same user and listing_type will be set to DRAFT
-- Compatible with PostgreSQL and H2.

UPDATE listings l1
SET status = 'DRAFT'
WHERE l1.status = 'PUBLISHED'
  AND EXISTS (
      SELECT 1
      FROM listings l2
      WHERE l2.user_id = l1.user_id
        AND l2.listing_type = l1.listing_type
        AND l2.status = 'PUBLISHED'
        AND CAST(l2.listing_id AS VARCHAR) < CAST(l1.listing_id AS VARCHAR)
  );
