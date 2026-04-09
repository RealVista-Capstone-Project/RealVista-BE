package com.sep.realvista.application.billing.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CheckoutResponse {
    private String checkoutOrderId;
    private Long orderCode;
    private String checkoutUrl;
    private String qrCode;
    private String paymentMethod;
    private String planName;
    private Long amount;
}
