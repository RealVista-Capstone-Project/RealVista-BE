package com.sep.realvista.application.listing.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @NotNull(message = "Property ID is required")
    @JsonProperty("property_id")
    private UUID propertyId;

    @NotNull(message = "Renter ID is required")
    @JsonProperty("renter_id")
    private UUID renterId;

    @NotNull(message = "Landlord ID is required")
    @JsonProperty("landlord_id")
    private UUID landlordId;

    @JsonProperty("agent_id")
    private UUID agentId;

    @JsonProperty("lease_start_date")
    private LocalDate leaseStartDate;

    @JsonProperty("lease_end_date")
    private LocalDate leaseEndDate;

    @NotNull(message = "Lease duration in months is required")
    @Min(value = 1, message = "Lease duration must be at least 1 month")
    @JsonProperty("lease_duration_months")
    private Integer leaseDurationMonths;

    @DecimalMin(value = "0.0", inclusive = false, message = "Monthly rent must be positive")
    @JsonProperty("monthly_rent")
    private BigDecimal monthlyRent;

    @DecimalMin(value = "0.0", message = "Security deposit cannot be negative")
    @JsonProperty("security_deposit")
    private BigDecimal securityDeposit;

    /** URL to the lease document PDF already uploaded to storage (e.g. DigitalOcean Spaces). */
    @JsonProperty("lease_document_url")
    private String leaseDocumentUrl;
}
