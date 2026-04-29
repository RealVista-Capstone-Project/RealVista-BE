package com.sep.realvista.domain.billing.snapshot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "boost_package_snapshots", indexes = {
        @Index(name = "idx_bp_snapshot_package", columnList = "boost_package_id"),
        @Index(name = "idx_bp_snapshot_created", columnList = "created_at"),
        @Index(name = "idx_bp_snapshot_reason",  columnList = "snapshot_reason")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BoostPackageSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "snapshot_id")
    private UUID snapshotId;

    @Column(name = "boost_package_id", nullable = false)
    private UUID boostPackageId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "featured_quota", nullable = false)
    private Integer featuredQuota;

    @Column(name = "hot_badge_quota", nullable = false)
    private Integer hotBadgeQuota;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "snapshot_reason", nullable = false, length = 20)
    private SnapshotReason snapshotReason;

    @Column(name = "changed_by_user_id")
    private UUID changedByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
