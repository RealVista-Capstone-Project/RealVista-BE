package com.sep.realvista.domain.property.attribute;

import com.sep.realvista.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "property_attribute_ranges", indexes = {
        @Index(name = "idx_property_attr_range_attr", columnList = "property_attribute_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PropertyAttributeRange extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "property_attribute_range_id")
    private UUID propertyAttributeRangeId;

    @Column(name = "property_attribute_id", nullable = false)
    private UUID propertyAttributeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_attribute_id", insertable = false, updatable = false)
    private PropertyAttribute propertyAttribute;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(name = "min_value", precision = 12, scale = 2)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 12, scale = 2)
    private BigDecimal maxValue;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    public void updateLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Label cannot be null or empty");
        }
        this.label = label;
    }

    public void updateRange(BigDecimal minValue, BigDecimal maxValue) {
        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
            throw new IllegalArgumentException("Min value cannot be greater than max value");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }
}
