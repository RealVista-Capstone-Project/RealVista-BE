package com.sep.realvista.domain.engagement.rental;

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
import java.util.UUID;

@Entity
@Table(name = "tenant_applications", indexes = {
        @Index(name = "idx_tenant_app_user", columnList = "user_id"),
        @Index(name = "idx_tenant_app_listing", columnList = "listing_id"),
        @Index(name = "idx_tenant_app_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TenantApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tenant_application_id")
    private UUID tenantApplicationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private Listing listing;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "monthly_income", precision = 15, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "move_in_date")
    private LocalDate moveInDate;

    @Column(name = "lease_term_months")
    private Integer leaseTermMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TenantApplicationStatus status = TenantApplicationStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String note;

    public void activate() {
        this.status = TenantApplicationStatus.ACTIVE;
    }

    public void archive() {
        this.status = TenantApplicationStatus.ARCHIVED;
    }
}
