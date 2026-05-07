-- V145__Lengthen_listing_content_with_richer_marketing_copy.sql
-- Reseed listings.content with much longer, realistic Vietnamese real-estate
-- marketing descriptions (multi-section, multi-paragraph) similar to popular
-- listing portals such as batdongsan.com.vn / chotot.com.
-- Compatible with both PostgreSQL and H2 databases.

UPDATE listings l
SET content = sub.long_content
FROM (
    SELECT
        l2.listing_id,
        CASE
            -- =====================================================================
            -- RESIDENTIAL - SALE (Apartment / House / Villa / Townhouse / Penthouse / Studio)
            -- =====================================================================
            WHEN pt.code IN ('APARTMENT', 'HOUSE', 'VILLA', 'TOWNHOUSE', 'PENTHOUSE', 'STUDIO')
                 AND l2.listing_type = 'SALE' THEN
                '★ ' || l2.name || ' - SỞ HỮU NGAY KHÔNG GIAN SỐNG ĐẲNG CẤP TẠI ' || loc.name || CHR(10) || CHR(10) ||
                'Chào mừng Quý khách đến với ' || l2.name || ', một sản phẩm bất động sản được chăm chút tỉ mỉ từ vị trí, thiết kế đến chất lượng hoàn thiện. Tọa lạc tại ' || p.street_address || ', ' || loc.name || ', dự án mang đến cho gia đình bạn một không gian sống lý tưởng, kết hợp hài hòa giữa sự tiện nghi hiện đại và phong cách thiết kế tinh tế.' || CHR(10) || CHR(10) ||
                'Với tổng diện tích sử dụng ' || COALESCE(p.usable_size_m2::text, 'thoáng rộng') || ' m², bố trí công năng thông minh, từng mét vuông đều được tận dụng tối đa, đáp ứng đầy đủ nhu cầu sinh hoạt cho gia đình từ 3 - 5 thành viên. Đây thực sự là cơ hội an cư lý tưởng và đồng thời là kênh đầu tư sinh lời bền vững giữa lòng TP. Hồ Chí Minh.' || CHR(10) || CHR(10) ||
                '✦ VỊ TRÍ ĐẮC ĐỊA - KẾT NỐI HOÀN HẢO' || CHR(10) ||
                '• Tọa lạc tại trung tâm khu dân cư hiện hữu, mật độ dân cư hợp lý, an ninh khu vực tốt.' || CHR(10) ||
                '• Di chuyển nhanh chóng đến trung tâm Quận 1 chỉ trong 15 - 20 phút qua các trục đường huyết mạch.' || CHR(10) ||
                '• Tiếp giáp hệ thống tiện ích ngoại khu hoàn chỉnh: trường học quốc tế, bệnh viện hạng A, siêu thị, ngân hàng, công viên cây xanh.' || CHR(10) ||
                '• Kết nối thuận tiện đến sân bay Tân Sơn Nhất, các tuyến Metro và cao tốc đang triển khai.' || CHR(10) || CHR(10) ||
                '✦ THIẾT KẾ THÔNG MINH - HOÀN THIỆN CAO CẤP' || CHR(10) ||
                '• Không gian mở, đón gió tự nhiên, ánh sáng dồi dào suốt cả ngày, view thoáng đãng.' || CHR(10) ||
                '• Phòng khách rộng rãi liên thông phòng bếp, lý tưởng cho những bữa tối ấm cúng và các buổi tiếp khách.' || CHR(10) ||
                '• Phòng ngủ master có cửa sổ lớn, khu vực thay đồ riêng biệt và nhà vệ sinh bên trong.' || CHR(10) ||
                '• Vật liệu hoàn thiện cao cấp: sàn gỗ tự nhiên / gạch granite cao cấp, thiết bị vệ sinh INAX/TOTO, kính cường lực Low-E, cửa nhôm Xingfa.' || CHR(10) ||
                '• Sẵn sàng các đường ống nước nóng / lạnh, hệ thống điện 3 pha và internet cáp quang tốc độ cao.' || CHR(10) || CHR(10) ||
                '✦ TIỆN ÍCH NỘI - NGOẠI KHU 5 SAO' || CHR(10) ||
                '• Hồ bơi, phòng gym, sân tennis, khu BBQ, công viên nội khu xanh mát quanh năm.' || CHR(10) ||
                '• Khu vui chơi trẻ em an toàn, tách biệt khỏi giao thông nội khu.' || CHR(10) ||
                '• An ninh 24/7 với hệ thống camera CCTV đa lớp, kiểm soát thẻ từ ra vào.' || CHR(10) ||
                '• Hầm để xe rộng rãi, có chỗ riêng cho xe ô tô và xe máy.' || CHR(10) || CHR(10) ||
                '✦ PHÁP LÝ MINH BẠCH - GIAO DỊCH AN TOÀN' || CHR(10) ||
                '• Sổ hồng riêng, pháp lý hoàn thiện 100%, chính chủ sang tên ngay.' || CHR(10) ||
                '• Hỗ trợ thủ tục công chứng, sang tên trọn gói, nhanh gọn trong vòng 7 - 10 ngày làm việc.' || CHR(10) ||
                '• Liên kết các ngân hàng lớn (Vietcombank, BIDV, Techcombank) hỗ trợ vay đến 70% giá trị căn nhà với lãi suất ưu đãi.' || CHR(10) || CHR(10) ||
                '✦ CƠ HỘI ĐẦU TƯ HẤP DẪN' || CHR(10) ||
                '• Khu vực có quy hoạch hạ tầng đồng bộ, tiềm năng tăng giá 15 - 20%/năm trong 3 - 5 năm tới.' || CHR(10) ||
                '• Dòng tiền cho thuê ổn định, lợi suất kỳ vọng 6 - 8%/năm nếu chuyển sang khai thác cho thuê.' || CHR(10) ||
                '• Quỹ căn còn lại hạn chế, nhiều khách đã đặt cọc - cơ hội cuối cùng cho nhà đầu tư nhanh tay.' || CHR(10) || CHR(10) ||
                'Liên hệ ngay với đội ngũ tư vấn của RealVista để được hỗ trợ xem nhà thực tế, kiểm tra pháp lý và tư vấn phương án vay phù hợp. Chúng tôi cam kết minh bạch giá, không phát sinh chi phí ẩn và đồng hành cùng bạn từ khâu xem nhà cho đến lúc nhận sổ hồng.'

            -- =====================================================================
            -- RESIDENTIAL - RENT
            -- =====================================================================
            WHEN pt.code IN ('APARTMENT', 'HOUSE', 'VILLA', 'TOWNHOUSE', 'PENTHOUSE', 'STUDIO')
                 AND l2.listing_type = 'RENT' THEN
                '★ ' || l2.name || ' - CHO THUÊ FULL NỘI THẤT, VÀO Ở NGAY' || CHR(10) || CHR(10) ||
                'RealVista trân trọng giới thiệu ' || l2.name || ' tại địa chỉ ' || p.street_address || ', ' || loc.name || ' - một không gian sống đáng mơ ước cho gia đình trẻ, chuyên gia nước ngoài hoặc khách thuê dài hạn. Bất động sản có tổng diện tích sử dụng ' || COALESCE(p.usable_size_m2::text, 'rộng rãi') || ' m², được setup đầy đủ nội thất cao cấp, bạn chỉ cần xách vali đến ở.' || CHR(10) || CHR(10) ||
                '✦ VỊ TRÍ THUẬN TIỆN' || CHR(10) ||
                '• Mặt tiền đường lớn, taxi - xe công nghệ ra vào dễ dàng 24/7.' || CHR(10) ||
                '• Cách trung tâm Quận 1 chỉ 10 - 15 phút di chuyển, gần các tòa nhà văn phòng hạng A.' || CHR(10) ||
                '• Xung quanh có siêu thị, cafe, nhà hàng, phòng tập gym, công viên - mọi nhu cầu hằng ngày đều đáp ứng trong bán kính 500m.' || CHR(10) || CHR(10) ||
                '✦ THIẾT KẾ HIỆN ĐẠI - NỘI THẤT ĐẦY ĐỦ' || CHR(10) ||
                '• Bố trí thông minh, ánh sáng tự nhiên dồi dào, view thoáng không bị che khuất.' || CHR(10) ||
                '• Đầy đủ nội thất cơ bản: giường, nệm, tủ quần áo, sofa, bàn ăn, kệ tivi.' || CHR(10) ||
                '• Thiết bị điện tử: máy lạnh inverter, máy giặt, tủ lạnh, máy nước nóng, bếp từ, máy hút mùi.' || CHR(10) ||
                '• Internet wifi tốc độ cao, truyền hình cáp đã được lắp đặt sẵn.' || CHR(10) || CHR(10) ||
                '✦ TIỆN ÍCH ĐI KÈM' || CHR(10) ||
                '• Hồ bơi, phòng gym, sky lounge, khu BBQ, công viên nội khu (đối với chung cư).' || CHR(10) ||
                '• An ninh 24/7, camera giám sát, lễ tân hỗ trợ nhận hàng và đặt lịch dịch vụ.' || CHR(10) ||
                '• Bãi giữ xe rộng, miễn phí cho 1 xe máy hoặc giảm 50% phí giữ xe ô tô.' || CHR(10) ||
                '• Có dịch vụ vệ sinh định kỳ, sửa chữa tận nơi, hỗ trợ song ngữ Anh - Việt.' || CHR(10) || CHR(10) ||
                '✦ ĐIỀU KHOẢN THUÊ LINH HOẠT' || CHR(10) ||
                '• Hợp đồng thuê tối thiểu 06 tháng, ưu tiên khách thuê dài hạn 12 tháng trở lên.' || CHR(10) ||
                '• Đặt cọc 02 tháng, thanh toán hằng tháng hoặc 03 tháng tùy nhu cầu.' || CHR(10) ||
                '• Hỗ trợ làm hợp đồng song ngữ, đăng ký tạm trú miễn phí cho người nước ngoài.' || CHR(10) ||
                '• Phí dịch vụ, điện nước, internet thanh toán theo số tiêu thụ thực tế.' || CHR(10) || CHR(10) ||
                'Liên hệ ngay để được sắp xếp lịch xem nhà 24/7. Phòng còn 1-2 căn cuối cùng - book sớm để có ưu đãi giảm giá tháng đầu và tặng gói vệ sinh chuyên sâu trị giá 2 triệu đồng.'

            -- =====================================================================
            -- COMMERCIAL - SALE (Office, Shophouse, Retail, Mall, Restaurant, Hotel)
            -- =====================================================================
            WHEN pt.code IN ('OFFICE', 'SHOPHOUSE', 'RETAIL', 'MALL', 'RESTAURANT', 'HOTEL')
                 AND l2.listing_type = 'SALE' THEN
                '★ ' || l2.name || ' - CHÍNH CHỦ BÁN MẶT BẰNG KINH DOANH ĐẮC ĐỊA TẠI ' || loc.name || CHR(10) || CHR(10) ||
                'Cơ hội đầu tư hiếm có cho doanh nhân và nhà đầu tư bất động sản thương mại. ' || l2.name || ' tọa lạc tại ' || p.street_address || ', ' || loc.name || ' - khu vực có lưu lượng người qua lại đông đúc, tệp khách hàng tiềm năng dồi dào và mật độ kinh doanh sầm uất bậc nhất khu vực.' || CHR(10) || CHR(10) ||
                'Tổng diện tích sử dụng lên đến ' || COALESCE(p.usable_size_m2::text, 'thoáng rộng') || ' m², thiết kế linh hoạt, phù hợp với đa dạng mô hình kinh doanh: showroom, văn phòng đại diện, F&B, spa, ngân hàng, cửa hàng tiện lợi, học viện, trung tâm thương mại mini.' || CHR(10) || CHR(10) ||
                '✦ ƯU ĐIỂM VỊ TRÍ' || CHR(10) ||
                '• Mặt tiền đường lớn, vỉa hè rộng, dễ dàng đậu xe ô tô và xe máy.' || CHR(10) ||
                '• Khu trung tâm thương mại - tài chính, dân cư đông đúc, thu nhập trung bình - cao.' || CHR(10) ||
                '• Gần ngã tư huyết mạch, lưu lượng giao thông qua lại trên 30.000 lượt/ngày.' || CHR(10) ||
                '• Xung quanh là các thương hiệu lớn: Highlands, The Coffee House, Circle K, Vinmart, ngân hàng, đảm bảo nguồn khách dồi dào.' || CHR(10) || CHR(10) ||
                '✦ KẾT CẤU CÔNG TRÌNH' || CHR(10) ||
                '• Kết cấu bê tông cốt thép kiên cố, móng cọc khoan nhồi, chịu lực tốt.' || CHR(10) ||
                '• Thiết kế nhiều tầng (trệt + lửng + lầu), tối ưu cho việc phân chia khu vực kinh doanh và lưu trữ.' || CHR(10) ||
                '• Hệ thống điện 3 pha, máy phát điện dự phòng, cấp thoát nước đầy đủ.' || CHR(10) ||
                '• Đã có giấy phép kinh doanh, PCCC đạt chuẩn, sẵn sàng đi vào hoạt động.' || CHR(10) || CHR(10) ||
                '✦ TIỀM NĂNG SINH LỜI' || CHR(10) ||
                '• Giá thuê thị trường khu vực dao động 80 - 150 triệu đồng/tháng, lợi suất kỳ vọng 7 - 9%/năm.' || CHR(10) ||
                '• Khu vực đang được quy hoạch mở rộng đường, xây dựng metro - cú hích tăng giá 20 - 30% trong 2 năm tới.' || CHR(10) ||
                '• Phù hợp cả mục đích tự kinh doanh lẫn đầu tư cho thuê dài hạn.' || CHR(10) || CHR(10) ||
                '✦ PHÁP LÝ - HỖ TRỢ TÀI CHÍNH' || CHR(10) ||
                '• Sổ hồng riêng đầy đủ, pháp lý sạch, không tranh chấp, không quy hoạch.' || CHR(10) ||
                '• Hỗ trợ vay ngân hàng tới 70% giá trị, lãi suất ưu đãi từ 8.5%/năm.' || CHR(10) ||
                '• Hợp đồng công chứng nhanh, sang tên trong vòng 7 ngày làm việc.' || CHR(10) || CHR(10) ||
                'Liên hệ ngay với chuyên viên RealVista để khảo sát thực tế, xem hồ sơ pháp lý và tư vấn phương án đầu tư tối ưu. Đây là cơ hội duy nhất, không nên bỏ lỡ!'

            -- =====================================================================
            -- COMMERCIAL - RENT
            -- =====================================================================
            WHEN pt.code IN ('OFFICE', 'SHOPHOUSE', 'RETAIL', 'MALL', 'RESTAURANT', 'HOTEL')
                 AND l2.listing_type = 'RENT' THEN
                '★ ' || l2.name || ' - CHO THUÊ MẶT BẰNG KINH DOANH GIÁ TỐT TẠI ' || loc.name || CHR(10) || CHR(10) ||
                'RealVista giới thiệu mặt bằng cho thuê chiến lược tại ' || p.street_address || ', ' || loc.name || '. ' || l2.name || ' sở hữu vị trí "vàng" với mặt tiền rộng, lưu lượng khách hàng dồi dào và tệp dân cư có thu nhập ổn định - lý tưởng cho mọi mô hình kinh doanh F&B, bán lẻ, dịch vụ chuyên môn.' || CHR(10) || CHR(10) ||
                '✦ THÔNG TIN MẶT BẰNG' || CHR(10) ||
                '• Tổng diện tích sử dụng: ' || COALESCE(p.usable_size_m2::text, 'rộng rãi') || ' m², không gian mở dễ dàng setup theo nhu cầu doanh nghiệp.' || CHR(10) ||
                '• Mặt tiền lớn, biển hiệu nổi bật, lý tưởng cho thương hiệu cần độ nhận diện cao.' || CHR(10) ||
                '• Trần cao trên 3.5m, sàn phẳng, tải trọng tốt, có hệ thống điều hòa âm trần.' || CHR(10) ||
                '• Nhà vệ sinh, kho phụ, khu pantry đã được xây dựng sẵn.' || CHR(10) || CHR(10) ||
                '✦ HẠ TẦNG - TIỆN ÍCH' || CHR(10) ||
                '• Điện 3 pha công suất lớn, đường truyền internet cáp quang tốc độ cao.' || CHR(10) ||
                '• Hệ thống PCCC đạt chuẩn, camera an ninh 24/7, máy phát điện dự phòng.' || CHR(10) ||
                '• Khu vực để xe rộng cho khách hàng, có nhân viên hỗ trợ trông xe.' || CHR(10) ||
                '• Liền kề các thương hiệu nổi tiếng, hỗ trợ thu hút khách chéo (cross-traffic).' || CHR(10) || CHR(10) ||
                '✦ ĐIỀU KIỆN THUÊ' || CHR(10) ||
                '• Thời hạn thuê tối thiểu 02 năm, thanh toán linh hoạt theo quý hoặc theo tháng.' || CHR(10) ||
                '• Đặt cọc 03 tháng tiền thuê, miễn phí 30 ngày đầu để cải tạo (đối với hợp đồng từ 03 năm).' || CHR(10) ||
                '• Hỗ trợ thủ tục đăng ký kinh doanh, biển hiệu, đấu nối điện nước.' || CHR(10) ||
                '• Chấp nhận mọi ngành nghề hợp pháp (trừ kinh doanh nhạy cảm).' || CHR(10) || CHR(10) ||
                'Liên hệ trực tiếp để xem mặt bằng, thương lượng giá và ký hợp đồng. Mặt bằng đẹp - khách thuê đông - book ngay hôm nay!'

            -- =====================================================================
            -- INDUSTRIAL (Warehouse / Factory / Workshop / Logistics) - SALE/RENT
            -- =====================================================================
            WHEN pt.code IN ('WAREHOUSE', 'FACTORY', 'WORKSHOP', 'LOGISTICS') THEN
                '★ ' || l2.name || ' - ' || (CASE WHEN l2.listing_type = 'SALE' THEN 'CHUYỂN NHƯỢNG' ELSE 'CHO THUÊ' END) ||
                ' KHO XƯỞNG TIÊU CHUẨN CÔNG NGHIỆP TẠI ' || loc.name || CHR(10) || CHR(10) ||
                'RealVista giới thiệu giải pháp mặt bằng công nghiệp cho doanh nghiệp sản xuất, logistics và thương mại điện tử. ' || l2.name || ' tọa lạc tại ' || p.street_address || ', ' || loc.name || ' - vị trí chiến lược kết nối nhanh đến cảng Cát Lái, ICD Phước Long, sân bay Tân Sơn Nhất và các tuyến cao tốc trọng điểm.' || CHR(10) || CHR(10) ||
                '✦ THÔNG SỐ KỸ THUẬT' || CHR(10) ||
                '• Tổng diện tích sử dụng: ' || COALESCE(p.usable_size_m2::text, 'lớn') || ' m², khuôn viên đất ' || COALESCE(p.land_size_m2::text, 'rộng') || ' m².' || CHR(10) ||
                '• Chiều cao thông thủy 8 - 12m, thuận tiện cho việc lắp đặt giá kệ kho cao tầng.' || CHR(10) ||
                '• Sàn nền bê tông mài, tải trọng 3 - 5 tấn/m², chịu lực xe nâng và các thiết bị hạng nặng.' || CHR(10) ||
                '• Mái lợp tôn cách nhiệt, hệ thống thông gió và chiếu sáng tự nhiên.' || CHR(10) || CHR(10) ||
                '✦ HẠ TẦNG ĐỒNG BỘ' || CHR(10) ||
                '• Điện 3 pha công suất 250 - 500 kVA, có sẵn trạm biến áp riêng.' || CHR(10) ||
                '• Hệ thống cấp thoát nước, xử lý nước thải đạt chuẩn quy định.' || CHR(10) ||
                '• Đường nội bộ rộng 12 - 16m, container 40ft ra vào và quay đầu dễ dàng.' || CHR(10) ||
                '• Khu văn phòng quản lý, nhà ăn cho công nhân, bãi đậu xe nhân viên đầy đủ.' || CHR(10) ||
                '• An ninh 24/7, hệ thống PCCC đạt chuẩn QCVN, camera giám sát toàn khu.' || CHR(10) || CHR(10) ||
                '✦ VỊ TRÍ KẾT NỐI' || CHR(10) ||
                '• Kết nối nhanh đến cảng Cát Lái (15 - 25 km), QL1A, QL51 và các tuyến cao tốc.' || CHR(10) ||
                '• Gần khu công nghiệp lân cận, thuận lợi cho việc tuyển dụng nhân lực.' || CHR(10) ||
                '• Khu vực có quy hoạch hạ tầng đồng bộ, không ngập lụt, không quy hoạch giải tỏa.' || CHR(10) || CHR(10) ||
                '✦ PHÁP LÝ - HỢP ĐỒNG' || CHR(10) ||
                '• Đầy đủ giấy chứng nhận quyền sử dụng đất, giấy phép xây dựng, PCCC, môi trường.' || CHR(10) ||
                '• Đối với cho thuê: hợp đồng tối thiểu 03 năm, đặt cọc 03 - 06 tháng, hỗ trợ điều khoản gia hạn.' || CHR(10) ||
                '• Đối với chuyển nhượng: pháp lý hoàn thiện, hỗ trợ vay ngân hàng tới 60% giá trị.' || CHR(10) ||
                '• Hỗ trợ thủ tục đăng ký kinh doanh, môi trường, an toàn lao động.' || CHR(10) || CHR(10) ||
                'Liên hệ ngay để khảo sát thực địa và nhận báo giá chi tiết. RealVista sở hữu hệ sinh thái BĐS công nghiệp lớn nhất khu vực phía Nam - đảm bảo tìm được phương án tối ưu cho doanh nghiệp của bạn.'

            -- =====================================================================
            -- LAND (Residential / Commercial / Industrial / Agricultural)
            -- =====================================================================
            ELSE
                '★ ' || l2.name || ' - ĐẦU TƯ ĐẤT NỀN PHÁP LÝ MINH BẠCH TẠI ' || loc.name || CHR(10) || CHR(10) ||
                'Cơ hội sở hữu mảnh đất "vàng" tại ' || p.street_address || ', ' || loc.name || '. ' || l2.name || ' là sản phẩm đầu tư đáng giá cho cả mục đích an cư lập nghiệp và đầu tư sinh lời dài hạn. Khu vực đang trong giai đoạn phát triển mạnh về hạ tầng, giao thông và tiện ích đô thị - thời điểm vàng để xuống tiền trước khi giá tăng theo quy hoạch.' || CHR(10) || CHR(10) ||
                '✦ THÔNG TIN LÔ ĐẤT' || CHR(10) ||
                '• Tổng diện tích đất: ' || COALESCE(p.land_size_m2::text, 'thoáng rộng') || ' m², thế đất bằng phẳng, không sụt lún, không ngập nước.' || CHR(10) ||
                '• Chiều ngang ' || COALESCE(p.width_m::text, 'lớn') || ' m, chiều dài ' || COALESCE(p.length_m::text, 'lý tưởng') || ' m - tỷ lệ đẹp, dễ xây dựng tận dụng tối đa.' || CHR(10) ||
                '• Hướng đất hợp phong thủy, đón gió và ánh sáng tự nhiên tốt.' || CHR(10) ||
                '• Đường trước đất rộng 8 - 12m, ô tô tránh nhau thoải mái.' || CHR(10) || CHR(10) ||
                '✦ TIỆN ÍCH XUNG QUANH' || CHR(10) ||
                '• Bao quanh là các khu dân cư hiện hữu, an ninh tốt, hàng xóm văn minh.' || CHR(10) ||
                '• Cách trung tâm hành chính khu vực chỉ 10 - 15 phút di chuyển.' || CHR(10) ||
                '• Gần trường học các cấp, chợ truyền thống, siêu thị, trạm y tế, công viên.' || CHR(10) ||
                '• Hạ tầng điện, nước, internet đã được kéo đến chân đất, sẵn sàng xây dựng.' || CHR(10) || CHR(10) ||
                '✦ TIỀM NĂNG TĂNG GIÁ' || CHR(10) ||
                '• Khu vực đang được quy hoạch mở rộng đường giao thông, xây dựng cầu vượt và metro.' || CHR(10) ||
                '• Mức tăng giá đất khu vực trung bình 12 - 18%/năm trong 3 năm gần đây.' || CHR(10) ||
                '• Quỹ đất khu vực ngày càng khan hiếm - sản phẩm hữu hạn, cơ hội duy nhất.' || CHR(10) ||
                '• Phù hợp xây dựng nhà ở gia đình, nhà cho thuê, hoặc đầu tư phân lô bán nền (đối với lô lớn).' || CHR(10) || CHR(10) ||
                '✦ PHÁP LÝ HOÀN HẢO' || CHR(10) ||
                '• Sổ hồng / sổ đỏ riêng từng lô, đầy đủ thổ cư, không quy hoạch, không tranh chấp.' || CHR(10) ||
                '• Chính chủ đứng tên, hỗ trợ công chứng sang tên trong ngày.' || CHR(10) ||
                '• Hỗ trợ vay ngân hàng tới 60% giá trị, lãi suất ưu đãi cho khách hàng có nhu cầu thực.' || CHR(10) ||
                '• Cam kết hoàn cọc 100% nếu phát hiện sai lệch về pháp lý.' || CHR(10) || CHR(10) ||
                'Liên hệ ngay để được khảo sát thực tế lô đất, kiểm tra hồ sơ pháp lý và tư vấn phương án đầu tư phù hợp. Cam kết giá tốt nhất khu vực, không phát sinh chi phí ẩn. RealVista đồng hành cùng bạn trên hành trình đầu tư bền vững.'
        END AS long_content
    FROM listings l2
    JOIN properties p ON l2.property_id = p.property_id
    JOIN locations loc ON p.location_id = loc.location_id
    JOIN property_types pt ON p.property_type_id = pt.property_type_id
) sub
WHERE l.listing_id = sub.listing_id;
