-- V17__Insert_property_amenities.sql
-- Insert master amenities (consolidate duplicates)
-- Each amenity defined once with unique code, reused across property types via property_type_amenities junction table

-- ============================================================================
-- INSERT AMENITIES 
-- ============================================================================

-- SECURITY AMENITIES
INSERT INTO amenities (amenity_id, amenity_name, amenity_type, description, created_at, updated_at, deleted)
VALUES
    ('420e8400-e29b-41d4-a716-446655440001', 'Bảo vệ 24/7', 'ONSITE', 'Dịch vụ bảo vệ toàn thời gian', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440002', 'Camera giám sát', 'ONSITE', 'Hệ thống camera giám sát', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440003', 'Bảo vệ VIP 24/7', 'ONSITE', 'Dịch vụ bảo vệ VIP toàn thời gian', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440004', 'Hệ thống báo động', 'ONSITE', 'Hệ thống báo động tự động', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440005', 'Kiểm soát ra vào', 'ONSITE', 'Hệ thống kiểm soát ra vào tự động', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440006', 'Cổng an ninh', 'ONSITE', 'Cổng an ninh có người gác', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440007', 'Hàng rào cao', 'ONSITE', 'Hàng rào an ninh cao', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440008', 'Hệ thống khóa thẻ', 'ONSITE', 'Hệ thống khóa thẻ từ', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440009', 'Hàng rào bao quanh', 'ONSITE', 'Hàng rào bao quanh bất động sản', NOW(), NOW(), FALSE),
    
-- COMFORT & CLIMATE
    ('420e8400-e29b-41d4-a716-446655440010', 'Điều hòa tập trung', 'ONSITE', 'Hệ thống điều hòa tập trung', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440011', 'Nước nóng', 'ONSITE', 'Hệ thống cung cấp nước nóng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440012', 'Hệ thống sưởi ấm', 'ONSITE', 'Hệ thống sưởi ấm', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440013', 'Lò sưởi', 'ONSITE', 'Lò sưởi', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440014', 'Nhà thông minh', 'ONSITE', 'Công nghệ nhà thông minh', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440015', 'Phòng spa', 'ONSITE', 'Phòng spa tiện nghi', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440016', 'Phòng xông hơi', 'ONSITE', 'Phòng xông hơi sauna', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440017', 'Phòng chiếu phim', 'ONSITE', 'Phòng chiếu phim trong nhà', NOW(), NOW(), FALSE),
    
-- FACILITIES
    ('420e8400-e29b-41d4-a716-446655440018', 'Thang máy', 'ONSITE', 'Thang máy', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440019', 'Bãi đỗ xe', 'ONSITE', 'Bãi đỗ xe', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440020', 'Phòng kho', 'ONSITE', 'Phòng kho lưu trữ', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440021', 'Phòng vệ sinh', 'ONSITE', 'Phòng vệ sinh', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440022', 'Phòng họp', 'ONSITE', 'Phòng họp', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440023', 'Quán cà phê', 'ONSITE', 'Quán cà phê', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440024', 'Phòng bếp', 'ONSITE', 'Phòng bếp', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440025', 'Thang cuốn', 'ONSITE', 'Thang cuốn', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440026', 'Bãi đỗ xe nhiều tầng', 'ONSITE', 'Bãi đỗ xe nhiều tầng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440027', 'Rạp chiếu phim', 'ONSITE', 'Rạp chiếu phim', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440028', 'Khu ăn uống', 'ONSITE', 'Khu ăn uống', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440029', 'Khu trò chơi trẻ em', 'ONSITE', 'Khu trò chơi trẻ em', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440030', 'Bếp thương mại', 'ONSITE', 'Bếp thương mại', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440031', 'Khu ăn uống riêng', 'ONSITE', 'Khu ăn uống riêng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440032', 'Nhà hàng', 'ONSITE', 'Nhà hàng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440033', 'Quán cà phê riêng', 'ONSITE', 'Quán cà phê', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440034', 'Hồ bơi', 'ONSITE', 'Hồ bơi', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440035', 'Trung tâm thể dục', 'ONSITE', 'Trung tâm thể dục', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440036', 'Phòng hội thảo', 'ONSITE', 'Phòng hội thảo', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440037', 'Trung tâm dịch vụ', 'ONSITE', 'Trung tâm dịch vụ', NOW(), NOW(), FALSE),
    
