-- V42__Update_slugs_with_short_uuid.sql
-- Migration V42: Update slugs to use short UUID format
-- New format: {slugified-name}-{short-uuid}
-- Example: luxury-2-bedroom-apartment-2qLn4Z8XooP (instead of luxury-apartment-i.610e8400...)
--
-- Benefits:
-- 1. SEO-friendly with readable names (50 chars max)
-- 2. Short URLs (~60-65 total chars vs 140+ chars)
-- 3. Unique via short UUID (11-12 chars)
-- 4. Better user experience (easier to share/remember)

-- ============================================================================
-- Note: Short UUID generation must be done in application code
-- This migration will be executed after application generates new slugs
-- For now, we'll update to a temporary format, then application will fix it
-- ============================================================================

-- Update slug format to use first 11 chars of UUID as temporary short ID
-- Application will regenerate proper Base62-encoded short IDs on startup
UPDATE listings
SET slug = LOWER(
    REGEXP_REPLACE(
        REGEXP_REPLACE(
            REGEXP_REPLACE(
                TRANSLATE(
                    CASE
                        WHEN name IS NOT NULL AND LENGTH(TRIM(name)) > 0
                        THEN TRIM(SUBSTRING(name FROM 1 FOR 50))
                        ELSE 'property'
                    END,
                    'áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđĐÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴ',
                    'aaaaaaaaaaaaaaaeeeeeeeeeeeiiiiioooooooooooooooooouuuuuuuuuuuyyyyydDAAAAAAAAAAAAAEEEEEEEEEEEIIIIIOOOOOOOOOOOOOOOOOUUUUUUUUUUUYYYYY'
                ),
                '[^a-zA-Z0-9\s-]', '', 'g'
            ),
            '\s+', '-', 'g'
        ),
        '-+', '-', 'g'
    )
) || '-' || SUBSTRING(REPLACE(listing_id::text, '-', '') FROM 1 FOR 11)
WHERE deleted = FALSE;

-- ============================================================================
-- Add comment for application to regenerate proper Base62 slugs
-- ============================================================================
COMMENT ON COLUMN listings.slug IS 'SEO-friendly slug format: {name}-{base62-short-uuid}. Updated by V42 migration.';
