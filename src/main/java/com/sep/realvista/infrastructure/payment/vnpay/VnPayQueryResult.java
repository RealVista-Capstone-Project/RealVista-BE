package com.sep.realvista.infrastructure.payment.vnpay;

/**
 * Parsed VNPay QueryDR JSON response (after signature verification).
 */
public record VnPayQueryResult(
        String responseCode,
        String message,
        String transactionStatus,
        Long amount,
        String txnRef,
        String transactionNo,
        String bankCode
) {
    /** API-level success and payment settled at VNPay. */
    public boolean isSettled() {
        return "00".equals(responseCode) && "00".equals(transactionStatus);
    }

    /** Transaction not found yet (retry). */
    public boolean isPending() {
        return "91".equals(responseCode);
    }
}
