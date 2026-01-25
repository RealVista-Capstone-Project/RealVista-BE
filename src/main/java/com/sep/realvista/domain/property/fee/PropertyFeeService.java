package com.sep.realvista.domain.property.fee;

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
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "property_fee_services", indexes = {
        @Index(name = "idx_property_fee_property", columnList = "property_id"),
        @Index(name = "idx_property_fee_type", columnList = "fee_type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = {"property"})
public class PropertyFeeService extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "property_fee_service_id")
    private UUID propertyFeeServiceId;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", insertable = false, updatable = false)
    private Property property;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 50)
    private FeeType feeType;

    @Column(name = "fee_name", nullable = false, length = 100)
    private String feeName;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 20)
    private BillingCycle billingCycle;

    @Column(name = "is_optional")
    @Builder.Default
    private Boolean isOptional = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    public void update(String feeName, BigDecimal amount, BillingCycle billingCycle, String description) {
        if (feeName != null && !feeName.isBlank()) {
            this.feeName = feeName;
        }
        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Amount cannot be negative");
            }
            this.amount = amount;
        }
        if (billingCycle != null) {
            this.billingCycle = billingCycle;
        }
        this.description = description;
    }

    public void markAsOptional() {
        this.isOptional = true;
    }

    public void markAsRequired() {
        this.isOptional = false;
    }
}
