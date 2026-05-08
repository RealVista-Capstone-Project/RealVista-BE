package com.sep.realvista.domain.property.claim;

import com.sep.realvista.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "property_claims", indexes = {
        @Index(name = "idx_claim_property",  columnList = "property_id"),
        @Index(name = "idx_claim_claimant",  columnList = "claimant_id"),
        @Index(name = "idx_claim_status",    columnList = "status"),
        @Index(name = "idx_claim_expires",   columnList = "expires_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PropertyClaim extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "claim_id")
    private UUID claimId;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "claimant_id", nullable = false)
    private UUID claimantId;

    @Column(name = "claim_reason", nullable = false, length = 50)
    private String claimReason;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PropertyClaimStatus status = PropertyClaimStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public void confirm() {
        this.status = PropertyClaimStatus.CONFIRMED;
        this.resolvedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = PropertyClaimStatus.REJECTED;
        this.resolvedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = PropertyClaimStatus.EXPIRED;
        this.resolvedAt = LocalDateTime.now();
    }

    public void escalate() {
        this.status = PropertyClaimStatus.ESCALATED;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
