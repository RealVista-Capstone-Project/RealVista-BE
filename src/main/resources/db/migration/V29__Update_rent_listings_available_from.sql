-- V30__Update_rent_listings_available_from.sql
-- Migration V30: Set available_from dates for RENT listings
-- Sets appropriate availability dates for rental properties

-- ============================================================================
-- Set available_from dates for RENT listings
-- ============================================================================

-- Strategy: Set different availability patterns based on listing status
-- 1. PUBLISHED rentals: Available immediately (current date)
-- 2. DRAFT/PENDING: Available in 1-4 weeks (future date)
-- 3. RENTED: Set historical availability (past date)
-- 4. Add some variety with random offsets for realism

UPDATE listings 
SET available_from = CASE 
    -- Published listings: available now or very soon (0-7 days)
    WHEN status = 'PUBLISHED' THEN 
        CURRENT_DATE + (RANDOM() * 7)::integer
    
    -- Draft/Pending listings: available in 2-6 weeks
    WHEN status IN ('DRAFT', 'PENDING') THEN 
        CURRENT_DATE + (14 + RANDOM() * 28)::integer
    
    -- Rented listings: were available in the past (1-3 months ago)
    WHEN status = 'RENTED' THEN 
        CURRENT_DATE - (30 + RANDOM() * 60)::integer
    
    -- Expired listings: were available recently (2 weeks to 2 months ago)
    WHEN status = 'EXPIRED' THEN 
        CURRENT_DATE - (14 + RANDOM() * 45)::integer
    
    -- Default: available in 1-2 weeks
    ELSE 
        CURRENT_DATE + (7 + RANDOM() * 14)::integer
END
WHERE listing_type = 'RENT' 
  AND deleted = FALSE 
  AND available_from IS NULL;