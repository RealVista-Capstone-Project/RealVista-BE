package com.sep.realvista.domain.property.attribute;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.property.PropertyType;
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

import java.util.UUID;

@Entity
@Table(name = "property_type_attributes",
        indexes = {
                @Index(name = "idx_property_type_attr_type", columnList = "property_type_id"),
                @Index(name = "idx_property_type_attr_attr", columnList = "property_attribute_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"property_type_id", "property_attribute_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PropertyTypeAttribute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "property_type_attribute_id")
    private UUID propertyTypeAttributeId;

    @Column(name = "property_type_id", nullable = false)
    private UUID propertyTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_type_id", insertable = false, updatable = false)
    private PropertyType propertyType;

    @Column(name = "property_attribute_id", nullable = false)
    private UUID propertyAttributeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_attribute_id", insertable = false, updatable = false)
    private PropertyAttribute propertyAttribute;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = false;

    public void markAsRequired() {
        this.isRequired = true;
    }

    public void markAsOptional() {
        this.isRequired = false;
    }
}
