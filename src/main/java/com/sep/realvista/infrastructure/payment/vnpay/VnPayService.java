package com.sep.realvista.infrastructure.payment.vnpay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

/**
 * VNPay pay URL + signature aligned with {@code pod-booking-system-server} {@code PaymentService}
 * (same field set, sort order, US_ASCII URL encoding in hash/query, HMAC key + data order).
 * QueryDR: <a href="https://sandbox.vnpayment.vn/apis/docs/truy-van-hoan-tien/querydr&refund.html">VNPay docs</a>.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VnPayService {

    private static final TimeZone VN_TZ = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");

    private final VnPayProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * @param txnRef    unique ref (UUID string); must be ASCII for signing charset used here
     * @param amount    amount in VND; sent as amount * 100 (same as FlexiPod {@code amount + "00"})
     * @param orderInfo ASCII-only description (e.g. {@code Thanh toan don hang:} + ref)
     * @param returnUrl must match VNPay merchant Return URL exactly (demo TMN often allows {@code http://localhost:3000/order-detail})
     * @param ipAddr    client IP
     */
    public VnPayPaymentLink buildPaymentUrl(
            String txnRef, long amount, String orderInfo, String returnUrl, String ipAddr) {
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
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
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
            return new VnPayPaymentLink(properties.getPayUrl() + "?" + queryUrl, createDate);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("VNPay URL build failed", e);
        }
    }

    /**
     * QueryDR: verify payment at VNPay. {@code transactionDate} is stored {@code vnp_CreateDate} from pay.
     */
    public VnPayQueryResult queryTransaction(String txnRef, String transactionDate, String ipAddr) {
        String requestId = UUID.randomUUID().toString().replace("-", "");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(VN_TZ);
        String createDate = formatter.format(new Date());

        String orderInfo = "Truy van GD:" + txnRef;

        String hashPlain = String.join("|",
                requestId,
                "2.1.0",
                "querydr",
                properties.getTmnCode(),
                txnRef,
                transactionDate,
                createDate,
                ipAddr,
                orderInfo
        );
        String secureHash = hmacSha512(properties.getHashSecret(), hashPlain);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("vnp_RequestId", requestId);
        body.put("vnp_Version", "2.1.0");
        body.put("vnp_Command", "querydr");
        body.put("vnp_TmnCode", properties.getTmnCode());
        body.put("vnp_TxnRef", txnRef);
        body.put("vnp_OrderInfo", orderInfo);
        body.put("vnp_TransactionDate", transactionDate);
        body.put("vnp_CreateDate", createDate);
        body.put("vnp_IpAddr", ipAddr);
        body.put("vnp_SecureHash", secureHash);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String raw;
        try {
            raw = restTemplate.postForObject(properties.getQueryUrl(), entity, String.class);
        } catch (RestClientException e) {
            log.warn("VNPay QueryDR HTTP error", e);
            throw e;
        }
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("VNPay QueryDR returned empty body");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(raw);
        } catch (Exception e) {
            throw new IllegalStateException("VNPay QueryDR response is not JSON", e);
        }

        verifyQueryDrResponseSignature(root);

        return parseQueryDrResult(root);
    }

    private void verifyQueryDrResponseSignature(JsonNode root) {
        String received = textOrEmpty(root, "vnp_SecureHash");
        if (received.isEmpty()) {
            throw new IllegalStateException("VNPay QueryDR response missing vnp_SecureHash");
        }
        // VNPay spec: data = vnp_ResponseId|vnp_Command|vnp_ResponseCode|vnp_Message|vnp_TmnCode|vnp_TxnRef|
        // vnp_Amount|vnp_BankCode|vnp_PayDate|vnp_TransactionNo|vnp_TransactionType|vnp_TransactionStatus|
        // vnp_OrderInfo|vnp_PromotionCode|vnp_PromotionAmount
        String data = String.join("|",
                textOrEmpty(root, "vnp_ResponseId"),
                textOrEmpty(root, "vnp_Command"),
                textOrEmpty(root, "vnp_ResponseCode"),
                textOrEmpty(root, "vnp_Message"),
                textOrEmpty(root, "vnp_TmnCode"),
                textOrEmpty(root, "vnp_TxnRef"),
                textOrEmpty(root, "vnp_Amount"),
                textOrEmpty(root, "vnp_BankCode"),
                textOrEmpty(root, "vnp_PayDate"),
                textOrEmpty(root, "vnp_TransactionNo"),
                textOrEmpty(root, "vnp_TransactionType"),
                textOrEmpty(root, "vnp_TransactionStatus"),
                textOrEmpty(root, "vnp_OrderInfo"),
                textOrEmpty(root, "vnp_PromotionCode"),
                textOrEmpty(root, "vnp_PromotionAmount")
        );
        String expected = hmacSha512(properties.getHashSecret(), data);
        if (!expected.equalsIgnoreCase(received)) {
            throw new IllegalStateException("VNPay QueryDR response signature invalid");
        }
    }

    private static VnPayQueryResult parseQueryDrResult(JsonNode root) {
        Long amount = null;
        if (root.hasNonNull("vnp_Amount")) {
            try {
                amount = root.get("vnp_Amount").asLong();
            } catch (Exception ignored) {
                try {
                    amount = Long.parseLong(root.get("vnp_Amount").asText());
                } catch (NumberFormatException ignored2) {
                    amount = null;
                }
            }
        }
        return new VnPayQueryResult(
                textOrEmpty(root, "vnp_ResponseCode"),
                textOrEmpty(root, "vnp_Message"),
                textOrEmpty(root, "vnp_TransactionStatus"),
                amount,
                textOrEmpty(root, "vnp_TxnRef"),
                textOrEmpty(root, "vnp_TransactionNo"),
                textOrEmpty(root, "vnp_BankCode")
        );
    }

    private static String textOrEmpty(JsonNode root, String field) {
        if (root == null || !root.has(field) || root.get(field).isNull()) {
            return "";
        }
        return root.get(field).asText();
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
