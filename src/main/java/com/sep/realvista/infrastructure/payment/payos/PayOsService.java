package com.sep.realvista.infrastructure.payment.payos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOsService {

    private final PayOsProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Creates a PayOS payment link and returns the response data node.
     * Signature is computed over: amount, cancelUrl, description, orderCode, returnUrl (sorted alphabetically).
     */
    public PayOsPaymentResult createPaymentLink(
            long orderCode,
            int amount,
            String description,
            String returnUrl,
            String cancelUrl,
            long expiredAt
    ) {
        try {
            String signature = buildSignature(orderCode, amount, description, returnUrl, cancelUrl);

            Map<String, Object> body = new HashMap<>();
            body.put("orderCode", orderCode);
            body.put("amount", amount);
            body.put("description", description);
            body.put("returnUrl", returnUrl);
            body.put("cancelUrl", cancelUrl);
            body.put("expiredAt", expiredAt);
            body.put("signature", signature);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", properties.getClientId());
            headers.set("x-api-key", properties.getApiKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            String url = properties.getApiUrl() + "/v2/payment-requests";
            String response = restTemplate.postForObject(url, request, String.class);

            JsonNode root = objectMapper.readTree(response);
            if (!"00".equals(root.path("code").asText())) {
                throw new RuntimeException("PayOS error: " + root.path("desc").asText());
            }

            JsonNode data = root.path("data");
            return PayOsPaymentResult.builder()
                    .checkoutUrl(data.path("checkoutUrl").asText())
                    .qrCode(data.path("qrCode").asText())
                    .paymentLinkId(data.path("paymentLinkId").asText())
                    .expiredAt(data.path("expiredAt").asLong(expiredAt))
                    .build();

        } catch (Exception e) {
            log.error("Failed to create PayOS payment link for orderCode={}", orderCode, e);
            throw new RuntimeException("Could not create PayOS payment link", e);
        }
    }

    /**
     * Fetches payment link state from PayOS (by shop {@code orderCode} or payment link id).
     * See PayOS "Lấy thông tin link thanh toán" — GET /v2/payment-requests/{id}.
     */
    public PayOsPaymentRequestInfo fetchPaymentRequestByOrderCode(long orderCode) {
        try {
            String url = properties.getApiUrl() + "/v2/payment-requests/" + orderCode;

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", properties.getClientId());
            headers.set("x-api-key", properties.getApiKey());

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            if (!"00".equals(root.path("code").asText())) {
                throw new RuntimeException("PayOS error: " + root.path("desc").asText());
            }

            JsonNode data = root.path("data");
            return PayOsPaymentRequestInfo.builder()
                    .status(data.path("status").asText(""))
                    .orderCode(data.path("orderCode").asLong(orderCode))
                    .amount(data.path("amount").asLong(0))
                    .amountPaid(data.path("amountPaid").asLong(0))
                    .amountRemaining(data.path("amountRemaining").asLong(0))
                    .build();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch PayOS payment request for orderCode={}", orderCode, e);
            throw new RuntimeException("Could not fetch PayOS payment status", e);
        }
    }

    /**
     * Validates a PayOS webhook payload signature.
     * data fields are sorted alphabetically and signed with the checksum key.
     */
    public boolean verifyWebhookSignature(Map<String, Object> data, String receivedSignature) {
        if (receivedSignature == null || receivedSignature.isBlank()) {
            return false;
        }
        try {
            String dataString = buildSortedDataString(data);
            String expected = hmacSha256(dataString, properties.getChecksumKey());
            return expected.equalsIgnoreCase(receivedSignature.trim());
        } catch (Exception e) {
            log.error("Failed to verify PayOS webhook signature", e);
            return false;
        }
    }

    // -------------------------------------------------------------------------

    private String buildSignature(long orderCode, int amount, String description, String returnUrl, String cancelUrl) {
        String data = "amount=" + amount
                + "&cancelUrl=" + cancelUrl
                + "&description=" + description
                + "&orderCode=" + orderCode
                + "&returnUrl=" + returnUrl;
        return hmacSha256(data, properties.getChecksumKey());
    }

    /**
     * PayOS payment-requests webhook: sort keys alphabetically, join as {@code key=value&...}.
     * Values must match PayOS docs (null → empty string; numbers without trailing ".0").
     */
    private String buildSortedDataString(Map<String, Object> data) {
        return data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + formatWebhookFieldValue(e.getValue()))
                .reduce((a, b) -> a + "&" + b)
                .orElse("");
    }

    private static String formatWebhookFieldValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Boolean b) {
            return Boolean.toString(b);
        }
        if (value instanceof java.math.BigDecimal bd) {
            return bd.stripTrailingZeros().toPlainString();
        }
        if (value instanceof java.math.BigInteger bi) {
            return bi.toString();
        }
        if (value instanceof Long l) {
            return Long.toString(l);
        }
        if (value instanceof Integer i) {
            return Integer.toString(i);
        }
        if (value instanceof Short s) {
            return Short.toString(s);
        }
        if (value instanceof Byte b) {
            return Byte.toString(b);
        }
        if (value instanceof Double d) {
            if (Double.isFinite(d) && d == Math.rint(d)) {
                return Long.toString(d.longValue());
            }
            return Double.toString(d);
        }
        if (value instanceof Float f) {
            if (Float.isFinite(f) && f == Math.rint(f)) {
                return Long.toString(f.longValue());
            }
            return Float.toString(f);
        }
        return String.valueOf(value);
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 computation failed", e);
        }
    }

}
