package com.sep.realvista.application.billing.dto.admin;

import com.sep.realvista.domain.billing.subscription.FeaturePackage;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FeaturePackageAdminResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private String featureType;
    private Integer quota;
    private Integer durationDays;
    private BigDecimal price;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FeaturePackageAdminResponse from(FeaturePackage fp) {
        return FeaturePackageAdminResponse.builder()
                .id(fp.getFeaturePackageId())
                .code(fp.getCode())
                .name(fp.getName())
                .description(fp.getDescription())
                .featureType(fp.getFeatureType() != null ? fp.getFeatureType().name() : null)
                .quota(fp.getQuota())
                .durationDays(fp.getDurationDays())
                .price(fp.getPrice())
                .isActive(fp.getIsActive())
                .createdAt(fp.getCreatedAt())
                .updatedAt(fp.getUpdatedAt())
                .build();
    }
}
