package com.sep.realvista.application.billing.dto.admin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateBoostPackageRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    private String description;

    @NotNull(message = "Featured quota is required")
    @Min(value = 0, message = "Featured quota must be non-negative")
    private Integer featuredQuota;

    @NotNull(message = "Hot badge quota is required")
    @Min(value = 0, message = "Hot badge quota must be non-negative")
    private Integer hotBadgeQuota;

    @NotNull(message = "Duration days is required")
    @Min(value = -1, message = "Duration days must be -1 (no expiration) or a positive number")
    private Integer durationDays;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    private BigDecimal price;
}
