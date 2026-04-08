package com.sep.realvista.infrastructure.payment.payos;

import lombok.Builder;
import lombok.Value;

/**
 * Subset of PayOS GET /v2/payment-requests/{id} response {@code data} node.
 */
@Value
@Builder
public class PayOsPaymentRequestInfo {
    String status;
    long orderCode;
    long amount;
    long amountPaid;
    long amountRemaining;
}
