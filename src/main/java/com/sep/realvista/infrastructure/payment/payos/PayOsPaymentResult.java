package com.sep.realvista.infrastructure.payment.payos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayOsPaymentResult {
    private final String checkoutUrl;
    private final String qrCode;
    private final String paymentLinkId;
}
