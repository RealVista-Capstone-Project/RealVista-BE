package com.sep.realvista.infrastructure.payment.vnpay;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payment.vnpay")
public class VnPayProperties {
    private String tmnCode;
    private String hashSecret;
    private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    /**
     * Browser Return URL — must match merchant portal exactly (same string as sent in vnp_ReturnUrl).
     * FE forwards vnp_* query params from this page to the API verify endpoint.
     */
    private String returnUrl = "http://localhost:3000/vi/subscribe";
}
