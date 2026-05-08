package com.sep.realvista.application.property.dto;

import com.sep.realvista.domain.property.fee.BillingCycle;
import com.sep.realvista.domain.property.fee.FeeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePropertyFeeRequest {

    @NotNull(message = "Fee type is required")
    private FeeType feeType;

    @NotBlank(message = "Fee name is required")
    @Size(max = 100, message = "Fee name must not exceed 100 characters")
    private String feeName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", message = "Amount must be non-negative")
    private BigDecimal amount;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;

    private Boolean isOptional = false;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
