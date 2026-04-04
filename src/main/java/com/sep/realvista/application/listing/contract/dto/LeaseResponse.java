package com.sep.realvista.application.listing.contract.dto;

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

    private UUID leaseAgreementId;
    private UUID listingId;
    private UUID renterId;
    private UUID landlordId;
    private UUID agentId;

    private LocalDate leaseStartDate;
    private LocalDate leaseEndDate;
    private Integer leaseDurationMonths;

    private BigDecimal monthlyRent;
    private BigDecimal securityDeposit;

    private String leaseDocumentUrl;

    private LocalDateTime signedByRenterAt;
    private LocalDateTime signedByLandlordAt;

    private LeaseStatus status;
    private String rejectReason;
    private UUID verifiedBy;

    /** DocuSign envelope ID (null if not yet sent for signing). */
    private String docusignEnvelopeId;

    /** Current DocuSign envelope status (e.g. "sent", "completed", "voided"). */
    private String docusignStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
