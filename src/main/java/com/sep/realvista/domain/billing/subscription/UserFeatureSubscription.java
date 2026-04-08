package com.sep.realvista.domain.billing.subscription;

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

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_feature_subscriptions", indexes = {
        @Index(name = "idx_user_feature_sub_user", columnList = "user_id"),
        @Index(name = "idx_user_feature_sub_package", columnList = "feature_package_id"),
        @Index(name = "idx_user_feature_sub_status", columnList = "status"),
        @Index(name = "idx_user_feature_sub_user_status", columnList = "user_id, status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserFeatureSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_feature_subscription_id")
    private UUID userFeatureSubscriptionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "feature_package_id", nullable = false)
    private UUID featurePackageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_package_id", insertable = false, updatable = false)
    private FeaturePackage featurePackage;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "remaining_quota")
    private Integer remainingQuota;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserFeatureSubscriptionStatus status = UserFeatureSubscriptionStatus.ACTIVE;

    public boolean isExpired() {
        if (endDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(endDate);
    }

    public boolean isExhausted() {
        if (remainingQuota == null) {
            return false;
        }
        return remainingQuota <= 0;
    }

    public boolean isUsable() {
        return status == UserFeatureSubscriptionStatus.ACTIVE && !isExpired() && !isExhausted();
    }

    public void useQuota(int amount) {
        if (remainingQuota != null && remainingQuota > 0) {
            this.remainingQuota = Math.max(0, this.remainingQuota - amount);
            if (this.remainingQuota <= 0) {
                this.status = UserFeatureSubscriptionStatus.EXHAUSTED;
            }
        }
    }

    public void cancel() {
        this.status = UserFeatureSubscriptionStatus.CANCELLED;
    }

    public void expire() {
        this.status = UserFeatureSubscriptionStatus.EXPIRED;
    }

    public void exhaust() {
        this.status = UserFeatureSubscriptionStatus.EXHAUSTED;
    }
}
