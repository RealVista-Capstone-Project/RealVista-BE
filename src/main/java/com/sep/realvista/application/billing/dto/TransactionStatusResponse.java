package com.sep.realvista.application.billing.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionStatusResponse {
    private String transactionId;
    private String status;
    private String planCode;
    private String planType;
}
