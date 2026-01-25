package com.sep.realvista.domain.agent;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.user.User;
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
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_licenses", indexes = {
        @Index(name = "idx_agent_license_profile", columnList = "agent_profile_id"),
        @Index(name = "idx_agent_license_status", columnList = "status"),
        @Index(name = "idx_agent_license_expiry", columnList = "expiry_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = {"agentProfile"})
public class AgentLicense extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "agent_license_id")
    private UUID agentLicenseId;

    @Column(name = "agent_profile_id", nullable = false)
    private UUID agentProfileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_profile_id", insertable = false, updatable = false)
    private AgentProfile agentProfile;

    @Column(name = "license_number", nullable = false, length = 100)
    private String licenseNumber;

    @Column(name = "issuing_authority", nullable = false)
    private String issuingAuthority;

    @Column(name = "license_type", nullable = false, length = 100)
    private String licenseType;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LicenseStatus status = LicenseStatus.PENDING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by", insertable = false, updatable = false)
    private User verifier;

    @Column(name = "verification_note", columnDefinition = "TEXT")
    private String verificationNote;

    @Column(name = "license_document_url", columnDefinition = "TEXT")
    private String licenseDocumentUrl;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public void verify(UUID verifierId, String note) {
        this.status = LicenseStatus.VERIFIED;
        this.verifiedBy = verifierId;
        this.verifiedAt = LocalDateTime.now();
        this.verificationNote = note;
    }

    public void reject(UUID verifierId, String reason) {
        this.status = LicenseStatus.REJECTED;
        this.verifiedBy = verifierId;
        this.verifiedAt = LocalDateTime.now();
        this.verificationNote = reason;
    }

    public void expire() {
        this.status = LicenseStatus.EXPIRED;
        this.isActive = false;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public boolean isValid() {
        return status == LicenseStatus.VERIFIED && !isExpired() && Boolean.TRUE.equals(isActive);
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
