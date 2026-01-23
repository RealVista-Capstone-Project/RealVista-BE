package com.sep.realvista.domain.billing.subscription;

import com.sep.realvista.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "subscription_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SubscriptionPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "subscription_plan_id")
    private UUID subscriptionPlanId;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String roles;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_features", columnDefinition = "json")
    private String aiFeatures;

    @Column(name = "verify_enabled")
    @Builder.Default
    private Boolean verifyEnabled = false;

    @Column(name = "verify_cycle_days", nullable = false)
    @Builder.Default
    private Integer verifyCycleDays = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
