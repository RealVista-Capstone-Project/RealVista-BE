package com.sep.realvista.application.listing.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.domain.listing.contract.LeaseStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for lease agreement data.
 */
@Data
@Builder
public class LeaseResponse {

    @JsonProperty("lease_agreement_id")
    private UUID leaseAgreementId;

    @JsonProperty("property_id")
    private UUID propertyId;

    @JsonProperty("renter_id")
    private UUID renterId;

    @JsonProperty("landlord_id")
    private UUID landlordId;

    @JsonProperty("agent_id")
    private UUID agentId;

    @JsonProperty("lease_start_date")
    private LocalDate leaseStartDate;

    @JsonProperty("lease_end_date")
    private LocalDate leaseEndDate;

    @JsonProperty("lease_duration_months")
    private Integer leaseDurationMonths;

    @JsonProperty("monthly_rent")
    private BigDecimal monthlyRent;

    @JsonProperty("security_deposit")
    private BigDecimal securityDeposit;

    @JsonProperty("lease_document_url")
    private String leaseDocumentUrl;

    @JsonProperty("signed_by_renter_at")
    private LocalDateTime signedByRenterAt;

    @JsonProperty("signed_by_landlord_at")
    private LocalDateTime signedByLandlordAt;

    @JsonProperty("status")
    private LeaseStatus status;

    @JsonProperty("reject_reason")
    private String rejectReason;

    @JsonProperty("verified_by")
    private UUID verifiedBy;

    /** DocuSign envelope ID (null if not yet sent for signing). */
    @JsonProperty("docusign_envelope_id")
    private String docusignEnvelopeId;

    /** Current DocuSign envelope status (e.g. "sent", "completed", "voided"). */
    @JsonProperty("docusign_status")
    private String docusignStatus;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
