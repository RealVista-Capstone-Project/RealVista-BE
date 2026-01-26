package com.sep.realvista.domain.listing.analytics;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "financial_calculations", indexes = {
        @Index(name = "idx_financial_calc_user", columnList = "user_id"),
        @Index(name = "idx_financial_calc_listing", columnList = "listing_id"),
        @Index(name = "idx_financial_calc_type", columnList = "calculation_type"),
        @Index(name = "idx_financial_calc_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FinancialCalculation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "calc_id")
    private UUID calcId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private Listing listing;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false, length = 50)
    private CalculationType calculationType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_params", columnDefinition = "json")
    private String inputParams;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CalculationStatus status = CalculationStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String result;

    public void complete(String result) {
        this.status = CalculationStatus.COMPLETED;
        this.result = result;
    }

    public void fail(String errorMessage) {
        this.status = CalculationStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
