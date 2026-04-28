package com.sep.realvista.domain.billing.subscription;

import com.sep.realvista.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "feature_packages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FeaturePackage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "feature_package_id")
    private UUID featurePackageId;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Convert(converter = FeatureTypeConverter.class)
    @Column(name = "feature_type", nullable = false, length = 30)
    private FeatureType featureType;

    @Column(nullable = false)
    private Integer quota;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isUnlimited() {
        return quota != null && quota == -1;
    }

    public boolean hasNoExpiration() {
        return durationDays != null && durationDays == -1;
    }

    public boolean isFree() {
        return price != null && price.compareTo(BigDecimal.ZERO) == 0;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void update(String name, String description, Integer quota,
                       Integer durationDays, java.math.BigDecimal price) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (quota != null) {
            this.quota = quota;
        }
        if (durationDays != null) {
            this.durationDays = durationDays;
        }
        if (price != null) {
            this.price = price;
        }
    }
}
