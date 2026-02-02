-- V29__Update_listings_with_missing_fields.sql
-- Migration V29: Update listings with missing fields for better UX
-- Updates: min_price, max_price, is_negotiable, published_at, user-friendly names and slugs
--
-- Strategy:
-- 1. Set min_price = 90% of price, max_price = 120% of price (negotiation range)
-- 2. Set is_negotiable randomly (70% true, 30% false)
-- 3. Set published_at = created_at for published listings
-- 4. Update name with property description + location
-- 5. Update slug with readable format + random number

-- ============================================================================
-- Step 0: Create unaccent extension if possible (ignore if fails)
-- ============================================================================
DO $$ 
BEGIN
    CREATE EXTENSION IF NOT EXISTS unaccent;
EXCEPTION 
    WHEN OTHERS THEN 
        -- Ignore if we don't have permission or extension doesn't exist
        NULL;
END $$;

-- ============================================================================
-- Step 1: Update min_price, max_price, and is_negotiable
-- ============================================================================
UPDATE listings 
SET 
    min_price = ROUND(price * 0.9, 0),
    max_price = ROUND(price * 1.2, 0),
    is_negotiable = CASE WHEN RANDOM() > 0.3 THEN TRUE ELSE FALSE END
WHERE deleted = FALSE 
  AND (min_price IS NULL OR max_price IS NULL);

-- ============================================================================
-- Step 2: Update published_at for published listings
-- ============================================================================
UPDATE listings 
SET published_at = created_at
WHERE deleted = FALSE 
  AND status = 'PUBLISHED' 
  AND published_at IS NULL;

-- ============================================================================
-- Step 3: Update names with user-friendly format using property descriptions
-- ============================================================================

-- APARTMENT listings
UPDATE listings 
SET name = CASE 
    WHEN p.descriptions IS NOT NULL AND LENGTH(TRIM(p.descriptions)) > 0 
    THEN TRIM(p.descriptions) || ' - ' || loc.name
    ELSE 
        CASE pt.code
            WHEN 'APARTMENT' THEN 'Căn hộ hiện đại'
            WHEN 'HOUSE' THEN 'Nhà gia đình'
            WHEN 'VILLA' THEN 'Biệt thự sang trọng'
            WHEN 'TOWNHOUSE' THEN 'Nhà phố liền kề'
            WHEN 'PENTHOUSE' THEN 'Penthouse cao cấp'
            WHEN 'STUDIO' THEN 'Căn hộ studio'
            WHEN 'OFFICE' THEN 'Văn phòng cho thuê'
            WHEN 'RETAIL' THEN 'Mặt bằng kinh doanh'
            WHEN 'RESTAURANT' THEN 'Nhà hàng'
            WHEN 'HOTEL' THEN 'Khách sạn'
            WHEN 'SHOPHOUSE' THEN 'Shophouse'
            WHEN 'MALL' THEN 'Trung tâm thương mại'
            WHEN 'WAREHOUSE' THEN 'Kho bãi'
            WHEN 'FACTORY' THEN 'Nhà máy'
            WHEN 'WORKSHOP' THEN 'Xưởng sản xuất'
            WHEN 'LOGISTICS' THEN 'Trung tâm logistics'
            ELSE 'Bất động sản'
        END || ' tại ' || loc.name
    END
FROM properties p
JOIN property_types pt ON p.property_type_id = pt.property_type_id
JOIN locations loc ON p.location_id = loc.location_id
WHERE listings.property_id = p.property_id
  AND listings.deleted = FALSE;

-- ============================================================================
-- Step 4: Update slugs with user-friendly format
-- ============================================================================

-- Generate new slugs based on updated names
-- Use a manual character replacement approach for Vietnamese characters
UPDATE listings 
SET slug = LOWER(
    REGEXP_REPLACE(
        REGEXP_REPLACE(
            REGEXP_REPLACE(
                TRANSLATE(
                    CASE 
                        WHEN name IS NOT NULL AND LENGTH(TRIM(name)) > 0 
                        THEN TRIM(SUBSTRING(name FROM 1 FOR 50))
                        ELSE 'bat-dong-san'
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
) || '-' || FLOOR(RANDOM() * 10000 + 1000)::text
WHERE deleted = FALSE;

-- ============================================================================
-- Handle duplicate slugs (if any)
-- ============================================================================

-- Update any remaining duplicate slugs by appending additional random numbers
UPDATE listings 
SET slug = slug || '-' || FLOOR(RANDOM() * 10000 + 1000)::text
WHERE listing_id IN (
    SELECT listing_id FROM (
        SELECT listing_id, 
               ROW_NUMBER() OVER (PARTITION BY slug ORDER BY created_at) as rn
        FROM listings 
        WHERE deleted = FALSE
    ) t WHERE rn > 1
);