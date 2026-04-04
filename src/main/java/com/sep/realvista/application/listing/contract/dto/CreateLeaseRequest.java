package com.sep.realvista.application.listing.contract.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating a new lease agreement draft.
 */
@Data
public class CreateLeaseRequest {

    @NotNull(message = "Listing ID is required")
    private UUID listingId;

    @NotNull(message = "Renter ID is required")
    private UUID renterId;

    @NotNull(message = "Landlord ID is required")
    private UUID landlordId;

    private UUID agentId;

    private LocalDate leaseStartDate;

    private LocalDate leaseEndDate;

    @NotNull(message = "Lease duration in months is required")
    @Min(value = 1, message = "Lease duration must be at least 1 month")
    private Integer leaseDurationMonths;

    @DecimalMin(value = "0.0", inclusive = false, message = "Monthly rent must be positive")
    private BigDecimal monthlyRent;

    @DecimalMin(value = "0.0", message = "Security deposit cannot be negative")
    private BigDecimal securityDeposit;

    /** URL to the lease document PDF already uploaded to storage (e.g. DigitalOcean Spaces). */
    private String leaseDocumentUrl;
}
