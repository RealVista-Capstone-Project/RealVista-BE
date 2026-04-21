-- V51__Add_content_to_listings.sql
-- Add content column to listings table
-- Compatible with both PostgreSQL and H2 databases

-- Add content column (for long-form listing descriptions/articles)
ALTER TABLE listings ADD COLUMN content TEXT;

-- Backfill content with realistic Vietnamese marketing descriptions
UPDATE listings l
SET content = sub.description
FROM (
    SELECT 
        l2.listing_id,
        CASE 
            WHEN pt.code IN ('APARTMENT', 'HOUSE', 'VILLA', 'TOWNHOUSE', 'PENTHOUSE', 'STUDIO') THEN
                'Chào mừng bạn đến với ' || l2.name || '. Tọa lạc tại địa chỉ ' || p.street_address || ', ' || loc.name || 
                ', bất động sản này sở hữu diện tích ' || p.usable_size_m2 || ' m2, được thiết kế hiện đại và tối ưu công năng. ' ||
                'Đây là cơ hội tuyệt vời để an cư hoặc đầu tư sinh lời bền vững tại khu vực phát triển sôi động của TP.HCM. ' ||
                'Pháp lý hoàn thiện, sổ hồng riêng, sẵn sàng giao dịch ngay.'
            WHEN pt.code IN ('OFFICE', 'SHOPHOUSE', 'RETAIL', 'MALL', 'RESTAURANT', 'HOTEL') THEN
                l2.name || ' - Cơ hội kinh doanh đắc địa tại ' || p.street_address || ', ' || loc.name || '. Với diện tích ' || p.usable_size_m2 || 
                ' m2, mặt bằng hội tụ đầy đủ các yếu tố thuận lợi về vị trí, giao thông và tệp khách hàng tiềm năng. ' ||
                'Pháp lý hoàn thiện, hỗ trợ tối đa doanh nghiệp vận hành.'
            ELSE 
                'Hệ thống ' || l2.name || ' tọa lạc tại vị trí chiến lược thuộc ' || loc.name || '. Tổng diện tích sử dụng ' || p.usable_size_m2 || 
                ' m2, thiết kế đạt chuẩn công nghiệp với hệ thống điện 3 pha, cấp thoát nước và lộ giới xe container ra vào dễ dàng. ' ||
                'Đây là lựa chọn hàng đầu cho các doanh nghiệp kho bãi, nhà xưởng.'
        END as description
    FROM listings l2
    JOIN properties p ON l2.property_id = p.property_id
    JOIN locations loc ON p.location_id = loc.location_id
    JOIN property_types pt ON p.property_type_id = pt.property_type_id
) sub
WHERE l.listing_id = sub.listing_id;
