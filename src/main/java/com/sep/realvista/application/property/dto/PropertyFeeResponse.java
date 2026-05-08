package com.sep.realvista.application.property.dto;

import com.sep.realvista.domain.property.fee.BillingCycle;
import com.sep.realvista.domain.property.fee.FeeType;
import com.sep.realvista.domain.property.fee.PropertyFeeService;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
public class PropertyFeeResponse {

    UUID propertyFeeServiceId;
    UUID propertyId;
    FeeType feeType;
    String feeName;
    BigDecimal amount;
    BillingCycle billingCycle;
    Boolean isOptional;
    String description;

    public static PropertyFeeResponse from(PropertyFeeService entity) {
        return PropertyFeeResponse.builder()
                .propertyFeeServiceId(entity.getPropertyFeeServiceId())
                .propertyId(entity.getPropertyId())
                .feeType(entity.getFeeType())
                .feeName(entity.getFeeName())
                .amount(entity.getAmount())
                .billingCycle(entity.getBillingCycle())
                .isOptional(entity.getIsOptional())
                .description(entity.getDescription())
                .build();
    }
}
