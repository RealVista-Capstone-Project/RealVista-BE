package com.sep.realvista.application.billing.dto.admin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateBoostPackageRequest {

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    private String description;

    @Min(value = 0, message = "Featured quota must be non-negative")
    private Integer featuredQuota;

    @Min(value = 0, message = "Hot badge quota must be non-negative")
    private Integer hotBadgeQuota;

    @Min(value = -1, message = "Duration days must be -1 (no expiration) or positive")
    private Integer durationDays;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    private BigDecimal price;
}
