package com.sep.realvista.domain.billing.boost;

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
@Table(name = "user_listing_boost_packages", indexes = {
        @Index(name = "idx_user_boost_pkg_user", columnList = "user_id"),
        @Index(name = "idx_user_boost_pkg_boost_pkg", columnList = "boost_package_id"),
        @Index(name = "idx_user_boost_pkg_status", columnList = "status"),
        @Index(name = "idx_user_boost_pkg_user_status", columnList = "user_id, status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserListingBoostPackage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_listing_boost_package_id")
    private UUID userListingBoostPackageId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "boost_package_id", nullable = false)
    private UUID boostPackageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boost_package_id", insertable = false, updatable = false)
    private BoostPackage boostPackage;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "remaining_featured_quota")
    private Integer remainingFeaturedQuota;

    @Column(name = "remaining_hot_badge_quota")
    private Integer remainingHotBadgeQuota;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserListingBoostPackageStatus status = UserListingBoostPackageStatus.ACTIVE;

    public boolean isExpired() {
        if (endDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(endDate);
    }

    public boolean isUsable() {
        return status == UserListingBoostPackageStatus.ACTIVE && !isExpired();
    }

    public void cancel() {
        this.status = UserListingBoostPackageStatus.CANCELLED;
    }

    public void expire() {
        this.status = UserListingBoostPackageStatus.EXPIRED;
    }

    public void decrementFeaturedQuota() {
        if (this.remainingFeaturedQuota == null || this.remainingFeaturedQuota <= 0) {
            throw new IllegalStateException("No remaining featured quota");
        }
        this.remainingFeaturedQuota--;
    }

    public void incrementFeaturedQuota() {
        if (this.remainingFeaturedQuota == null) {
            this.remainingFeaturedQuota = 1;
        } else {
            this.remainingFeaturedQuota++;
        }
    }

    public void decrementHotBadgeQuota() {
        if (this.remainingHotBadgeQuota == null || this.remainingHotBadgeQuota <= 0) {
            throw new IllegalStateException("No remaining hot badge quota");
        }
        this.remainingHotBadgeQuota--;
    }

    public void incrementHotBadgeQuota() {
        if (this.remainingHotBadgeQuota == null) {
            this.remainingHotBadgeQuota = 1;
        } else {
            this.remainingHotBadgeQuota++;
        }
    }
}
