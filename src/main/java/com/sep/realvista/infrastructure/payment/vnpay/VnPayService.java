package com.sep.realvista.infrastructure.payment.vnpay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * VNPay pay URL + signature aligned with {@code pod-booking-system-server} {@code PaymentService}
 * (same field set, sort order, US_ASCII URL encoding in hash/query, HMAC key + data order).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VnPayService {

    private static final TimeZone VN_TZ = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");

    private final VnPayProperties properties;

    /**
     * @param txnRef    unique ref (UUID string); must be ASCII for signing charset used here
     * @param amount    amount in VND; sent as amount * 100 (same as FlexiPod {@code amount + "00"})
     * @param orderInfo ASCII-only description (e.g. {@code Thanh toan don hang:} + ref)
     * @param returnUrl must match VNPay merchant Return URL exactly (demo TMN often allows {@code http://localhost:3000/order-detail})
     * @param ipAddr    client IP
     */
    public String buildPaymentUrl(String txnRef, long amount, String orderInfo, String returnUrl, String ipAddr) {
        try {
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", properties.getTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf(amount) + "00");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", returnUrl);
            vnpParams.put("vnp_TxnRef", txnRef);
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_BankCode", "NCB");
            vnpParams.put("vnp_IpAddr", ipAddr);
            vnpParams.put("vnp_OrderType", "100000");
            vnpParams.put("vnp_OrderInfo", orderInfo);

            Calendar cld = Calendar.getInstance(VN_TZ);
            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
            formatter.setTimeZone(VN_TZ);
            String createDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_CreateDate", createDate);
            cld.add(Calendar.MINUTE, 15);
            String expireDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_ExpireDate", expireDate);

            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            String ascii = StandardCharsets.US_ASCII.name();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, ascii));
                    query.append(URLEncoder.encode(fieldName, ascii))
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, ascii));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String secureHash = hmacSha512(properties.getHashSecret(), hashData.toString());
            String queryUrl = query + "&vnp_SecureHash=" + secureHash;
            return properties.getPayUrl() + "?" + queryUrl;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("VNPay URL build failed", e);
        }
    }

    /**
     * Validates return query (same hash rules as FlexiPod / VNPay servlet sample).
     */
    public boolean verifyReturnSignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) {
            return false;
        }
        try {
            Map<String, String> signFields = new HashMap<>();
            for (Map.Entry<String, String> e : params.entrySet()) {
                String k = e.getKey();
                if (!"vnp_SecureHash".equals(k) && !"vnp_SecureHashType".equals(k) && e.getValue() != null) {
                    signFields.put(k, e.getValue());
                }
            }
            List<String> fieldNames = new ArrayList<>(signFields.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            String ascii = StandardCharsets.US_ASCII.name();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = signFields.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, ascii));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }
            String expected = hmacSha512(properties.getHashSecret(), hashData.toString());
            return expected.equalsIgnoreCase(receivedHash);
        } catch (UnsupportedEncodingException e) {
            log.warn("VNPay verify encoding error", e);
            return false;
        }
    }

    public boolean isSuccess(String vnpResponseCode) {
        return "00".equals(vnpResponseCode);
    }

    /** FlexiPod: {@code hmacSHA512(hashSecret, hashData)} */
    private static String hmacSha512(final String key, final String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA512 failed", e);
        }
    }
}
