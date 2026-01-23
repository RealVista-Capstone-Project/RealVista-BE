package com.sep.realvista.domain.listing.contract;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.listing.Listing;
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
        @Index(name = "idx_lease_listing", columnList = "listing_id"),
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

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private Listing listing;

    @Column(name = "renter_id", nullable = false)
    private UUID renterId;

    @Column(name = "landlord_id", nullable = false)
    private UUID landlordId;

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

    @Column(name = "verified_by")
    private UUID verifiedBy;

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

    public void terminate() {
        this.status = LeaseStatus.TERMINATED;
    }

    public void expire() {
        this.status = LeaseStatus.EXPIRED;
    }
}
