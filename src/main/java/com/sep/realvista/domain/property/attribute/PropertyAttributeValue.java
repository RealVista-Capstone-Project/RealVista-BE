package com.sep.realvista.domain.property.attribute;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.property.Property;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "property_attribute_values",
        indexes = {
                @Index(name = "idx_property_attr_value_property", columnList = "property_id"),
                @Index(name = "idx_property_attr_value_attr", columnList = "property_attribute_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"property_id", "property_attribute_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PropertyAttributeValue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "property_attribute_value_id")
    private UUID propertyAttributeValueId;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", insertable = false, updatable = false)
    private Property property;

    @Column(name = "property_attribute_id", nullable = false)
    private UUID propertyAttributeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_attribute_id", insertable = false, updatable = false)
    private PropertyAttribute propertyAttribute;

    @Column(name = "value_number", precision = 12, scale = 2)
    private BigDecimal valueNumber;

    @Column(name = "value_text")
    private String valueText;

    @Column(name = "value_boolean")
    private Boolean valueBoolean;

    public void updateNumberValue(BigDecimal value) {
        this.valueNumber = value;
        this.valueText = null;
        this.valueBoolean = null;
    }

    public void updateTextValue(String value) {
        this.valueText = value;
        this.valueNumber = null;
        this.valueBoolean = null;
    }

    public void updateBooleanValue(Boolean value) {
        this.valueBoolean = value;
        this.valueNumber = null;
        this.valueText = null;
    }
}