-- OUTDOOR & FEATURES
    ('420e8400-e29b-41d4-a716-446655440038', 'Ban công', 'ONSITE', 'Ban công', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440039', 'Sân vườn chung', 'ONSITE', 'Sân vườn chung', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440040', 'Sân vườn riêng', 'ONSITE', 'Sân vườn riêng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440041', 'Sân hiên', 'ONSITE', 'Sân hiên', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440042', 'Nhà để xe', 'ONSITE', 'Nhà để xe', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440043', 'Hồ bơi riêng', 'ONSITE', 'Hồ bơi riêng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440044', 'Sân tennis riêng', 'ONSITE', 'Sân tennis riêng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440045', 'Sân vườn cảnh quan', 'ONSITE', 'Sân vườn cảnh quan', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440046', 'Sân chơi trẻ em', 'ONSITE', 'Sân chơi trẻ em', NOW(), NOW(), FALSE),
    
-- SERVICES & UTILITIES
    ('420e8400-e29b-41d4-a716-446655440047', 'WiFi miễn phí', 'ONSITE', 'Dịch vụ WiFi miễn phí', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440048', 'WiFi cao tốc', 'ONSITE', 'Dịch vụ WiFi cao tốc', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440049', 'Điện dự phòng', 'ONSITE', 'Hệ thống điện dự phòng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440050', 'Bồn chứa nước', 'ONSITE', 'Bồn chứa nước', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440051', 'Dịch vụ vệ sinh', 'ONSITE', 'Dịch vụ vệ sinh', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440052', 'Dịch vụ bảo trì', 'ONSITE', 'Dịch vụ bảo trì', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440053', 'Dịch vụ giặt', 'ONSITE', 'Dịch vụ giặt', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440054', 'Lễ tân', 'ONSITE', 'Dịch vụ lễ tân 24/7', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440055', 'Hệ thống thanh toán', 'ONSITE', 'Hệ thống thanh toán tự động', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440056', 'Dịch vụ hộp tư vấn', 'ONSITE', 'Dịch vụ hộp tư vấn', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440057', 'Dịch vụ valet', 'ONSITE', 'Dịch vụ valet parking', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440058', 'Dịch vụ giúp việc', 'ONSITE', 'Dịch vụ giúp việc', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440059', 'Dịch vụ phòng', 'ONSITE', 'Dịch vụ phòng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440060', 'Dịch vụ chăm sóc', 'ONSITE', 'Dịch vụ chăm sóc turndown', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440061', 'Cây ATM', 'ONSITE', 'Cây ATM', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440062', 'Điện 3 pha', 'ONSITE', 'Điện 3 pha', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440063', 'Cấp nước', 'ONSITE', 'Hệ thống cấp nước', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440064', 'Hệ thống thoát nước', 'ONSITE', 'Hệ thống thoát nước', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440065', 'Năng lượng mặt trời', 'ONSITE', 'Năng lượng mặt trời', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440066', 'Hệ thống lọc nước', 'ONSITE', 'Hệ thống lọc nước', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440067', 'Máy phát điện dự phòng', 'ONSITE', 'Máy phát điện dự phòng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440068', 'Xử lý nước thải', 'ONSITE', 'Xử lý nước thải', NOW(), NOW(), FALSE),
    
-- COMMUNITY & AMENITIES
    ('420e8400-e29b-41d4-a716-446655440069', 'Phòng tập gym', 'ONSITE', 'Phòng tập gym', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440070', 'Trung tâm cộng đồng', 'ONSITE', 'Trung tâm cộng đồng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440071', 'Phòng chờ chung', 'ONSITE', 'Phòng chờ chung', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440072', 'Phòng rượu', 'ONSITE', 'Phòng rượu', NOW(), NOW(), FALSE),
    
-- INDUSTRIAL ACCESS & FACILITIES
    ('420e8400-e29b-41d4-a716-446655440073', 'Cổng vào rộng', 'ONSITE', 'Cổng vào rộng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440074', 'Bãi xếp hàng', 'ONSITE', 'Bãi xếp hàng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440075', 'Đường trải nhựa', 'ONSITE', 'Đường trải nhựa', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440076', 'Khu văn phòng', 'ONSITE', 'Khu văn phòng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440077', 'Chỗ để nâng hàng', 'ONSITE', 'Chỗ để nâng hàng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440078', 'Kho ngoài trời', 'ONSITE', 'Kho ngoài trời', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440079', 'Khu sản xuất', 'ONSITE', 'Khu sản xuất', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440080', 'Căn tin', 'ONSITE', 'Căn tin', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440081', 'Khu xưởng', 'ONSITE', 'Khu xưởng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440082', 'Nhiều cổng vào', 'ONSITE', 'Nhiều cổng vào', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440083', 'Chỗ đỗ xe tải', 'ONSITE', 'Chỗ đỗ xe tải', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440084', 'Sân ngoài trời', 'ONSITE', 'Sân ngoài trời', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440085', 'Kho lạnh', 'ONSITE', 'Kho lạnh', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440086', 'Kho chính', 'ONSITE', 'Kho chính', NOW(), NOW(), FALSE),
    
-- LAND & INFRASTRUCTURE
    ('420e8400-e29b-41d4-a716-446655440087', 'Tiếp cận đường chính', 'ONSITE', 'Tiếp cận đường chính', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440088', 'Đất bằng phẳng', 'ONSITE', 'Đất bằng phẳng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440089', 'Cây xanh sẵn', 'ONSITE', 'Cây xanh sẵn', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440090', 'Lưu thông cao', 'ONSITE', 'Lưu thông cao', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440091', 'Tầm nhìn tốt', 'ONSITE', 'Tầm nhìn tốt', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440092', 'Đường lưu thông xe tải', 'ONSITE', 'Đường lưu thông xe tải', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440093', 'Tiếp cận đường sắt', 'ONSITE', 'Tiếp cận đường sắt', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440094', 'Tiếp cận đường thủy', 'ONSITE', 'Tiếp cận đường thủy', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440095', 'Khu công nghiệp', 'ONSITE', 'Khu công nghiệp', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440096', 'Tiếp cận sông/kênh', 'ONSITE', 'Tiếp cận sông/kênh', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440097', 'Giếng nước', 'ONSITE', 'Giếng nước', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440098', 'Hệ thống tưới sẵn', 'ONSITE', 'Hệ thống tưới sẵn', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440099', 'Thoát nước tốt', 'ONSITE', 'Thoát nước tốt', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440100', 'Đất béo phì', 'ONSITE', 'Đất béo phì', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440101', 'Cây che bóng', 'ONSITE', 'Cây che bóng', NOW(), NOW(), FALSE),
    
-- PROXIMITY AMENITIES (OFFSITE)
    ('420e8400-e29b-41d4-a716-446655440102', 'Gần bệnh viện (< 2km)', 'OFFSITE', 'Gần bệnh viện', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440103', 'Gần trường học (< 2km)', 'OFFSITE', 'Gần trường học', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440104', 'Gần chợ/siêu thị (< 1km)', 'OFFSITE', 'Gần chợ/siêu thị', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440105', 'Gần trạm MRT (< 500m)', 'OFFSITE', 'Gần trạm MRT', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440106', 'Gần nhà hàng (< 1km)', 'OFFSITE', 'Gần nhà hàng', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440107', 'Gần công viên (< 1km)', 'OFFSITE', 'Gần công viên', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440108', 'Gần sân bay (< 10km)', 'OFFSITE', 'Gần sân bay', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440109', 'Gần khu kinh doanh', 'OFFSITE', 'Gần khu kinh doanh', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440110', 'Gần trường quốc tế (< 3km)', 'OFFSITE', 'Gần trường quốc tế', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440111', 'Gần sân golf (< 5km)', 'OFFSITE', 'Gần sân golf', NOW(), NOW(), FALSE),
    ('420e8400-e29b-41d4-a716-446655440112', 'Vị trí lưu thông cao', 'OFFSITE', 'Vị trí lưu thông cao', NOW(), NOW(), FALSE);
