package com.sep.realvista.domain.property.legal;

import com.sep.realvista.domain.common.entity.BaseEntity;
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

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "property_legal_documents", indexes = {
        @Index(name = "idx_property_legal_doc_property", columnList = "property_id"),
        @Index(name = "idx_property_legal_doc_type", columnList = "document_type"),
        @Index(name = "idx_property_legal_doc_status", columnList = "verification_status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PropertyLegalDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "property_legal_document_id")
    private UUID propertyLegalDocumentId;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", insertable = false, updatable = false)
    private Property property;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false, length = 100)
    private String documentNumber;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "issuing_authority", nullable = false)
    private String issuingAuthority;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "document_url", nullable = false, columnDefinition = "TEXT")
    private String documentUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    public void verify(UUID verifiedBy) {
        this.verificationStatus = VerificationStatus.VERIFIED;
        this.verifiedBy = verifiedBy;
    }

    public void reject(String reason, UUID verifiedBy) {
        this.verificationStatus = VerificationStatus.REJECTED;
        this.rejectReason = reason;
        this.verifiedBy = verifiedBy;
    }
}
