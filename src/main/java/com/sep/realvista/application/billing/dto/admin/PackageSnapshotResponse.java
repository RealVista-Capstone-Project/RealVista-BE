package com.sep.realvista.application.billing.dto.admin;

import com.sep.realvista.domain.billing.snapshot.BoostPackageSnapshot;
import com.sep.realvista.domain.billing.snapshot.FeaturePackageSnapshot;
import com.sep.realvista.domain.billing.snapshot.SnapshotReason;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PackageSnapshotResponse {

    private UUID snapshotId;
    private UUID packageId;
    private String code;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private SnapshotReason snapshotReason;
    private UUID changedByUserId;
    private Instant createdAt;

    // FeaturePackage specific
    private String featureType;
    private Integer quota;

    // BoostPackage specific
    private Integer featuredQuota;
    private Integer hotBadgeQuota;

    private Boolean isActive;

    public static PackageSnapshotResponse from(FeaturePackageSnapshot s) {
        return PackageSnapshotResponse.builder()
                .snapshotId(s.getSnapshotId())
                .packageId(s.getFeaturePackageId())
                .code(s.getCode())
                .name(s.getName())
                .description(s.getDescription())
                .featureType(s.getFeatureType())
                .quota(s.getQuota())
                .durationDays(s.getDurationDays())
                .price(s.getPrice())
                .isActive(s.getIsActive())
                .snapshotReason(s.getSnapshotReason())
                .changedByUserId(s.getChangedByUserId())
                .createdAt(s.getCreatedAt())
                .build();
    }

    public static PackageSnapshotResponse from(BoostPackageSnapshot s) {
        return PackageSnapshotResponse.builder()
                .snapshotId(s.getSnapshotId())
                .packageId(s.getBoostPackageId())
                .code(s.getCode())
                .name(s.getName())
                .description(s.getDescription())
                .featuredQuota(s.getFeaturedQuota())
                .hotBadgeQuota(s.getHotBadgeQuota())
                .durationDays(s.getDurationDays())
                .price(s.getPrice())
                .isActive(s.getIsActive())
                .snapshotReason(s.getSnapshotReason())
                .changedByUserId(s.getChangedByUserId())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
