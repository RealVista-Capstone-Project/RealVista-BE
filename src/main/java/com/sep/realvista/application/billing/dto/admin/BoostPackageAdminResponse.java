package com.sep.realvista.application.billing.dto.admin;

import com.sep.realvista.domain.billing.boost.BoostPackage;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BoostPackageAdminResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private Integer featuredQuota;
    private Integer hotBadgeQuota;
    private Integer durationDays;
    private BigDecimal price;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BoostPackageAdminResponse from(BoostPackage bp) {
        return BoostPackageAdminResponse.builder()
                .id(bp.getBoostPackageId())
                .code(bp.getCode())
                .name(bp.getName())
                .description(bp.getDescription())
                .featuredQuota(bp.getFeaturedQuota())
                .hotBadgeQuota(bp.getHotBadgeQuota())
                .durationDays(bp.getDurationDays())
                .price(bp.getPrice())
                .isActive(bp.getIsActive())
                .createdAt(bp.getCreatedAt())
                .updatedAt(bp.getUpdatedAt())
                .build();
    }
}
