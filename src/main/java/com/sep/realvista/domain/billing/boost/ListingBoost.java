package com.sep.realvista.domain.billing.boost;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.listing.Listing;
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
import java.util.UUID;

@Entity
@Table(name = "listing_boosts", indexes = {
        @Index(name = "idx_listing_boost_package", columnList = "boost_package_id"),
        @Index(name = "idx_listing_boost_listing", columnList = "listing_id"),
        @Index(name = "idx_listing_boost_user", columnList = "user_id"),
        @Index(name = "idx_listing_boost_type", columnList = "boost_type"),
        @Index(name = "idx_listing_boost_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = {"user", "listing", "boostPackage"})
public class ListingBoost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "listing_boost_id")
    private UUID listingBoostId;

    @Column(name = "boost_package_id", nullable = false)
    private UUID boostPackageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boost_package_id", insertable = false, updatable = false)
    private BoostPackage boostPackage;

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private Listing listing;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "boost_type", nullable = false, length = 20)
    private BoostType boostType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ListingBoostStatus status = ListingBoostStatus.ACTIVE;

    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    public void cancel() {
        this.status = ListingBoostStatus.CANCELLED;
    }

    public void expire() {
        this.status = ListingBoostStatus.EXPIRED;
    }
}
