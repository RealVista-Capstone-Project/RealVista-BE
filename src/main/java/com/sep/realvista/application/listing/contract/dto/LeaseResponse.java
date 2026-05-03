package com.sep.realvista.application.listing.contract.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.domain.listing.contract.LeaseStatus;
import com.sep.realvista.domain.listing.contract.SignedDocumentStatus;
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

    @JsonProperty("termination_reason")
    private String terminationReason;

    @JsonProperty("terminated_at")
    private LocalDateTime terminatedAt;

    @JsonProperty("verified_by")
    private UUID verifiedBy;

    /** DocuSign envelope ID (null if not yet sent for signing). */
    @JsonProperty("docusign_envelope_id")
    private String docusignEnvelopeId;

    /** Current DocuSign envelope status (e.g. "sent", "completed", "voided"). */
    @JsonProperty("docusign_status")
    private String docusignStatus;

    /** Final signed DocuSign PDF URL after background upload completes. */
    @JsonProperty("signed_document_url")
    private String signedDocumentUrl;

    /** Background processing status for the final signed PDF. */
    @JsonProperty("signed_document_status")
    private SignedDocumentStatus signedDocumentStatus;

    /** Last background processing error, present only when status is FAILED. */
    @JsonProperty("signed_document_error")
    private String signedDocumentError;

    @JsonProperty("signed_document_processed_at")
    private LocalDateTime signedDocumentProcessedAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    // ── Enriched renter info ──────────────────────────────────────────────────

    @JsonProperty("renter_full_name")
    private String renterFullName;

    @JsonProperty("renter_email")
    private String renterEmail;

    @JsonProperty("renter_phone")
    private String renterPhone;

    @JsonProperty("renter_avatar_url")
    private String renterAvatarUrl;

    // ── Enriched landlord info ────────────────────────────────────────────────

    @JsonProperty("landlord_full_name")
    private String landlordFullName;

    @JsonProperty("landlord_email")
    private String landlordEmail;

    @JsonProperty("landlord_phone")
    private String landlordPhone;

    @JsonProperty("landlord_avatar_url")
    private String landlordAvatarUrl;

    // ── Enriched property info ────────────────────────────────────────────────

    @JsonProperty("property_title")
    private String propertyTitle;

    @JsonProperty("property_address")
    private String propertyAddress;

    @JsonProperty("property_type")
    private String propertyType;
}
