package com.sep.realvista.application.billing.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutResponse {
    private String checkoutOrderId;
    private Long orderCode;
    private String checkoutUrl;
    /** Only present for PayOS — a VietQR string that can be rendered as a QR image. */
    private String qrCode;
    private String paymentMethod;
    private String planName;
    private Long amount;
}
