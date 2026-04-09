-- V61__Insert_policies.sql
-- Inserts 14 highly detailed, comprehensive policy documents mimicking the length and strictness of leading real estate portals.

INSERT INTO policies (policy_id, title, slug, content)
VALUES
    (
        gen_random_uuid(), 
        'Điều khoản sử dụng', 
        'terms-of-service', 
        '<h2 class="text-xl font-bold text-red-600 mb-4">I. NGUYÊN TẮC CHUNG VÀ PHẠM VI ÁP DỤNG</h2>
<p class="mb-4 text-justify">1.1. Chào mừng Quý khách đến với sàn giao dịch bất động sản điện tử RealVista (sau đây gọi tắt là "RealVista" hoặc "Chúng tôi"). Bằng việc truy cập, đăng ký tài khoản, tải ứng dụng hoặc sử dụng bất kỳ dịch vụ nào trên nền tảng của Chúng tôi, Người sử dụng được hiểu là đã tự nguyện đọc, hiểu rõ, đồng ý và chấp nhận chịu sự ràng buộc bởi toàn bộ các <strong>Điều khoản thỏa thuận</strong> dưới đây.</p>
<p class="mb-4 text-justify">1.2. Bản Điều khoản sử dụng này cấu thành một hợp đồng pháp lý có giá trị ràng buộc giữa Người sử dụng và RealVista. Nếu Người sử dụng không đồng ý với bất kỳ điều khoản nào, vui lòng ngay lập tức ngừng việc truy cập và sử dụng các dịch vụ của hệ thống.</p>
<p class="mb-4 text-justify">1.3. RealVista bảo lưu quyền tự do sửa đổi, bổ sung, xóa bỏ hoặc cập nhật một phần hay toàn bộ nội dung của Điều khoản này bất kỳ lúc nào nhằm đáp ứng sự thay đổi của pháp luật sở tại hoặc đường lối hoạt động kinh doanh mà không cần phải thông báo trước cho từng cá nhân. Việc Quý khách tiếp tục sử dụng nền tảng sau khi các thay đổi được đăng tải đồng nghĩa với việc Quý khách chấp thuận những sửa đổi đó.</p>

<h2 class="text-xl font-bold text-red-600 mb-4 mt-8">II. QUYỀN VÀ NGHĨA VỤ CỦA NGƯỜI SỬ DỤNG VÀ THÀNH VIÊN</h2>
<h3 class="text-lg font-semibold mt-4 mb-2">2.1. Đăng ký tài khoản và Bảo mật</h3>
<p class="mb-4 text-justify">Người sử dụng phải cung cấp bộ thông tin định danh (Họ tên, số thẻ Căn cước công dân, số điện thoại, email) một cách chính xác, trung thực và cập nhật. Mọi hành vi mạo danh cơ quan, tổ chức hoặc cá nhân khác sẽ bị coi là vi phạm nghiêm trọng, dẫn tới việc tài khoản bị khóa vĩnh viễn và mọi số dư trong hệ thống sẽ bị đóng băng để phục vụ công tác đối soát của pháp luật.</p>
<p class="mb-4 text-justify">Thành viên tự chịu trách nhiệm áp dụng các biện pháp an toàn kỹ thuật để bảo mật mật khẩu và thiết bị đăng nhập. RealVista sẽ không chịu bất kỳ trách nhiệm pháp lý hoặc đền bù thiệt hại nào đối với những thất thoát tài chính nảy sinh do sự tắc trách trong bảo mật của Thành viên.</p>

<h3 class="text-lg font-semibold mt-4 mb-2">2.2. Nghĩa vụ đối với Nội dung Hệ thống</h3>
<ul class="list-disc pl-6 space-y-3 mb-6 text-justify">
<li><strong>(i) Tuân thủ Tính xác thực:</strong> Người đăng tin hoàn toàn chịu trách nhiệm trước pháp luật về tính hợp pháp, chính xác của thông tin bất động sản. Tuyệt đối không đăng tải các tài sản đang trong tình trạng bị kê biên, thế chấp chờ giải quyết, hoặc đang có tranh chấp mà không công khai tình trạng pháp lý một cách minh bạch.</li>
<li><strong>(ii) Hành vi cấm:</strong> Nghiêm cấm việc sử dụng bất kỳ hệ thống tự động, bot, spider, scraper hoặc các phương thức điện tử trái phép nào để can thiệp vào mã nguồn, trích xuất dữ liệu diện rộng, hoặc tạo ra các tương tác ảo nhằm đánh lừa hệ thống đánh giá của Sàn.</li>
<li><strong>(iii) Văn hóa giao tiếp:</strong> Khi sử dụng các tính năng nhắn tin, bình luận hoặc đánh giá trên nền tảng, Thành viên phải sử dụng ngôn ngữ văn minh. Tuyệt đối không dùng lời lẽ thóa mạ, truyền bá văn hóa phẩm đồi trụy, vi phạm thuần phong mỹ tục hoặc khơi mào các vấn đề nhạy cảm về chính trị, tôn giáo.</li>
</ul>

<h2 class="text-xl font-bold text-red-600 mb-4 mt-8">III. GIỚI HẠN TRÁCH NHIỆM PHÁP LÝ CỦA REALVISTA</h2>
<p class="mb-4 text-justify">3.1. RealVista đóng vai trò là "Sàn giao dịch thương mại điện tử" cung cấp không gian tương tác khách quan cho bên Bán và bên Mua. Chúng tôi <strong>KHÔNG</strong> tham gia vào quá trình định giá, thỏa thuận hợp đồng, làm chứng đặt cọc hay bảo lãnh dòng tiền giữa các bên tham gia giao dịch.</p>
<p class="mb-4 text-justify">3.2. Bằng tối đa nỗ lực kỹ thuật và quy trình kiểm duyệt nội bộ, RealVista nỗ lực loại bỏ các nội dung lừa đảo. Tuy nhiên, Chúng tôi không thể cam kết, đảm bảo hay chịu trách nhiệm tuyệt đối 100% về tính hợp pháp của mọi tin đăng, phẩm chất thực tế của khối bất động sản, cũng như năng lực hành vi dân sự của các bên giao dịch.</p>
<p class="mb-4 text-justify">3.3. Các đường dẫn (link) trỏ ra bên ngoài các trang web thuộc hệ sinh thái RealVista chỉ mang tính chất tham khảo. Chúng tôi không xác nhận, tài trợ hay chịu trách nhiệm cho các rủi ro máy tính (malware, virus) hoặc thiệt hại thương mại xuất phát từ các tên miền ngoại lai đó.</p>'
    ),
    (
        gen_random_uuid(), 
        'Quy định đăng tin', 
        'posting-regulations', 
        '<h2 class="text-xl font-bold text-red-600 mb-4">I. QUY ĐỊNH CHI TIẾT VỀ HÌNH THỨC VÀ TIÊU ĐỀ TIN ĐĂNG</h2>
<p class="mb-4 text-justify">Nhằm đem lại trải nghiệm tìm kiếm tối ưu và một môi trường dữ liệu bất động sản chuyên nghiệp, trong sạch, mọi tin đăng tải trên RealVista bắt buộc phải thỏa mãn nghiêm ngặt các quy chuẩn kỹ thuật sau đây. Tin đăng vi phạm sẽ bị hệ thống AI từ chối duyệt hoặc hạ bài tự động bởi Kiểm duyệt viên mà không được hoàn lại phí dịch vụ (nếu có).</p>

<h3 class="text-lg font-semibold mt-4 mb-2">1.1. Tiêu chuẩn viết Tiêu đề</h3>
<ul class="list-disc pl-6 space-y-3 mb-6 text-justify">
<li><strong>Độ dài bắt buộc:</strong> Tiêu đề tin đăng giới hạn độ dài tối thiểu là <strong>33 ký tự</strong> và tối đa không vượt quá <strong>99 ký tự</strong>.</li>
<li><strong>Cách thức viết hoa (Casing strictness):</strong> Nghiêm cấm mọi hình thức lạm dụng việc viết IN HOA toàn bộ tiêu đề hoặc viết IN HOA tùy tiện các từ khóa (VD: BÁN GẤP, ĐẤT NỀN GIÁ RẺ). Chỉ được phép viết hoa chữ cái đầu tiên của câu và viết hoa đúng chuẩn các Danh từ riêng (Tên phường xã, quận huyện, tên Dự án, tên Đường).</li>
<li><strong>Ngữ pháp và Ký tự:</strong> Bắt buộc sử dụng <strong>Tiếng Việt có dấu</strong> chuẩn xác. Không dùng tiếng lóng, teencode, hoặc từ ngữ xúi giục trái pháp luật. Tuyệt đối không chèn các ký tự đặc biệt mang tính chất gây chú ý, đánh lừa thị giác người xem (Ví dụ: @, #, $, %, ^, &, *, ~, !, >, <, ====, \`\`\`).</li>
<li><strong>Nội dung cấm trên tiêu đề:</strong> Tuyệt đối <strong>không chèn Số điện thoại</strong>, Link website, tên miền công ty đối thủ vào Tiêu đề tin đăng. Tiêu đề phải phản ánh trực diện loại hình, địa điểm và đặc tính vật lý của căn nhà/khu đất.</li>
</ul>

<h3 class="text-lg font-semibold mt-4 mb-2">1.2. Tiêu chuẩn Nội dung Mô tả (Description)</h3>
<p class="mb-4 text-justify">1.2.1. Nội dung trong phần mô tả phải đồng nhất và trùng khớp hoàn toàn với Tiêu đề, Loại hình bất động sản, và Mức giá đã niêm yết ở các trường dữ liệu hệ thống thao tác. (Tránh tình trạng Tiêu đề ghi 2 Tỷ, mô tả ghi trả trước 2 tỷ phần còn lại 5 tỷ ngân hàng).</p>
<p class="mb-4 text-justify">1.2.2. Thông tin diện tích phải ghi rõ là mét vuông sử dụng (trên sổ) hay mét vuông sàn xây dựng. Kích thước (Mặt tiền x Chiều sâu) phải diễn giải rõ ràng bằng các con số La tinh.</p>
<p class="mb-4 text-justify">1.2.3. Quy tắc "01 Tin - 01 Bất Động Sản": Nghiêm cấm gộp nhiều căn nhà, nhiều lô đất khác nhau vào dùng chung một bản mô tả hoặc một tiêu đề. Người tìm kiếm phải nắm bắt chính xác căn nhà nào đang được giao dịch qua bài đăng đó.</p>

<h2 class="text-xl font-bold text-red-600 mb-4 mt-8">II. YÊU CẦU VỀ HÌNH ẢNH VÀ ĐA PHƯƠNG TIỆN (MEDIA)</h2>
<h3 class="text-lg font-semibold mt-4 mb-2">2.1. Quy định Khắt khe về Hình ảnh</h3>
<ul class="list-disc pl-6 space-y-3 mb-6 text-justify">
<li><strong>Số lượng quy chuẩn:</strong> Tối thiểu <strong>03 ảnh thực tế</strong> và tối đa <strong>24 ảnh</strong> cho một bất động sản. Kích thước khuyến nghị là từ <strong>600x800 px</strong> trở lên, dung lượng tối đa 15MB/ảnh. Các bức ảnh bị nhòe, vỡ nét, ảnh khổ dọc quá kén (Vertical 9:16) khi hệ thống đẩy ra Web máy tính sẽ bị đánh điểm chất lượng thấp.</li>
<li><strong>Tính xác thực:</strong> Hình ảnh phải là ảnh chụp thực trạng tại hiện trường của căn nhà/mảnh đất. Không sử dụng ảnh mạng minh họa, ảnh phối cảnh 3D (Render) thay cho ảnh thực tế (trừ trường hợp bài đăng thuộc hạng mục Dự án đang hình thành trong tương lai và có sự cho phép công bố của Chủ đầu tư).</li>
<li><strong>Đóng dấu (Watermark) và Chèn chữ:</strong> 
    <br/>- Tuyệt đối <strong>KHÔNG</strong> chèn chữ nổi bôi đậm, KHÔNG chèn số điện thoại, KHÔNG chèn tên miền, và KHÔNG chứa logo của các trang web/nền tảng cạnh tranh lên bề mặt bức ảnh.
    <br/>- Logo xác nhận thương hiệu của cá nhân/Sàn giao dịch môi giới chỉ được phép hiển thị ở một trong bốn góc tường của bức ảnh, có độ mờ hợp lý, và diện tích hiển thị không được vượt quá <strong>1/5 (20%)</strong> tổng diện tích bức ảnh đó.</li>
</ul>

<h3 class="text-lg font-semibold mt-4 mb-2">2.2. Tiêu chuẩn Video</h3>
<p class="mb-4 text-justify">Người đăng tin có thể đính kèm tối đa <strong>01 Video</strong> bằng phương thức chèn Link YouTube/TikTok đã qua kiểm duyệt nội bộ hoặc tải trực tiếp lên bộ nhớ đám mây của nền tảng. Thời lượng clip thực tế không được dưới 10 giây và không dài quá 3 phút. Hình ảnh trong clip không được rung lắc quá mạnh và không được chèn các đoạn âm thanh dính bản quyền âm nhạc quốc tế. Không quay cận mặt người đăng nhằm che khuất khung hình bất động sản.</p>'
    ),
    (
        gen_random_uuid(), 
        'Chính sách bảo mật thông tin', 
        'privacy-policy', 
        '<h2 class="text-xl font-bold text-red-600 mb-4">I. TỔNG QUAN VỀ SỰ CAM KẾT VÀ PHẠM VI THU THẬP DỮ LIỆU</h2>
<p class="mb-4 text-justify">1.1. RealVista hiểu sâu sắc rằng việc bảo mật Tín dụng cá nhân và Thông tin danh tính là chìa khóa xây dựng sự trung thành của khách hàng. Chúng tôi thiết lập Chính sách Bảo mật này để trình bày các nguyên tắc minh bạch nhất về việc Chúng tôi thu thập, sử dụng, lưu trữ và bảo vệ Dữ liệu Cá nhân của người dùng trên lãnh thổ Việt Nam, tuân thủ chặt chẽ Nghị định 13/2023/NĐ-CP về bảo vệ dữ liệu cá nhân.</p>

<h3 class="text-lg font-semibold mt-4 mb-2">1.2. Mức độ và Hạng mục Dữ liệu được Thu thập</h3>
<ul class="list-disc pl-6 space-y-3 mb-6 text-justify">
<li>Trực tiếp từ Quý khách: Họ và Tên, Ngày Sinh, Căn cước công dân (áp dụng với tính năng tài khoản Xác thực KYC cấp cao), Địa chỉ Email, Số điện thoại di động chính xác.</li>
<li>Được thu thập tự động qua hệ thống: Các dấu vết điện tử kỹ thuật số gồm Địa chỉ IP máy tính, Chuỗi nhận dạng Trình duyệt (User Agents), MAC Address, Loại thiết bị phần cứng đang thao tác, Lịch sử tạo và chỉnh sửa các bài đăng niêm yết bất động sản, Lịch sử nạp và thanh toán RealCoin.</li>
<li>Cookie và Beacons: Phục vụ lưu trữ tệp hành vi phiên làm việc nhằm duy trì trạng thái đăng nhập xuyên suốt mà không yêu cầu Quý khách phải khai báo mật khẩu quá nhiều lần.</li>
</ul>

<h2 class="text-xl font-bold text-red-600 mb-4 mt-8">II. MỤC ĐÍCH SỬ DỤNG VÀ NGUYÊN TẮC CHIA SẺ DỮ LIỆU</h2>
<h3 class="text-lg font-semibold mt-4 mb-2">2.1. Hệ sinh thái Nội bộ</h3>
<p class="mb-4 text-justify">Toàn bộ kho dữ liệu người dùng được xử lý khép kín trong cụm lõi Server điện toán đám mây. Mục đích duy nhất của Chúng tôi là: (1) Cung cấp đầy đủ các tính năng Sàn giao dịch cho Quý khách, (2) Đối soát các thao tác chuyển tiền, trừ điểm ví phục vụ kế toán doanh nghiệp, (3) Triển khai thuật toán Đề xuất căn nhà phù hợp với túi tiền và khu vực địa lý mà bạn quan tâm dựa vào lịch sử View.</p>

<h3 class="text-lg font-semibold mt-4 mb-2">2.2. Chính sách KHÔNG BUÔN BÁN DỮ LIỆU</h3>
<p class="mb-4 text-justify">Chúng tôi long trọng cam kết <strong>tuyệt đối không trao đổi, cho phép bên thứ ba khai thác, hay mua bán</strong> Dữ liệu của Quý khách dưới hình thức gói Data khách hàng để phục vụ các mục đích thương mại ngoài phạm vi của hệ sinh thái nền tảng. Chống lại mọi hành vi trích xuất sdt cho công ty telesale, ngoại vi.</p>

<h3 class="text-lg font-semibold mt-4 mb-2">2.3. Châm chước Tiết lộ Dữ liệu trước Pháp luật</h3>
<p class="mb-4 text-justify">Thông tin của Quý khách sẽ chỉ bị trích xuất khỏi két sắt điện tử và cung cấp một cách miễn cưỡng khi có văn bản quy phạm, Lệnh khám xét, Lệnh triệu tập chính thức và có dấu đỏ từ <strong>Cơ quan cảnh sát điều tra, Tòa án nhân dân cao cấp</strong> hoặc Các cơ quan quản lý Nhà Nước có thẩm quyền nhằm phục vụ cho mục đích chặn đứng hoạt động tội phạm, lừa đảo xuyên biên giới mạng.</p>

<h2 class="text-xl font-bold text-red-600 mb-4 mt-8">III. TIÊU CHUẨN AN TOÀN KỸ THUẬT VÀ QUYỀN CHỌN LỰA</h2>
<p class="mb-4 text-justify">3.1. Toàn bộ tài khoản và chuỗi phiên truyền tải dữ liệu đăng nhập được phong tỏa mã hóa theo giao thức chuẩn công nghiệp SSL/TLS 256-bit mức độ tài chính ngân hàng, triệt tiêu mọi rủi ro Nghe lén (Man-in-the-middle) hoặc đánh cắp băng thông cục bộ.</p>
<p class="mb-4 text-justify">3.2. Quý khách giữ toàn quyền tự quyết trong thao tác cá nhân hóa mức độ can thiệp vào trang Settings: Yêu cầu xóa vĩnh viễn dữ liệu (Right to be Forgotten), Yêu cầu ngừng nhận các Email và Thông báo Push Marketing, Yêu cầu từ chối Cookie theo dõi thông qua Control Panel trình duyệt.</p>'
    ),
    (
        gen_random_uuid(), 
        'Chính sách phí & thanh toán', 
        'fees-and-payments', 
        '<h2 class="text-xl font-bold text-red-600 mb-4">I. QUY CHẾ VỀ ĐỒNG TIỀN NIÊM YẾT VÀ THANH TOÁN DỊCH VỤ</h2>
<p class="mb-4 text-justify">1.1. Dựa trên Pháp lệnh Ngoại hối số 28/2005/PL-UBTVQH11 và các văn bản chỉ đạo của Ngân hàng Nhà nước Việt Nam về việc "hạn chế đô la hóa nền kinh tế", mọi giao dịch cung ứng dịch vụ tại hệ thống nền tảng, cũng như toàn bộ các mức giá tài sản bất động sản hiển thị trong tiêu đề, thẻ tính năng, hoặc lời mô tả đều <strong>bắt buộc phải niêm yết bằng Việt Nam Đồng (VNĐ)</strong>.</p>
<p class="mb-4 text-justify">1.2. Mọi tin bài vô tình hay cố ý lách luật niêm yết bằng ngoại tệ mạnh (USD, EUR, Yen), lượng vàng, hoặc giao dịch trao đổi hàng hóa cấm sẽ bị hệ thống phát hiện tự động, tự kích hoạt chế độ khóa bài viết ẩn khỏi kết quả tìm kiếm, tài khoản đăng bài sẽ nhận Cảnh cáo mức 1.</p>

<h2 class="text-xl font-bold text-red-600 mb-4 mt-8">II. HỆ THỐNG ĐỒNG TIỀN QUY ƯỚC (REALCOIN) VÀ HÌNH THỨC NẠP TIỀN</h2>
<p class="mb-4 text-justify">2.1. Để trả chi phí cho các Tiện ích nâng cao như Nổi bật tin rao (Gói Tin VIP), Nhảy Top tin hàng ngày, Đăng chéo hệ sinh thái, người dùng phải sử dụng đồng tiền số hóa nội bộ được neo giá cố định có tên là <strong>RealCoin</strong> (Tỷ giá 1 RealCoin = 1 VNĐ, chưa bao gồm các khuyến mãi chiết khấu số lượng lớn).</p>
<p class="mb-4 text-justify">2.2. Các Phương thức nạp số dư (Top-up Wallet) đang được nền tảng ủy quyền thu chi hợp pháp bao gồm:</p>
<ul class="list-disc pl-6 space-y-3 mb-6 text-justify">
<li><strong>ATM Nội địa & Chuyển khoản Liên ngân hàng 24/7:</strong> Hỗ trợ mã VietQR thiết lập tĩnh theo cú pháp định danh tài khoản từng cá nhân, tự động quét và cộng số dư theo thời gian thực (Real-time).</li>
<li><strong>Thẻ tín dụng và Ghi nợ quốc tế (Visa, Mastercard, JCB):</strong> Định tuyến qua cổng thanh toán NAPAS/VNPAY theo tiêu chuẩn khắt khe PCI DSS nhằm miễn nhiễm với rủi ro đánh cắp dữ liệu thẻ in ấn.</li>
<li><strong>Ví điện tử trung gian:</strong> Tới từ các đối tác siêu ứng dụng hàng đầu (MoMo, ZaloPay, ViettelMoney).</li>
</ul>

<h2 class="text-xl font-bold text-red-600 mb-4 mt-8">III. CHÍNH SÁCH KHÔNG HOÀN LẠI VÀ CHÍNH SÁCH THU THUẾ</h2>
<h3 class="text-lg font-semibold mt-4 mb-2">3.1. Nguyên tắc Không hoàn phí Số dư Nạp</h3>
<p class="mb-4 text-justify">Ngoại trừ trường hợp vô cùng hãn hữu xuất phát từ hệ thống máy chủ vận hành lỗi (lỗi cơ sở dữ liệu làm thất thoát data, lỗi giao dịch double chard) dẫn đến việc khách hàng bị trừ tiền mà tài khoản ví chưa nhảy số dư. Chúng tôi áp dụng triệt để <strong>Nguyên tắc Không hoàn trả (No-Refund Policy)</strong> cho các khoản số dư đã được nạp thành công vào hệ thống để quy đổi thành RealCoin dưới mọi hình thức, mọi lý do (kể cả việc Quý khách quyết định nghỉ dùng dịch vụ hay đã bán được hình mẫu bất động sản).</p>
<h3 class="text-lg font-semibold mt-4 mb-2">3.2. Thuế GTGT và Hóa đơn tài chính</h3>
<p class="mb-4 text-justify">Mọi mức giá của Gói tin nâng cấp đã bao gồm phần trăm Thuế giá trị gia tăng (VAT) quy định do pháp luật hiện hành. Nếu người sử dụng đại diện cho danh xưng Pháp nhân/Doanh nghiệp muốn xuất hóa đơn điện tử VAT nhằm mục đích hạch toán khấu trừ chi phí kinh doanh, vui lòng thao tác điền Form Mã số thuế và Email phòng kế toán tại Cổng hỗ trợ trong vòng thời gian hạn mức 15 ngày kế từ thời điểm nạp ví thành công.</p>'
    ),
    (
        gen_random_uuid(), 
        'Quy chế hoạt động TMĐT', 
        'e-commerce-regulations', 
        '<h2 class="text-xl font-bold text-red-600 mb-4">I. TÔN CHỈ VÀ QUY TẮC HOẠT ĐỘNG CHUNG CỦA BỘ MÁY</h2>
<p class="mb-4 text-justify">1.1. Sàn giao dịch bất động sản điện tử cấp quốc gia RealVista được lập ra và cung ứng dựa trên sự bảo trợ của Bộ Công Thương. Mọi hoạt động quy thương, tìm kiếm, đăng tin, thương thuyết về giá m2, ký hợp đồng kết nối giữa môi giới, chính chủ, và nhà đầu tư trên Sàn đều phải tuân thủ nghiêm minh các quy định của hệ thống pháp luật nhà nước Cộng hòa Xã hội Chủ nghĩa Việt Nam.</p>
<p class="mb-4 text-justify">1.2. Mọi hoạt động trên Sàn đều được thực hiện dựa trên cơ chế Tự nguyện, Công khai và Minh bạch, hướng tới mục tiêu tối thượng: Giảm thiểu sự rủi ro bất đối xứng thông tin, đảm bảo tối đa quyền lợi chính đáng cho những người mua/thuê nhà cuối cùng.</p>

<h2 class="text-xl font-bold text-red-600 mb-4 mt-8">II. BA KHÔNG: BẢN CHẤT VỊ THẾ CỦA SÀN GIAO DỊCH VÀ KHƯỚC TỪ PHÁP LÝ</h2>
<p class="mb-4 text-justify">Nhằm tránh những diễn giải sai lệch về trách nhiệm ràng buộc hợp đồng, Chúng tôi cung cấp một "Môi trường giao dịch kỹ thuật số thông minh". Cụ thể, RealVista <strong>tuyệt đối KHÔNG ĐÓNG VAI TRÒ LÀ:</strong></p>
<ul class="list-disc pl-6 space-y-3 mb-6 text-justify">
<li><strong>(i) Bên sở hữu trực tiếp tài sản:</strong> Chúng tôi không phải là Chủ đầu tư dư án bán, hay cá nhân đang cho thuê căn hộ niêm yết. Mọi thuộc tính hiện diện trên nền tảng đều do cá thể User công bố và cam kết.</li>
<li><strong>(ii) Chủ thể bảo lãnh của hợp đồng:</strong> Chúng tôi không phải là đơn vị phát hành bảo lãnh dòng tiền, chứng thư bảo toàn vốn, hay đứng ra thế chấp chịu thay đền bù cho bất kỳ giao dịch bất động sản, khoản cọc mua bán nào phát sinh bởi hai người dùng tự thỏa thuận ra môi trường bên ngoài máy chủ màn hình.</li>
<li><strong>(iii) Chủ thể định giá quy chuẩn thẩm quyền:</strong> Công cụ định giá và chỉ dấu gợi ý giá của chúng tôi đơn thuần xoay quanh Big-data thống kê phân tích các chuỗi hành vi của đám đông và các tin có sẵn. Nó không có chức năng là Dịch vụ định giá pháp quyền như của Ngân hàng.</li>
</ul>

<h2 class="text-xl font-bold text-red-600 mb-4 mt-8">III. CHỐNG HÀNH VI LŨNG ĐOẠN, ĐẦU CƠ & XẢ TIN RÁC (SPAM)</h2>
<p class="mb-4 text-justify">Sàn cực lực lên án và loại bỏ vô thời hạn hệ sinh thái các cá nhân, tập hợp môi giới có hành vi "Bơm và xả" thị trường để phá giá, đánh sập niềm tin của nhà đầu tư nhỏ lẻ. Cụ thể, mọi cá nhân có hành vi: Dùng phần mềm giả lập, mở vô số đa tài khoản hòng lách luật lên tin tự động; Đăng đồng loạt những bất động sản với mức giá rớt đáy hoang đường nhằm thu thập thông tin người muốn mua; Tạo tin đồn quy hoạch giả để gây biến động giá khu vực cục bộ... Tất cả sẽ bị xóa ngay bộ biên mục tài khoản và bị báo cáo trích xuất truy tố tới cơ quan thanh tra an ninh mạng.</p>'
    ),
    (
        gen_random_uuid(), 
        'An toàn giao dịch', 
        'transaction-safety', 
        '<h2 class="text-xl font-bold text-red-600 mb-4">I. CẢNH BÁO CAO ĐỘ VÀ QUẢN TRỊ RỦI RO CHO KHÁCH HÀNG</h2>
<p class="mb-4 text-justify">1.1. Bất động sản cấu kết nên một hàm lượng giá trị dòng tiền khổng lồ, thường được đánh đổi bằng quá trình tích lũy nhiều thế hệ. Do đó, sự nôn nóng, mất cảnh giác hay mọi quyết định vội vàng qua các kết nối mạng ảo đều mở ra nguy cơ trở thành con mồi ngon cho hệ thống tổ chức lừa đảo mạo danh tinh vi.</p>
<p class="mb-4 text-justify">1.2. Nhằm triệt tiêu môi chất gây nuôi tội phạm công nghệ, Ban Quản trị Sàn giao dịch RealVista khẩn thiết vạch ra nguyên tắc <strong>"4 KHÔNG CƠ BẢN"</strong> cho mọi khách đi tìm bất động sản:</p>
<ul class="list-disc pl-6 space-y-3 mb-6 text-justify">
<li><strong>KHÔNG CỌC MÙ:</strong> Tuyệt đối không thỏa thuận giao tiền bằng các hình thức qua tài khoản Momo, Viettel pay, hay bank cá nhân xa lạ để "đặt chỗ giữ nhà kẻo lỡ" khi bản thân chưa đến chấn kiến mảnh đất, lô nhà đó và đo đạc tận nơi bằng mắt thường.</li>
<li><strong>KHÔNG GIẤY TAY ẨU:</strong> Tuyệt đối không ký hay phó thác hàng trăm triệu đồng ở các quán cafe, nhà riêng với những mảnh giấy viết tay không hề có sự xác thực của Văn phòng Công chứng được Bộ Tư Pháp chính quy ủy quyền.</li>
<li><strong>KHÔNG PHÍ MỞ CỬA:</strong> Bỏ qua và đưa vào Blacklist mọi mẩu tin rao căn hộ khang trang, đẹp như trên phim, mức giá siêu rẻ hấp dẫn, đi theo những yêu sách bệnh hoạn như gửi tiền đổ xăng cất công đến mở cửa cho xem nhà. Đều là lừa đảo.</li>
<li><strong>KHÔNG RỜI KHỎI NỀN TẢNG YẾU ỚT:</strong> Khi thực sự không tin tưởng các liên kết qua Chat App thứ ba, hãy tiến hành duy trì mọi báo giá, chat, tương tác nội bộ trên ứng dụng RealVista nhằm lưu trữ lại toàn bộ bằng chứng pháp đình nếu điều xấu nảy sinh.</li>
</ul>

<h2 class="text-xl font-bold text-red-600 mb-4 mt-8">II. DANH MỤC CÔNG CỤ BÁO CÁO VI PHẠM (REPORT SYSTEM) VÀ CHẾ TÀI</h2>
<p class="mb-4 text-justify">2.1. Cùng nhau xây dựng tường thành vững chắc cho dữ liệu quy tín. Toàn bộ các giao diện hiển thị tin rao chi tiết của RealVista đều được gắn cứng phím bấm tiện ích "Báo cáo tin lừa đảo / Tin ảo đỏ đen" tại vị trí đắc địa dễ click nhất. Sức mạnh của sự rà quét nhân công từ cộng đồng là vũ khí khắc chế tốt nhất của cái xấu.</p>
<p class="mb-4 text-justify">2.2. Khi bất kỳ tin rao nào tiếp nhận khối lượng Báo cáo "Tin treo đầu dê bán thịt chó / Không có nhà mà cứ báo giá láo để dụ gặp", Hệ thống quản lý sự cố (Incident Response Team) sẽ tạm ẩn bài viết trong 3 giờ để chờ minh chứng giấy tờ sổ đỏ qua Camera. Hệ sinh thái Sẽ thu thập MAC/IP của thủ phạm với khả năng cao đưa vào lệnh Cấm đăng xuất bản chéo IP Vĩnh viễn (Permanent Banned).</p>'
    ),
    (
        gen_random_uuid(), 
        'Chính sách tài khoản & KYC', 
        'account-and-kyc', 
        '<h2 class="text-xl font-bold text-red-600 mb-4">I. ĐIỀU KIỆN TIÊN QUYẾT TRONG ĐĂNG KÝ VÀ DUY TRÌ DANH TÍNH</h2>
<h3 class="text-lg font-semibold mt-4 mb-2">1.1. Yêu cầu chính danh pháp quyền</h3>
<p class="mb-4 text-justify">Nền tảng của chúng tôi nỗ lực hạn chế cơ chế cho phép tạo vô số tài khoản ẩn danh để trục lợi spam. Theo đó, mỗi cá nhân tại lãnh thổ Việt Nam sở hữu thông qua một đầu số thuê bao Di động Việt Nam có đầu số vùng (+84) hợp pháp chỉ được liên kết mở rộng để duy trì <strong>01 phiên Tài khoản cá nhân duy nhất</strong>.</p>
<p class="mb-4 text-justify">Người dùng phải đảm trách năng lực dân sự (tối thiểu 18 Tuổi). Mọi hành vi lập sàn buôn bán, sang nhượng điểm tài khoản chui trái phép không qua kiểm duyệt sẽ khiến hệ thống quét đồ thị khóa cả phần tài khoản người mua và người mang điểm đi bán.</p>

<h2 class="text-xl font-bold text-red-600 mb-4 mt-8">II. CHƯƠNG TRÌNH XÁC QUYỀN VÀ TRÁCH NHIỆM DANH TÍNH KYC (KNOW YOUR CUSTOMER)</h2>
<p class="mb-4 text-justify">2.1. Khái niệm xác định tài khoản KYC: Đây là dòng tài khoản phân cấp bậc danh dự và uy tín cao thủ bật nhất, dành riêng để vinh danh các Chuyên viên môi giới hoặc chính chủ nhà tận tâm muốn đẩy mạnh dòng tiền thanh khoản.</p>
<p class="mb-4 text-justify">2.2. Quy trình kiểm thử giấy tờ: Cá nhân sẽ nộp lên màn hình trung tâm của chúng tôi Ảnh chụp trực diện sắc nét mặt sau/mặt trước Thẻ CCCD gắn chip (hoặc Căn cước mới) kèm thao tác nhận diện cử động sinh trắc học khuôn mặt. Bộ máy trí tuệ nhân tạo sẽ chích xuất dữ liệu so khớp để phong tặng phù hiệu bảo lãnh <strong>Tài khoản Bất Động Sản Đã Xác Thực Công Dân</strong>. RealVista đồng thời cam kết lưu khối giấy tờ này vào vệt khối hệ thống băng lạnh cách ly với các truy cập thông thường nhằm đảm bảo người nộp CCCD không lo bị đánh cắp tài liệu.</p>'
    ),
    (
        gen_random_uuid(), 
        'Quy định đăng tin dự án', 
        'project-posting-regulations', 
        '<h2 class="text-xl font-bold text-red-600 mb-4">I. HÀNG RÀO KIỂM DUYỆT THÔNG TIN DỰ ÁN BẤT ĐỘNG SẢN MỚI</h2>
<p class="mb-4 text-justify">Thay vì những bài đăng chuyển nhượng nhỏ lẻ, các sản phẩm nằm trong Khu Đô Thị tích hợp hoặc Quần thể Chung cư (còn gọi chung là Sản Phẩm Dự Án Mở Bán) mang theo một trọng trách lớn vì chúng huy động vốn đại chúng. Quy định đăng tin loại hình này bị thắt chặt ở ngưỡng cao nhất, nhằm không để Sàn thương mại điện tử trở thành đồng lõa tiếp tay của các Chủ đầu tư rác (Bán dự án bãi hoang, chung cư trên giấy không móng).</p>

<h3 class="text-lg font-semibold mt-4 mb-2">1.1. Bắt buộc minh bạch Pháp lý Dự án</h3>
<p class="mb-4 text-justify">Nhà môi giới hoặc đại diện doanh nghiệp lập bài đăng khai báo Tổ hợp dự án bắt buộc phải đăng tải và miêu tả minh bạch và trần trụi về tình trạng Tiến độ pháp lý thực tại. Nếu dự án đã đủ yêu cầu bán thì phải khai rõ: Sổ nguyên lô hay tách thửa, Đã có quyết định 1/500 hoàn thiện chưa, Đã khởi công ép móng lấy Giấy phép Xây dựng chưa, Quyết định giao thu hồi đất từ Sở Tài nguyên và Môi trường. Các thông số lờ mờ sẽ khiến tin bài từ chối duyệt chờ bổ sung.</p>
<p class="mb-4 text-justify">Nghiêm cấm hành vi gắn chữ Dự án mới cho những khu đất phân lô tự hoang hóa ở cách xa hàng dặm so với các khu Đô thị lớn để trục lợi và làm hoang mang thuật toán tìm tuyến điểm.</p>

<h3 class="text-lg font-semibold mt-4 mb-2">1.2. Thẩm quyền chủ thể đứng chức danh công bố bán</h3>
<p class="mb-4 text-justify">Chỉ có <strong>Chủ đầu tư tập đoàn thực sự</strong>, hoặc các Sàn phân phối F1/F2 được cấp độc quyền hoặc chứng thư tiếp nhận phân phối chính thức mới được quyền công bố loại hình "Bán mới, ra mắt Dự án". Mọi cá nhân đầu cơ đã làm hợp đồng mua tự phát thứ cấp khi có nhu cầu nhượng lại, chỉ được phép đăng thao tác trên danh nghĩa loại hình "Tin Mua Bán Chuyển Nhượng cá nhân" để dễ bề đối chứng.</p>'
    ),
    (
        gen_random_uuid(), 
        'Quyền sở hữu trí tuệ', 
        'intellectual-property', 
        '<h2 class="text-xl font-bold text-red-600 mb-4">I. THƯƠNG QUYỀN ĐỘC LẬP TỪ PHÍA NỀN TẢNG (REALVISTA)</h2>
<p class="mb-4 text-justify">1.1. Toàn bộ hình khối vật chất hiện vật, ngôn ngữ thiết kế của ứng dụng, cấu trúc mã nguồn API tinh túy, nghệ thuật sắp xếp UX/UI, logo biểu trưng, màu sắc nhận diện thương hiệu hình thành nên bộ mặt nền tảng điện tử của sàn RealVista hoàn toàn thuộc độc quyền sơ hữu bản quyền trí tuệ.</p>
<p class="mb-4 text-justify">1.2. Mọi cơ quan lập trình, tổ chức môi giới thứ ba hoặc tập thể sao chép ngang nhiên toàn phần cấu trúc giao diện để phát triển Sàn giao dịch ngoại lai, gây ra sự định vị nhầm lẫn đối với tệp khách hàng, Chúng tôi đủ hành lang pháp lý để đâm đơn và đề xuất đánh sập vĩnh viễn tên miền thông qua Hiệp hội thương mại mạng quốc tế.</p>

<h2 class="text-xl font-bold text-red-600 mb-4 mt-8">II. DỮ LIỆU ĐÓNG GÓP TỪ PHÍA CÁ NHÂN NGƯỜI DÙNG (USER-GENERATED CONTENT)</h2>
<p class="mb-4 text-justify">2.1. Chúng tôi túc trực với tư duy đề cao quyền làm cơ sở chủ của người dân. Điển hình, khi Bạn thao tác bấm Submit để tải lên Sàn một loạt tác phẩm ảnh chụp các căn phòng của mình nhằm rao bán nhà... Bạn và vĩnh viễn là Bạn sẽ là người gìn giữ độc lập Bản Tác Quyền với đống Ảnh Gốc đó. Sàn giao dịch RealVista không bao giờ nhận vơ.</p>
<p class="mb-4 text-justify">2.2. Kế tiếp, Bằng việc ấn Tải ảnh, Quý khách ngầm định ủy thác quy trình để bộ máy trí tuệ hệ thống tự động hóa RealVista được phép can thiệp (Chỉnh độ phân giải cho nhẹ mượt hơn, Cắt ghép xoay lật đúng tỷ lệ chuẩn của web, Tự động đóng dấu một Logo chìm mảnh mai "Chứng Nhận bởi RealVista" vào tấm ảnh nhà bạn). Việc chèn logo này nhằm bẻ khóa đường lui của mọi công cụ Cào dữ liệu (Scraping/Crawling Bot) ăn cắp vô luân chất xám ảnh của bạn sang nền tảng đối thủ cạnh tranh.</p>
<p class="mb-4 text-justify">2.3. Trong quá trình điều tra chéo nền tảng, nếu bạn phát hiện ra Môi giới giả mạo đã lén bệ nguyên ảnh căn nhà bạn từ máy chủ chúng tôi, xóa lẹnh tên file và đăng sang web bds khác, RealVista sẽ đứng ra bảo trợ, mở hồ sơ Digital Millennium Copyright Act (DMCA) trút quyền và yêu cầu máy chủ ngoại lai triệt tiêu chùm ảnh nhà cửa của bạn.</p>'
    ),
    (
        gen_random_uuid(), 
        'Giải quyết tranh chấp & khiếu nại', 
        'dispute-resolution', 
        '<h2 class="text-xl font-bold text-red-600 mb-4">I. QUY CÁCH TIẾP NHẬN PHẢN HỒI LỖI KỸ THUẬT VÀ GIAO DỊCH TÀI KHOẢN</h2>
<p class="mb-4 text-justify">1.1. Với vai trò chủ quản vận hành Sàn Thương Mại Điện Tử, quá trình giải quyết mọi vướng mắc, trục trặc máy chủ, sụt giảm số dư điểm nạp RealCoin không lý do, đều dựa trên kim chỉ nam: Tôn trọng quyền lợi tài chính tiêu dùng.</p>
<p class="mb-4 text-justify">1.2. Đội Kiểm duyệt và CSKH (Call Center) của Chúng tôi trực thu sóng qua kênh Hotline chính và Hộp thư Số bảo hộ chuyên biệt, nhằm tiến hành sửa lỗi, hoàn trả tự động các giao dịch nạp Wallet kép. Cam kết mọi vấn đề liên quan tới chất lượng dòng mạch sử dụng phần mềm, gói Đăng Vip sẽ được phân tách xử lý trong hạn mức 05 ngày làm việc hành chính.</p>

<h2 class="text-xl font-bold text-red-600 mb-4 mt-8">II. VAI TRÒ CHỐNG TRANH CHẤP DÂN SỰ RA NGOÀI PHẠM VI SÀN GIAO DỊCH</h2>
<p class="mb-4 text-justify">2.1. Đa phần các rủi ro mâu thuẫn lớn đều không xuất phát từ chúng tôi, mà khởi sinh bởi việc thỏa thuận phá giá, cung cấp số liệu sai căn nhà, lật lọng giữa Người Mua Đất với Chuyên viên Môi Giới/Tập Thể Tổ Chức hoặc Chủ Nhà phát sinh trên thực địa sau khi làm quen nhau qua Sàn.</p>
<p class="mb-4 text-justify">2.2. Đối mặt với các xung đột vượt mức đó, Thực thể Pháp Nhân đại diện Sàn RealVista duy trì vị thế Trọng tài viên trung lập vô tư. Chúng tôi sẽ không chi ngân quỹ đền bù cho các thoả thuận miệng của đôi bên. Tuy nhiên, thay vì khoanh tay đúng ngó, Sàn giao dịch sẽ hỗ trợ trích lục khối lượng lớn, tiến hành phong tỏa các luồng tin dẫn chứng, đoạn ghi chú chat, thu chi số liệu nhật ký server để bàn giao toàn diện tạo lập bằng chứng thép cho cho Thanh Tra Điều Tra, Tòa Án Pháp Việt Nam nếu quy trình 2 bên không hòa giải êm đẹp để định tội cưỡng đoạt.</p>'
    ),
    (
        gen_random_uuid(), 
        'Chính sách cookie', 
        'cookie-policy', 
        '<h2 class="text-xl font-bold text-red-600 mb-4">I. DIỆN TÍCH SỬ DỤNG VÀ LÝ LẼ CHẤP NHẬN BẢO LƯU SESSION-COOKIE DỮ LIỆU</h2>
<p class="mb-4 text-justify">1.1. Cookie bản chất là các chuỗi giá trị văn bản tinh giản nhỏ nhẹ được phía Server (Hệ thống điều vận) của Chúng tôi phái đi và tựa vào nằm ẩn trên Bộ nhớ Trình duyệt của Quý khách. Điều này nhằm giúp hệ thống thần kinh máy tính tự động hiểu được thói quen và quá khứ tìm trang của bạn mà không tốn công hỏi lại mỗi ngày.</p>
<p class="mb-4 text-justify">1.2. Bằng cách định kỳ ghi lại cấu hình bộ lọc giá từng mét vuông quan tâm, loại hình căn hộ (biệt thự hay căn hộ ven kênh) bạn vừa miệt mài xem suốt ngày hôm qua, Cookie trực tiếp lược bỏ việc lặp lại vô nghĩa khối lượng thao tác "Nhập vào lại thông tin diện tích tìm kiếm từ đầu" trong các lần tải trang kế tiếp từ cùng một PC/Điện thoại. Ngoài ra Session-Cookie phân nhánh là nhân tố cấu thành chức năng duy trì duy nhất Đăng Nhập Xuyên Các Thiết Bị mà không hỏi Mật Khẩu lặp lại hai mươi lần.</p>

<h2 class="text-xl font-bold text-red-600 mb-4 mt-8">II. QUYỀN TRỪ TÁC ĐỘNG VÔ HIỆU HÓA NGAY LẬP TỨC DATA THEO DÕI ADVERTISING MARKETING</h2>
<p class="mb-4 text-justify">2.1. Bên cạnh cái cốt lõi thân thuộc nêu trên (Strictly Necessary Cookies - thứ duy trì trang web không tê liệt vòng lặp hiển thị), Quý khách hoàn toàn tự cường nắm trọn đặc quyền hủy hoại tính chất đeo bám của Cookie Quảng Cáo (Ad-retargeting).</p>
<p class="mb-4 text-justify">2.2. Bạn hoàn toàn có thể can thiệp tùy ý vào Cấu Hình Quyền Riêng Tư (Privacy Shield / Incognito Mode) ở hệ thống cài đặt ngầm của Chrome / Safari Browser, hoặc chọc biểu tượng Ổ Khóa ở ngay trên đầu thanh nhập trình duyệt để nhấn chối bỏ quyền Tracking thông qua Cookie. Dĩ nhiên, đi kèm lúc này quảng cáo của Chúng tôi sẽ rời rạc, và trang web có thể quên mật khẩu tự lưu của bạn, đó là điều không tránh khỏi của cơ thế chặn Cookies.</p>'
    ),
    (
        gen_random_uuid(), 
        'Chính sách quảng cáo', 
        'advertising-policy', 
        '<h2 class="text-xl font-bold text-red-600 mb-4">I. THƯỚC ĐO CHUẨN MỰC VÀ QUY CHẾ DÀNH CHO CÁC NHÀ TÀI TRỢ DOANH NGHIỆP QUA NATIVE ADS</h2>
<h3 class="text-lg font-semibold mt-4 mb-2">1.1. Chất lượng khung hình ảnh và sự tinh tươm của Banner Điện Tử</h3>
<p class="mb-4 text-justify">Thiết kế Banner quảng cáo kích cỡ (Leaderboards, Sidebar Rectangles) được ban giám đốc Marketing duyệt thủ công gắn tại Trang Chủ, Menu Trục trượt phải vượt qua vòng duyệt khắt khe về kỹ thuật điện học, mỹ nghệ hội họa. Biển không được nhấp nháy chuyển chớp liên hồ với tần số cực đoan nhằm móc mắt người đi qua lại. Tone nền và Typography không được chèn các thông điệp câu view cực hạn, nhảm nhí nhằm xúc phạm trí tuệ nhóm công chức dân văn phòng.</p>

<h3 class="text-lg font-semibold mt-4 mb-2">1.2. Cơ chế lọc lĩnh vực kinh doanh độc quyền</h3>
<p class="mb-4 text-justify">Việc bỏ tiền đấu thầu hiển thị Ads không có tác dụng với các chuyên ngành cấm kỵ. Chúng tôi tiến hành nghiêm cấm tước bỏ 100% các quảng cáo dẫn xuất đầu dây (Lead traffic redirect) trỏ tay hạ cánh qua các tên miền liên đới tới Tổ Phường cờ bạc cá độ quốc tế, Hoạt động phao tin đồn thổi giá đất ngoại cảm, tư vấn các kênh khóa đào tạo mua đất ảo siêu lợi nhuận đa cấp ảo ảnh chưa được Nhà nước thẩm định.</p>

<h3 class="text-lg font-semibold mt-4 mb-2">1.3. Trừng trị trớ trêu hình sự mạo nhận và thủ đoạn Cloaking Website</h3>
<p class="mb-4 text-justify">Sàn giao dịch sẽ truy sát tận cùng và đối tác mua Không Gian Trưng Bày phải chịu chi phí pháp lý và hình sự tòa án liên đới nếu Bộ Tư Lệnh Kỹ thuật An Ninh phát hiện có hành vi tráo Domain dơ bẩn (Cloaking Website / Man in The Middle) - kiểu qua cầu rút ván nhằm đánh lừa màn hình lúc đi xét duyệt ra một web khác, rồi vào đêm tối thì đổi máy chủ luồng sang một đường dẫn dơ dáy chứa mã độc (Drive-by Download) hòng che giấu, bôi đen bộ đếm của Sàn.</p>'
    ),
    (
        gen_random_uuid(), 
        'Chính sách đánh giá & review', 
        'review-policy', 
        '<h2 class="text-xl font-bold text-red-600 mb-4">I. QUY TẮC ĐẠO ĐỨC TIÊU DÙNG VÀ BẦU CHỌN THÔNG TIN TÌM KIẾM CÔNG MINH</h2>
<p class="mb-4 text-justify">1.1. Thưa Quý vị, Hệ thống Vote Trực Tuyến Review bình loạn (Xếp hạng người bán/môi giới thông qua hệ thống dải quy chiếu 1 đến 5 sao cộng văn bản viết tay) là Cột mốc cốt lõi duy nhất nhằm thanh lọc thế giới mạng của Sàn Giao Dịch BĐS Uy Tín. Không một đồng tiền lách luật chui nào ở đây có thể mua chuộc một tài khoản đạt huy hiệu Kim Cương nếu nó đầy dẫy những lỗi thiếu chuyên nghiệp.</p>
<p class="mb-4 text-justify">1.2. Mọi hệ thống thuật toán kéo Bots ảo (Click-Farms), thành lập mạng lưới mua bán Nick lập khống từ điện thoại cỏ để thay phiên kéo Vote 5 sao nhằm vượt tường lên Top Môi giới Xuất sắc, đều bị quét trần thấu và bị tiêu diệt bởi công nghệ Vùng Chặn AI chống Seeding gian đối của hệ thống máy chủ, dẫn đến xóa kênh người làm Môi Giới Vĩnh Viễn.</p>

<h2 class="text-xl font-bold text-red-600 mb-4 mt-8">II. TIẾP ĐIỂM CƠ CHẾ KHÁNG CÁO, XÓA YÊU CẦU & KHIẾU NẠI NGƯỢC DO XÚC PHẠM XẾP HẠNG</h2>
<p class="mb-4 text-justify">2.1. Đội ngũ đánh giá trung lập Ban vận hành sẽ chủ động can thiệp bằng sức mạnh admin để tiêu hủy trảm diệt tức khắc các Reviews bôi nhọ bị gắn tính chất phá hoại đối thủ công ty, dùng chung văn phong khiếm nhã vô văn hóa, đả kích phân vùng địa lý quê hương gốc gác của Môi Giới, văng tục phi nhân tín. Bất kể việc có phải người mua đúng hay không, xúc phạm nhân quyền sẽ bị hủy comment không bồi thường khóa tài khoản.</p>
<p class="mb-4 text-justify">2.2. Tuy nhiên điều hệ trọng cuối cùng. Thực thể Ban Quản Trị RealVista có nguyên tắc <strong>TỪ CHỐI THOẢ HIỆP VÀ LÀM NGƠ VIỆC XÓA BỎ CÁC REVIEWS CHÂN THỰC 1 SAO</strong> của những khách hàng gặp phải khổ đau phiền não nanh nọc khi giao dịch. Những lời lẽ phàn nàn bực dọc về trình tự thái độ phục vụ lồi lõm từ Broker, sự giấu giếm gian lận pháp lý căn hộ, vi phạm nghiêm trọng thông tin số đo mét vuông với thế giới hiện thực. Hãy nghe rõ: Bảng Vàng Danh Dự hệ thống Tín Nhiệm không chuyên bán dịch vụ che đậy lừa dối với bất kỳ món tiền kếch xù của bất kỳ tay to nào trên thương trường.</p>'
    ),
    (
        gen_random_uuid(), 
        'Chính sách marketing', 
        'marketing-policy', 
        '<h2 class="text-xl font-bold text-red-600 mb-4">I. HỆ SỐ NẠP CHIẾN DỊCH VÀ CƠ CHẾ TIẾP THỊ GẦN GŨI (AUTOMATED MARKETING FUNNELS)</h2>
<p class="mb-4 text-justify">1.1. Hoạt động trên cương vị một nền tảng tư vấn tự động hóa cực độ thông qua Trí Tuệ Điện Tử. Khách hàng sử dụng dịch vụ thông minh của Hệ Thống Sẽ Cảm Thấy may mắn khi được tiếp cận các Luồng hệ thống Email Chuỗi (Funnels), tin nhắn Nhấn nhịp Push App tự động hàng tuần nhằm liên tục cập nhật sự lên xuống, trượt giá hoặc các khuyến mãi mua trả góp đối với các ngôi nhà mà Mình từng đau đáu vào giỏ (Wishlist). Điều này đạt được thông qua cơ chế Auto-Marketing chuyên biệt được huấn luyện theo độ sâu cá nhân hóa thân thiện của RealVista.</p>

<h2 class="text-xl font-bold text-red-600 mb-4 mt-8">II. CẢNH GIỚI TÔN KÍNH VÀ QUYỀN LỰA CHỌN TỰ QUYẾT BẤT KHẢ CHỐI TỪ ĐỐI VỚI KHÁCH HÀNG</h2>
<p class="mb-4 text-justify">2.1. Tuy là thế lực Marketing dày đặc. Tuy vậy, RealVista luôn khét tiếng là bên chống Rác thư, nỗ lực tôn trọng chu kỳ sinh học và luật chống quấy rối (Anti-Spam Regulations) của Châu âu và Việt Nam. Điển hình, Mọi thư ngỏ và Email Newsletters báo cáo Mua Bán Nhà Đất của chúng tôi không bao giờ phát sau ngưỡng kim chỉ nam là 22:00 đem. Thêm đó, cơ chế lọc tần số loại trừ việc Gửi Spam hơn 2 tin liên hoàn trong 1 ngày cúng tới 1 User cụ thể trên điện thoại.</p>
<p class="mb-4 text-justify">2.2. Hơn thế nữa, một cú Click chạm nhè nhẹ vào nút link nhỏ ở dưới đáy thư <strong>"Unsubscribe"</strong>, hay việc gạt một thanh trễ cài đặt duy nhất ghi <strong>"Ngưng nhận Email/SMS quảng cáo mồi nhà đất"</strong> trong trang Cài Đặt Hồ Sơ Settings. Đó chính là một tấm giật dây tối cao, một mệnh lệnh chấm dứt tức thì đối với mọi hệ thống thuật toán săn bắn trên phiên diện tiếp thị. Bạn sẽ tĩnh lặng hoàn toàn cho đến ngày bạn chủ động bật chúng trở lại.</p>'
    );
