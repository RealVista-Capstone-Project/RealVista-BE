-- V146__Reseed_listing_content_with_markdown.sql
-- Re-seed listings.content with shorter, Markdown-formatted Vietnamese
-- real-estate marketing copy. Headings use Markdown so the frontend can
-- render them as bold/heading styles via react-markdown + prose.
-- Compatible with both PostgreSQL and H2 databases.

UPDATE listings l
SET content = sub.long_content
FROM (
    SELECT
        l2.listing_id,
        CASE
            -- =====================================================================
            -- RESIDENTIAL - SALE
            -- =====================================================================
            WHEN pt.code IN ('APARTMENT', 'HOUSE', 'VILLA', 'TOWNHOUSE', 'PENTHOUSE', 'STUDIO')
                 AND l2.listing_type = 'SALE' THEN
                '**' || l2.name || '** — Sở hữu không gian sống đẳng cấp tại ' || loc.name || '.' || CHR(10) || CHR(10) ||
                'Tọa lạc tại **' || p.street_address || '**, ' || loc.name || ', dự án mang đến không gian sống lý tưởng cho gia đình bạn. Tổng diện tích sử dụng **' || COALESCE(p.usable_size_m2::text, 'thoáng rộng') || ' m²**, bố trí công năng thông minh, tận dụng tối đa từng mét vuông.' || CHR(10) || CHR(10) ||
                '## Vị trí đắc địa' || CHR(10) ||
                '- Trung tâm khu dân cư hiện hữu, an ninh tốt, tiện ích đầy đủ.' || CHR(10) ||
                '- Di chuyển vào trung tâm Quận 1 chỉ 15 - 20 phút qua các trục đường huyết mạch.' || CHR(10) ||
                '- Gần trường học quốc tế, bệnh viện, siêu thị, công viên cây xanh.' || CHR(10) || CHR(10) ||
                '## Thiết kế & Hoàn thiện' || CHR(10) ||
                '- Không gian mở, đón gió và ánh sáng tự nhiên, view thoáng đãng.' || CHR(10) ||
                '- Vật liệu cao cấp: sàn gỗ tự nhiên, thiết bị vệ sinh INAX/TOTO, kính Low-E.' || CHR(10) ||
                '- Sẵn sàng dọn vào ở, đầy đủ các đầu chờ điện/nước/internet tốc độ cao.' || CHR(10) || CHR(10) ||
                '## Tiện ích nội khu' || CHR(10) ||
                '- Hồ bơi, gym, sân tennis, công viên, khu BBQ, khu vui chơi trẻ em.' || CHR(10) ||
                '- An ninh 24/7 với camera CCTV, kiểm soát thẻ từ ra vào.' || CHR(10) ||
                '- Hầm để xe rộng rãi cho ô tô và xe máy.' || CHR(10) || CHR(10) ||
                '## Pháp lý & Tài chính' || CHR(10) ||
                '- Sổ hồng riêng, pháp lý hoàn thiện 100%, sang tên trong 7 - 10 ngày.' || CHR(10) ||
                '- Hỗ trợ vay ngân hàng tới **70% giá trị**, lãi suất ưu đãi.' || CHR(10) ||
                '- Tiềm năng tăng giá 15 - 20%/năm, lợi suất cho thuê 6 - 8%/năm.' || CHR(10) || CHR(10) ||
                'Liên hệ ngay với RealVista để xem nhà thực tế và tư vấn phương án vay tối ưu.'

            -- =====================================================================
            -- RESIDENTIAL - RENT
            -- =====================================================================
            WHEN pt.code IN ('APARTMENT', 'HOUSE', 'VILLA', 'TOWNHOUSE', 'PENTHOUSE', 'STUDIO')
                 AND l2.listing_type = 'RENT' THEN
                '**' || l2.name || '** — Cho thuê full nội thất, vào ở ngay tại ' || loc.name || '.' || CHR(10) || CHR(10) ||
                'Địa chỉ: **' || p.street_address || '**, ' || loc.name || '. Diện tích sử dụng **' || COALESCE(p.usable_size_m2::text, 'rộng rãi') || ' m²**, đã setup đầy đủ nội thất cao cấp — chỉ cần xách vali đến ở.' || CHR(10) || CHR(10) ||
                '## Vị trí thuận tiện' || CHR(10) ||
                '- Mặt tiền lớn, taxi và xe công nghệ ra vào dễ dàng 24/7.' || CHR(10) ||
                '- Cách trung tâm Quận 1 chỉ 10 - 15 phút, gần các tòa nhà văn phòng hạng A.' || CHR(10) ||
                '- Bán kính 500m đầy đủ siêu thị, cafe, gym, công viên.' || CHR(10) || CHR(10) ||
                '## Nội thất & Tiện ích' || CHR(10) ||
                '- Đầy đủ giường, tủ, sofa, bàn ăn, kệ tivi, máy lạnh inverter, máy giặt, tủ lạnh.' || CHR(10) ||
                '- Internet wifi tốc độ cao và truyền hình cáp đã lắp sẵn.' || CHR(10) ||
                '- Hồ bơi, gym, sky lounge, công viên nội khu (đối với chung cư).' || CHR(10) || CHR(10) ||
                '## Điều khoản thuê' || CHR(10) ||
                '- Hợp đồng tối thiểu 06 tháng, đặt cọc 02 tháng.' || CHR(10) ||
                '- Hỗ trợ hợp đồng song ngữ và đăng ký tạm trú miễn phí.' || CHR(10) ||
                '- Phí dịch vụ, điện nước thanh toán theo số tiêu thụ thực tế.' || CHR(10) || CHR(10) ||
                'Liên hệ để sắp xếp lịch xem nhà 24/7 — book sớm nhận ưu đãi tháng đầu.'

            -- =====================================================================
            -- COMMERCIAL - SALE
            -- =====================================================================
            WHEN pt.code IN ('OFFICE', 'SHOPHOUSE', 'RETAIL', 'MALL', 'RESTAURANT', 'HOTEL')
                 AND l2.listing_type = 'SALE' THEN
                '**' || l2.name || '** — Mặt bằng kinh doanh đắc địa tại ' || loc.name || '.' || CHR(10) || CHR(10) ||
                'Địa chỉ **' || p.street_address || '**, ' || loc.name || '. Diện tích sử dụng **' || COALESCE(p.usable_size_m2::text, 'thoáng rộng') || ' m²**, lưu lượng khách hàng dồi dào, phù hợp đa dạng mô hình kinh doanh: F&B, showroom, ngân hàng, văn phòng đại diện.' || CHR(10) || CHR(10) ||
                '## Ưu điểm vị trí' || CHR(10) ||
                '- Mặt tiền đường lớn, vỉa hè rộng, dễ đậu xe ô tô và xe máy.' || CHR(10) ||
                '- Khu trung tâm thương mại, dân cư đông, thu nhập trung bình - cao.' || CHR(10) ||
                '- Lưu lượng giao thông trên **30.000 lượt/ngày**, kế bên các thương hiệu lớn.' || CHR(10) || CHR(10) ||
                '## Kết cấu công trình' || CHR(10) ||
                '- Bê tông cốt thép kiên cố, móng cọc khoan nhồi, nhiều tầng linh hoạt.' || CHR(10) ||
                '- Điện 3 pha, máy phát điện dự phòng, cấp thoát nước đầy đủ.' || CHR(10) ||
                '- Đã có giấy phép kinh doanh và PCCC đạt chuẩn, sẵn sàng vận hành.' || CHR(10) || CHR(10) ||
                '## Pháp lý & Tài chính' || CHR(10) ||
                '- Sổ hồng riêng, pháp lý sạch, không tranh chấp, không quy hoạch.' || CHR(10) ||
                '- Hỗ trợ vay ngân hàng **tới 70% giá trị**, lãi suất từ 8.5%/năm.' || CHR(10) ||
                '- Lợi suất kỳ vọng 7 - 9%/năm, tiềm năng tăng giá 20 - 30% trong 2 năm.' || CHR(10) || CHR(10) ||
                'Liên hệ chuyên viên RealVista để khảo sát thực tế và tư vấn phương án đầu tư.'

            -- =====================================================================
            -- COMMERCIAL - RENT
            -- =====================================================================
            WHEN pt.code IN ('OFFICE', 'SHOPHOUSE', 'RETAIL', 'MALL', 'RESTAURANT', 'HOTEL')
                 AND l2.listing_type = 'RENT' THEN
                '**' || l2.name || '** — Cho thuê mặt bằng kinh doanh giá tốt tại ' || loc.name || '.' || CHR(10) || CHR(10) ||
                'Địa chỉ **' || p.street_address || '**, ' || loc.name || '. Diện tích sử dụng **' || COALESCE(p.usable_size_m2::text, 'rộng rãi') || ' m²**, không gian mở dễ dàng setup theo nhu cầu doanh nghiệp.' || CHR(10) || CHR(10) ||
                '## Thông tin mặt bằng' || CHR(10) ||
                '- Mặt tiền lớn, biển hiệu nổi bật, lý tưởng cho thương hiệu cần độ nhận diện cao.' || CHR(10) ||
                '- Trần cao trên 3.5m, sàn phẳng, có hệ thống điều hòa âm trần.' || CHR(10) ||
                '- Nhà vệ sinh, kho phụ và khu pantry đã xây sẵn.' || CHR(10) || CHR(10) ||
                '## Hạ tầng & Tiện ích' || CHR(10) ||
                '- Điện 3 pha công suất lớn, internet cáp quang tốc độ cao.' || CHR(10) ||
                '- PCCC đạt chuẩn, camera 24/7, máy phát điện dự phòng.' || CHR(10) ||
                '- Khu vực để xe rộng cho khách hàng, có nhân viên hỗ trợ.' || CHR(10) || CHR(10) ||
                '## Điều kiện thuê' || CHR(10) ||
                '- Thuê tối thiểu 02 năm, thanh toán theo tháng hoặc theo quý.' || CHR(10) ||
                '- Đặt cọc 03 tháng, **miễn phí 30 ngày đầu** để cải tạo (hợp đồng từ 03 năm).' || CHR(10) ||
                '- Hỗ trợ thủ tục đăng ký kinh doanh, biển hiệu, đấu nối điện nước.' || CHR(10) || CHR(10) ||
                'Liên hệ trực tiếp để xem mặt bằng và thương lượng giá tốt nhất.'

            -- =====================================================================
            -- INDUSTRIAL
            -- =====================================================================
            WHEN pt.code IN ('WAREHOUSE', 'FACTORY', 'WORKSHOP', 'LOGISTICS') THEN
                '**' || l2.name || '** — ' ||
                (CASE WHEN l2.listing_type = 'SALE' THEN 'Chuyển nhượng' ELSE 'Cho thuê' END) ||
                ' kho xưởng tiêu chuẩn công nghiệp tại ' || loc.name || '.' || CHR(10) || CHR(10) ||
                'Địa chỉ **' || p.street_address || '**, ' || loc.name || '. Tổng diện tích sử dụng **' || COALESCE(p.usable_size_m2::text, 'lớn') || ' m²** trên khuôn viên đất **' || COALESCE(p.land_size_m2::text, 'rộng') || ' m²**. Vị trí chiến lược kết nối nhanh đến cảng Cát Lái và các tuyến cao tốc.' || CHR(10) || CHR(10) ||
                '## Thông số kỹ thuật' || CHR(10) ||
                '- Thông thủy 8 - 12m, sàn bê tông tải trọng 3 - 5 tấn/m².' || CHR(10) ||
                '- Mái tôn cách nhiệt, hệ thống thông gió và chiếu sáng tự nhiên.' || CHR(10) ||
                '- Container 40ft ra vào và quay đầu dễ dàng.' || CHR(10) || CHR(10) ||
                '## Hạ tầng đồng bộ' || CHR(10) ||
                '- Điện 3 pha **250 - 500 kVA**, có trạm biến áp riêng.' || CHR(10) ||
                '- Cấp thoát nước, xử lý nước thải đạt chuẩn quy định.' || CHR(10) ||
                '- Văn phòng quản lý, nhà ăn, bãi đậu xe nhân viên đầy đủ.' || CHR(10) ||
                '- An ninh 24/7, PCCC đạt chuẩn QCVN, camera giám sát toàn khu.' || CHR(10) || CHR(10) ||
                '## Pháp lý & Hợp đồng' || CHR(10) ||
                '- Đầy đủ giấy chứng nhận quyền sử dụng đất, giấy phép xây dựng và môi trường.' || CHR(10) ||
                '- Cho thuê: tối thiểu 03 năm, đặt cọc 03 - 06 tháng.' || CHR(10) ||
                '- Chuyển nhượng: hỗ trợ vay ngân hàng tới **60% giá trị**.' || CHR(10) || CHR(10) ||
                'Liên hệ ngay để khảo sát thực địa và nhận báo giá chi tiết.'

            -- =====================================================================
            -- LAND
            -- =====================================================================
            ELSE
                '**' || l2.name || '** — Đầu tư đất nền pháp lý minh bạch tại ' || loc.name || '.' || CHR(10) || CHR(10) ||
                'Địa chỉ **' || p.street_address || '**, ' || loc.name || '. Tổng diện tích đất **' || COALESCE(p.land_size_m2::text, 'thoáng rộng') || ' m²**, ngang **' || COALESCE(p.width_m::text, 'lớn') || ' m** dài **' || COALESCE(p.length_m::text, 'lý tưởng') || ' m**. Thế đất bằng phẳng, đường trước rộng 8 - 12m, ô tô tránh nhau thoải mái.' || CHR(10) || CHR(10) ||
                '## Tiện ích xung quanh' || CHR(10) ||
                '- Khu dân cư hiện hữu, an ninh tốt, hàng xóm văn minh.' || CHR(10) ||
                '- Cách trung tâm hành chính khu vực 10 - 15 phút di chuyển.' || CHR(10) ||
                '- Gần trường học các cấp, chợ, siêu thị, trạm y tế, công viên.' || CHR(10) ||
                '- Hạ tầng điện, nước, internet đã kéo đến chân đất.' || CHR(10) || CHR(10) ||
                '## Tiềm năng tăng giá' || CHR(10) ||
                '- Khu vực quy hoạch mở rộng đường giao thông, xây dựng cầu vượt và metro.' || CHR(10) ||
                '- Mức tăng giá đất khu vực **12 - 18%/năm** trong 3 năm gần đây.' || CHR(10) ||
                '- Quỹ đất ngày càng khan hiếm — sản phẩm hữu hạn.' || CHR(10) || CHR(10) ||
                '## Pháp lý hoàn hảo' || CHR(10) ||
                '- Sổ hồng riêng, đầy đủ thổ cư, không quy hoạch, không tranh chấp.' || CHR(10) ||
                '- Chính chủ đứng tên, hỗ trợ công chứng sang tên trong ngày.' || CHR(10) ||
                '- Hỗ trợ vay ngân hàng tới **60% giá trị**, lãi suất ưu đãi.' || CHR(10) ||
                '- Cam kết hoàn cọc 100% nếu phát hiện sai lệch về pháp lý.' || CHR(10) || CHR(10) ||
                'Liên hệ ngay để khảo sát thực tế lô đất và kiểm tra hồ sơ pháp lý.'
        END AS long_content
    FROM listings l2
    JOIN properties p ON l2.property_id = p.property_id
    JOIN locations loc ON p.location_id = loc.location_id
    JOIN property_types pt ON p.property_type_id = pt.property_type_id
) sub
WHERE l.listing_id = sub.listing_id;
