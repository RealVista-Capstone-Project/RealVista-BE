package com.sep.realvista.infrastructure.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationMessageService {

    private final Map<String, Map<String, String>> messages = new HashMap<>();

    public NotificationMessageService() {
        // Vietnamese (vi)
        Map<String, String> vi = new HashMap<>();
        vi.put("LISTING_UNPUBLISHED_TITLE", "Thông báo: Tin đăng tạm ẩn");
        vi.put("LISTING_UNPUBLISHED_MESSAGE", "Tin đăng cho '%s' hiện đang tạm thời "
                + "ngừng hoạt động để cập nhật. Lịch hẹn của bạn đang được giữ.");
        vi.put("PROPERTY_SOLD_TITLE", "Thông báo: Bất động sản đã bán");
        vi.put("PROPERTY_SOLD_MESSAGE", "Bất động sản '%s' đã được bán và không "
                + "còn khả dụng. Lịch hẹn của bạn đã bị hủy.");
        vi.put("PROPERTY_RENTED_TITLE", "Thông báo: Bất động sản đã cho thuê");
        vi.put("PROPERTY_RENTED_MESSAGE", "Bất động sản '%s' đã được cho thuê và không "
                + "còn khả dụng. Lịch hẹn của bạn đã bị hủy.");
        vi.put("AUTO_CANCELLATION_REASON", "Bất động sản không còn khả dụng "
                + "(đã bán/cho thuê/ngừng hoạt động).");
        
        // Error Messages (Vietnamese)
        vi.put("ERROR_RESOURCE_NOT_FOUND", "Không tìm thấy dữ liệu yêu cầu.");
        vi.put("ERROR_BUSINESS_CONFLICT", "Xung đột nghiệp vụ: %s");
        vi.put("ERROR_VALIDATION_FAILED", "Dữ liệu không hợp lệ.");
        vi.put("ERROR_ACCESS_DENIED", "Bạn không có quyền thực hiện thao tác này.");

        // English (en)
        Map<String, String> en = new HashMap<>();
        en.put("LISTING_UNPUBLISHED_TITLE", "Notification: Listing Temporarily Hidden");
        en.put("LISTING_UNPUBLISHED_MESSAGE", "The listing for '%s' is temporarily "
                + "off-market for updates. Your appointment is on hold.");
        en.put("PROPERTY_SOLD_TITLE", "Notification: Property Sold");
        en.put("PROPERTY_SOLD_MESSAGE", "The property '%s' has been sold and is no "
                + "longer available. Your appointment has been cancelled.");
        en.put("PROPERTY_RENTED_TITLE", "Notification: Property Rented");
        en.put("PROPERTY_RENTED_MESSAGE", "The property '%s' has been rented and is no "
                + "longer available. Your appointment has been cancelled.");
        en.put("AUTO_CANCELLATION_REASON", "Property no longer available "
                + "(sold/rented/off-market).");

        // Error Messages (English)
        en.put("ERROR_RESOURCE_NOT_FOUND", "Requested resource not found.");
        en.put("ERROR_BUSINESS_CONFLICT", "Business conflict: %s");
        en.put("ERROR_VALIDATION_FAILED", "Validation failed.");
        en.put("ERROR_ACCESS_DENIED", "Access denied.");

        messages.put("vi", vi);
        messages.put("en", en);
    }

    public String getMessage(String key, String lang) {
        String language = (lang == null || !messages.containsKey(lang.toLowerCase())) 
                ? "vi" : lang.toLowerCase();
        return messages.get(language).getOrDefault(key, key);
    }

    public String getMessage(String key, String lang, Object... args) {
        String template = getMessage(key, lang);
        return String.format(template, args);
    }
}
