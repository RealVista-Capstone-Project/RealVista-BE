package com.sep.realvista.infrastructure.payment.vnpay;

/**
 * VNPay pay URL plus the {@code vnp_CreateDate} sent in the request (must be stored for QueryDR).
 */
public record VnPayPaymentLink(String checkoutUrl, String createDate) {
}
