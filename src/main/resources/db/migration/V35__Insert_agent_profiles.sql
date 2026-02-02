-- V37__Insert_agent_profiles.sql
-- Migration V37: Insert agent profiles for all agents
-- Uses consistent text values for bio, specialties, and service_areas

-- ============================================================================
-- Insert agent profiles with consistent text content
-- ============================================================================
INSERT INTO agent_profiles (
    user_id,
    bio,
    specialties,
    service_areas,
    rating,
    years_of_experience,
    properties_sold
)
SELECT 
    u.user_id,
    'Tôi là một chuyên viên bất động sản chuyên nghiệp với nhiều năm kinh nghiệm trong lĩnh vực mua bán, cho thuê và tư vấn bất động sản. Cam kết mang đến cho khách hàng dịch vụ tốt nhất với sự hiểu biết sâu sắc về thị trường địa phương và các xu hướng bất động sản hiện tại. Luôn đặt lợi ích của khách hàng lên hàng đầu và hỗ trợ tìm kiếm giải pháp bất động sản phù hợp nhất.' as bio,
    'Căn hộ chung cư, Nhà phố, Biệt thự, Đất nền, Bất động sản thương mại, Tư vấn đầu tư, Định giá bất động sản, Hỗ trợ vay vốn ngân hàng' as specialties,
    'TP. Hồ Chí Minh, Quận 1, Quận 2, Quận 3, Quận 7, Quận Bình Thạnh, Quận Phú Nhuận, Quận Gò Vấp, Quận Tân Bình, Quận Tân Phú, Huyện Bình Chánh, Huyện Nhà Bè' as service_areas,
    -- Generate realistic ratings between 3.5 and 5.0
    ROUND((3.5 + RANDOM() * 1.5)::numeric, 1) as rating,
    -- Generate years of experience between 2 and 15 years
    (2 + RANDOM() * 13)::integer as years_of_experience,
    -- Generate properties sold based on experience (5-50 properties per year of experience)
    ((2 + RANDOM() * 13) * (5 + RANDOM() * 45))::integer as properties_sold
FROM users u
JOIN user_roles ur ON u.user_id = ur.user_id
JOIN roles r ON ur.role_id = r.role_id
WHERE r.role_code = 'AGENT'
  AND u.deleted = FALSE
  AND u.user_id NOT IN (
      -- Exclude agents who already have profiles
      SELECT ap.user_id 
      FROM agent_profiles ap 
      WHERE ap.deleted = FALSE
  );