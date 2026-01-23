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
@Table(name = "user_subscriptions", indexes = {
        @Index(name = "idx_user_subscription_plan", columnList = "subscription_plan_id"),
        @Index(name = "idx_user_subscription_user", columnList = "user_id"),
        @Index(name = "idx_user_subscription_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_subscription_id")
    private UUID userSubscriptionId;

    @Column(name = "subscription_plan_id", nullable = false)
    private UUID subscriptionPlanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", insertable = false, updatable = false)
    private SubscriptionPlan subscriptionPlan;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserSubscriptionStatus status = UserSubscriptionStatus.ACTIVE;

    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    public void cancel() {
        this.status = UserSubscriptionStatus.CANCELLED;
    }

    public void expire() {
        this.status = UserSubscriptionStatus.EXPIRED;
    }
}
