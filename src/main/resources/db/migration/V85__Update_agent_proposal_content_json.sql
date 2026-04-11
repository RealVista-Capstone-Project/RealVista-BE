-- V82: Update engagement content for AGENT_PROPOSAL and OWNER_INVITATION
-- Enriches AGENT_PROPOSAL with full proposal fields (title, pitchContent, commissionRate, experienceYears, priceRange)
-- Updates OWNER_INVITATION with title and longer, more realistic messages
-- Note: mediaUrls is resolved at API level from property_media table, not stored in content JSON

-- =============================================
-- AGENT_PROPOSAL updates (owner001 engagements from V81)
-- =============================================

-- Agent 004 - SUBMITTED
UPDATE engagements SET content = '{"title": "Chuyên gia Bất Động Sản Công Nghiệp", "message": "Tôi có nhiều kinh nghiệm với bất động sản công nghiệp, đặc biệt là khu vực kho bãi và nhà xưởng. Rất mong được hợp tác với bạn để tìm được đối tác phù hợp nhất cho tài sản của bạn.", "specialty": "320e8400-e29b-41d4-a716-446655440008", "pitchContent": "Với hơn 5 năm kinh nghiệm trong lĩnh vực bất động sản công nghiệp, tôi đã giúp hàng trăm khách hàng tìm được kho bãi và nhà xưởng phù hợp. Tôi cam kết mang lại kết quả tốt nhất cho tài sản của bạn.", "commissionRate": 2.5, "experienceYears": 5, "priceRange": {"rent": {"min": 200, "max": 500}, "sale": {"min": 5000, "max": 15000}}}'
WHERE engagement_id = 'e0000000-0081-0000-0000-000000000006';

-- Agent 005 - ACCEPTED
UPDATE engagements SET content = '{"title": "Chuyên gia Chuyển nhượng Shophouse & Mặt bằng Kinh doanh", "message": "Tôi có rất nhiều tệp khách hàng thích phân khúc của bạn, rất mong được hợp tác để đẩy nhanh bán nhà dùm bạn. Với mạng lưới khách hàng sẵn có, tôi tin rằng có thể giúp bạn chốt deal trong thời gian ngắn.", "specialty": "320e8400-e29b-41d4-a716-446655440008", "pitchContent": "Tôi am hiểu sâu sắc về lưu lượng giao thông, tiềm năng kinh doanh và quy hoạch tương lai tại các trục đường chính. Với kinh nghiệm tư vấn cho nhiều nhãn hàng chuỗi, tôi sẽ giúp bạn tìm được đối tác thuê hoặc người mua shophouse có tầm nhìn dài hạn, đảm bảo biên lợi nhuận kỳ vọng cao nhất.", "commissionRate": 2.5, "experienceYears": 7, "priceRange": {"rent": {"min": 123, "max": 456}, "sale": {"min": 7889, "max": 101112}}}'
WHERE engagement_id = 'e0000000-0081-0000-0000-000000000007';

-- Agent 006 - REJECTED
UPDATE engagements SET content = '{"title": "Môi giới Kho Bãi & Nhà Xưởng Chuyên Nghiệp", "message": "Tôi có giấy phép và kinh nghiệm dày dặn trong lĩnh vực bất động sản thương mại. Tôi sẵn sàng đánh giá tài sản và đưa ra phương án marketing tối ưu cho bạn.", "specialty": "320e8400-e29b-41d4-a716-446655440008", "pitchContent": "Với mạng lưới khách hàng rộng lớn trong ngành logistics và sản xuất, tôi có thể kết nối bạn với những đối tác tiềm năng nhất. Tôi sẽ phân tích thị trường, định giá chính xác và marketing hiệu quả.", "commissionRate": 3.5, "experienceYears": 10, "priceRange": {"rent": {"min": 150, "max": 600}, "sale": {"min": 3000, "max": 20000}}}'
WHERE engagement_id = 'e0000000-0081-0000-0000-000000000008';

-- Agent 009 - CANCELLED
UPDATE engagements SET content = '{"title": "Tư Vấn Đầu Tư Bất Động Sản Công Nghiệp", "message": "Chuyên gia với nhiều năm kinh nghiệm phục vụ các chủ bất động sản công nghiệp. Tôi luôn đặt lợi ích của khách hàng lên hàng đầu và cam kết minh bạch trong mọi giao dịch.", "specialty": "320e8400-e29b-41d4-a716-446655440008", "pitchContent": "Tôi chuyên tư vấn đầu tư và chuyển nhượng bất động sản công nghiệp. Với khả năng phân tích tài chính và thẩm định giá, tôi sẽ giúp bạn tối ưu hóa giá trị tài sản.", "commissionRate": 2.2, "experienceYears": 4, "priceRange": {"rent": {"min": 100, "max": 400}, "sale": {"min": 2000, "max": 10000}}}'
WHERE engagement_id = 'e0000000-0081-0000-0000-000000000009';

