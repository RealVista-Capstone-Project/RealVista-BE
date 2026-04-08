package com.sep.realvista.application.billing.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private String transactionId;
    private String planCode;
    private String planType;
    private String paymentMethod;
    private String status;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private String description;
}
