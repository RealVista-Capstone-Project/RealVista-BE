package com.sep.realvista.domain.listing.contract;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.property.Property;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lease_agreements", indexes = {
        @Index(name = "idx_lease_property", columnList = "property_id"),
        @Index(name = "idx_lease_renter", columnList = "renter_id"),
        @Index(name = "idx_lease_landlord", columnList = "landlord_id"),
        @Index(name = "idx_lease_agent", columnList = "agent_id"),
        @Index(name = "idx_lease_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LeaseAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "lease_agreement_id")
    private UUID leaseAgreementId;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", insertable = false, updatable = false)
    private Property property;

    @Column(name = "renter_id", nullable = false)
    private UUID renterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", insertable = false, updatable = false)
    private com.sep.realvista.domain.user.User renter;

    @Column(name = "landlord_id", nullable = false)
    private UUID landlordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", insertable = false, updatable = false)
    private com.sep.realvista.domain.user.User landlord;

    @Column(name = "agent_id")
    private UUID agentId;

    @Column(name = "lease_start_date")
    private LocalDate leaseStartDate;

    @Column(name = "lease_end_date")
    private LocalDate leaseEndDate;

    @Column(name = "lease_duration_months", nullable = false)
    private Integer leaseDurationMonths;

    @Column(name = "monthly_rent", precision = 12, scale = 2)
    private BigDecimal monthlyRent;

    @Column(name = "security_deposit", precision = 12, scale = 2)
    private BigDecimal securityDeposit;

    @Column(name = "lease_document_url", columnDefinition = "TEXT")
    private String leaseDocumentUrl;

    @Column(name = "signed_by_renter_at")
    private LocalDateTime signedByRenterAt;

    @Column(name = "signed_by_landlord_at")
    private LocalDateTime signedByLandlordAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private LeaseStatus status = LeaseStatus.DRAFT;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "termination_reason", columnDefinition = "TEXT")
    private String terminationReason;

    @Column(name = "terminated_at")
    private LocalDateTime terminatedAt;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    // ── DocuSign eSignature fields ──
    @Column(name = "docusign_envelope_id", length = 100)
    private String docusignEnvelopeId;

    @Column(name = "docusign_status", length = 50)
    private String docusignStatus;

    @Column(name = "signed_document_url", columnDefinition = "TEXT")
    private String signedDocumentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "signed_document_status", nullable = false, length = 30)
    @Builder.Default
    private SignedDocumentStatus signedDocumentStatus = SignedDocumentStatus.NOT_REQUESTED;

    @Column(name = "signed_document_error", columnDefinition = "TEXT")
    private String signedDocumentError;

    @Column(name = "signed_document_processed_at")
    private LocalDateTime signedDocumentProcessedAt;

    public void submitToLandlord() {
        this.status = LeaseStatus.PENDING_LANDLORD;
    }

    public void submitToRenter() {
        this.status = LeaseStatus.PENDING_RENTER;
    }

    public void renterSign() {
        this.signedByRenterAt = LocalDateTime.now();
        this.status = LeaseStatus.PENDING_LANDLORD;
    }

    public void landlordSign() {
        this.signedByLandlordAt = LocalDateTime.now();
        this.status = LeaseStatus.ACTIVE;
    }

    public void reject(String reason) {
        this.status = LeaseStatus.REJECTED;
        this.rejectReason = reason;
    }

    public void terminate(String reason) {
        if (this.status != LeaseStatus.ACTIVE) {
            throw new BusinessConflictException(
                    "Only an ACTIVE lease can be terminated. Current status: " + this.status);
        }
        this.status = LeaseStatus.TERMINATED;
        this.terminationReason = reason;
        this.terminatedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = LeaseStatus.EXPIRED;
    }

    // ── DocuSign business methods ──

    public void assignDocuSignEnvelope(String envelopeId) {
        this.docusignEnvelopeId = envelopeId;
        this.docusignStatus = "sent";
    }

    public void updateDocuSignStatus(String newDocuSignStatus) {
        this.docusignStatus = newDocuSignStatus;
    }

    public void renterSignViaDocuSign() {
        this.signedByRenterAt = LocalDateTime.now();
        this.docusignStatus = "completed";
        this.status = LeaseStatus.ACTIVE;
    }

    public void landlordSignViaDocuSign() {
        this.signedByLandlordAt = LocalDateTime.now();
        this.docusignStatus = "completed";
        this.status = LeaseStatus.ACTIVE;
    }

    public void setLeaseDocumentUrl(String url) {
        this.leaseDocumentUrl = url;
    }

    public void markSignedDocumentPending() {
        if (this.signedDocumentStatus != SignedDocumentStatus.COMPLETED) {
            this.signedDocumentStatus = SignedDocumentStatus.PENDING;
            this.signedDocumentError = null;
        }
    }

    public void markSignedDocumentProcessing() {
        this.signedDocumentStatus = SignedDocumentStatus.PROCESSING;
        this.signedDocumentError = null;
    }

    public void completeSignedDocument(String documentUrl) {
        this.signedDocumentUrl = documentUrl;
        this.signedDocumentStatus = SignedDocumentStatus.COMPLETED;
        this.signedDocumentError = null;
        this.signedDocumentProcessedAt = LocalDateTime.now();
    }

    public void failSignedDocumentProcessing(String errorMessage) {
        this.signedDocumentStatus = SignedDocumentStatus.FAILED;
        this.signedDocumentError = errorMessage;
        this.signedDocumentProcessedAt = LocalDateTime.now();
    }
}