-- Agent 010 - FINISHED
UPDATE engagements SET content = '{"title": "Chuyên Viên Bán Bất Động Sản Công Nghiệp", "message": "Chuyên gia bán bất động sản công nghiệp với mạng lưới người mua rộng lớn. Tôi đã hoàn thành hàng chục giao dịch thành công trong phân khúc này và sẵn sàng hỗ trợ bạn.", "specialty": "320e8400-e29b-41d4-a716-446655440008", "pitchContent": "Tôi có mạng lưới vững chắc các nhà đầu tư và doanh nghiệp đang tìm kiếm bất động sản công nghiệp. Với chiến lược marketing đa kênh và khả năng đàm phán chuyên nghiệp, tôi đảm bảo bạn nhận được giá tốt nhất.", "commissionRate": 3.2, "experienceYears": 8, "priceRange": {"rent": {"min": 250, "max": 700}, "sale": {"min": 6000, "max": 18000}}}'
WHERE engagement_id = 'e0000000-0081-0000-0000-000000000010';

-- =============================================
-- OWNER_INVITATION updates (owner001 engagements from V81)
-- =============================================

-- Owner001 -> Agent 001 - ACCEPTED
UPDATE engagements SET content = '{"title": "Mời đại diện bán kho bãi công nghiệp", "message": "Mời bạn làm việc cho dự án kho bãi của chúng tôi. Tôi đã xem qua hồ sơ của bạn và rất ấn tượng với kinh nghiệm trong lĩnh vực bất động sản công nghiệp. Tôi sẽ offer hoa hồng tầm 2.5% cho căn kho của tôi nếu bạn bán được trong 2 tuần.", "offeredCommission": "2.5%"}'
WHERE engagement_id = 'e0000000-0081-0000-0000-000000000001';

-- Owner001 -> Agent 002 - SUBMITTED
UPDATE engagements SET content = '{"title": "Tìm agent bán đất công nghiệp gấp", "message": "Chào bạn, tôi có một lô đất công nghiệp cần bán gấp. Sau khi tìm hiểu trên hệ thống, tôi thấy bạn có nhiều kinh nghiệm với phân khúc này. Tôi sẵn sàng offer hoa hồng 3% nếu bạn có thể giúp tôi chốt deal trong vòng 1 tháng.", "offeredCommission": "3.0%"}'
WHERE engagement_id = 'e0000000-0081-0000-0000-000000000002';

-- Owner001 -> Agent 003 - REJECTED
UPDATE engagements SET content = '{"title": "Đại diện bán kho công nghiệp gần cảng", "message": "Tôi muốn mời bạn đại diện bán kho bãi công nghiệp của tôi. Đây là tài sản có vị trí đắc địa gần cảng và khu công nghiệp lớn. Hoa hồng offer là 2% trên giá bán.", "offeredCommission": "2.0%"}'
WHERE engagement_id = 'e0000000-0081-0000-0000-000000000003';

-- Owner001 -> Agent 007 - CANCELLED
UPDATE engagements SET content = '{"title": "Hợp đồng độc quyền bán đất công nghiệp", "message": "Xin chào, tôi đang tìm agent có kinh nghiệm để bán lô đất công nghiệp của mình. Tôi offer hoa hồng 3.5% và sẵn sàng ký hợp đồng độc quyền nếu bạn cam kết thời gian bán dưới 3 tháng.", "offeredCommission": "3.5%"}'
WHERE engagement_id = 'e0000000-0081-0000-0000-000000000004';

-- Owner001 -> Agent 008 - FINISHED
UPDATE engagements SET content = '{"title": "Bán kho bãi công nghiệp - có khách thuê sẵn", "message": "Mời bạn hợp tác bán kho bãi công nghiệp. Tài sản này đã có sẵn khách hỏi thuê nhưng tôi muốn bán dứt điểm. Hoa hồng 2.8% cho giao dịch thành công, có thể thương lượng thêm nếu bán được giá tốt.", "offeredCommission": "2.8%"}'
WHERE engagement_id = 'e0000000-0081-0000-0000-000000000005';
