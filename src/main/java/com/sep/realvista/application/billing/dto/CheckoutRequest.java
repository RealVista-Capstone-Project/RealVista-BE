package com.sep.realvista.application.billing.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CheckoutRequest {

    @NotBlank(message = "planCode is required")
    private String planCode;

    @NotNull(message = "planType is required")
    private PlanType planType;

    @NotNull(message = "paymentMethod is required")
    private PaymentMethodRequest paymentMethod;

    public enum PlanType {
        SUBSCRIPTION, BOOST
    }

    public enum PaymentMethodRequest {
        PAYOS, VNPAY
    }
}
