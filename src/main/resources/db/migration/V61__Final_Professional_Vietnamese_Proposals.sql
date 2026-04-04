-- Consolidating agent proposals seed with high-quality Vietnamese content
-- This replaces V59 and V60 logic with a pristine set of 6 professional templates

INSERT INTO agent_proposals (
    agent_proposal_id, 
    user_id, 
    title, 
    pitch_content, 
    commission_rate, 
    experience_years, 
    status, 
    created_at, 
    updated_at
) VALUES 
-- 1. High-end Apartment Specialist
(
    'a1b2c3d4-e5f6-4a5b-acad-f1e2d3c4b5a6', 
    (SELECT user_id FROM users WHERE email = 'agent001@realvista.com'), 
    'Chuyên gia Phân khúc Căn hộ Cao cấp & Luxury', 
    'Tôi sở hữu mạng lưới khách hàng thượng lưu rộng khắp và chiến lược marketing số hóa tiên tiến. Bằng cách sử dụng công nghệ VR 360 độ và quay phim Cinematic, tôi cam kết nâng tầm giá trị bất động sản của bạn lên mức cao nhất, đảm bảo thu hút đúng tệp khách hàng mục tiêu trong thời gian ngắn nhất.', 
    1.5, 
    8, 
    'ACTIVE', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
),
-- 2. Fast Liquidity Strategy
(
    'b2c3d4e5-f6a7-5b6c-bcbd-e2d3c4b5a6f7', 
    (SELECT user_id FROM users WHERE email = 'agent001@realvista.com'), 
    'Chiến lược "Thanh khoản Vàng" - Cam kết 25 ngày', 
    'Nếu tốc độ là ưu tiên hàng đầu, chiến lược này được thiết kế dành cho bạn. Tôi áp dụng mô hình định giá Real-time Market và tổ chức sự kiện Open-house quy mô lớn để tạo hiệu ứng khan hiếm. Trung bình các căn hộ tôi quản lý đều được chốt giao dịch sớm hơn thị trường 15 ngày với tỉ lệ ép giá dưới 2%.', 
    2.2, 
    6, 
    'ACTIVE', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
),
-- 3. Yield & Smart-Home Advisory
(
    'c3d4e5f6-a7b8-6c7d-cdce-d3c4b5a6f7e2', 
    (SELECT user_id FROM users WHERE email = 'agent001@realvista.com'), 
    'Tư vấn Khai thác Dòng tiền & Vận hành Smart-Home', 
    'Dành riêng cho các nhà đầu tư chú trọng dòng tiền bền vững. Tôi không chỉ môi giới mà còn hỗ trợ tư vấn nâng cấp nội thất theo xu hướng Smart-life để tăng giá trị cho thuê lên 20-30%. Tôi sẽ đồng hành cùng bạn từ khâu tìm khách thuê chất lượng đến khi bàn giao quản lý chuyên nghiệp.', 
    1.2, 
    5, 
    'ACTIVE', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
),
-- 4. Ultra-Luxury Villa Brokering
(
    'd4e5f6a7-b8c9-7d8e-deef-c4b5a6f7e2d3', 
    (SELECT user_id FROM users WHERE email = 'agent001@realvista.com'), 
    'Quản lý Tài sản & Môi giới Biệt thự Thượng lưu', 
    'Sự tinh tế, bảo mật và chuyên nghiệp là tôn chỉ của tôi khi làm việc với các bất động sản hàng tỷ đồng. Tôi không quảng cáo đại trà mà tiếp cận qua các kênh networking nội bộ và hội nhóm doanh nhân. Mỗi căn biệt thự sẽ được tôi kể một câu chuyện riêng biệt để chạm đến cảm xúc của chủ nhân tương lai.', 
    3.0, 
    12, 
    'ACTIVE', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
),
-- 5. Multi-channel Digital Marketing Specialist
(
    'e5f6a7b8-c9d0-8e9f-ef01-b5a6f7e2d3c4', 
    (SELECT user_id FROM users WHERE email = 'agent001@realvista.com'), 
    'Chiến lược Tiếp thị Đa kênh & Phủ sóng Social Media', 
    'Trong kỷ nguyên số, ai nắm bắt được sự chú ý sẽ chiến thắng. Tôi đầu tư ngân sách lớn vào quảng cáo Facebook, TikTok và Google cho mỗi bài đăng của khách hàng. Hình ảnh lung linh, nội dung Viral là cam kết của tôi để căn hộ của bạn luôn đứng Top đầu tìm kiếm và nhận được hàng chục cuộc gọi mỗi tuần.', 
    2.0, 
    4, 
    'ACTIVE', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
),
-- 6. Commercial & Shophouse Specialist
(
    'f6a7b8c9-d0e1-9f01-f012-a6f7e2d3c4b5', 
    (SELECT user_id FROM users WHERE email = 'agent001@realvista.com'), 
    'Chuyên gia Chuyển nhượng Shophouse & Mặt bằng Kinh doanh', 
    'Tôi am hiểu sâu sắc về lưu lượng giao thông, tiềm năng kinh doanh và quy hoạch tương lai tại các trục đường chính. Với kinh nghiệm tư vấn cho nhiều nhãn hàng chuỗi, tôi sẽ giúp bạn tìm được đối tác thuê hoặc người mua shophouse có tầm nhìn dài hạn, đảm bảo biên lợi nhuận kỳ vọng cao nhất.', 
    2.5, 
    7, 
    'ACTIVE', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
)
;
