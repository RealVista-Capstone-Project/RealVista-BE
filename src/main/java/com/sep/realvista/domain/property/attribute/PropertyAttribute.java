package com.sep.realvista.domain.property.attribute;

import com.sep.realvista.domain.common.entity.BaseEntity;
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

import java.util.UUID;

@Entity
@Table(name = "property_attributes", indexes = {
        @Index(name = "idx_property_attribute_data_type", columnList = "data_type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PropertyAttribute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "property_attribute_id")
    private UUID propertyAttributeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private AttributeDataType dataType;

    @Column(name = "is_searchable")
    @Builder.Default
    private Boolean isSearchable = true;

    @Column(length = 100)
    private String icon;

    @Column(length = 20)
    private String unit;

    public void update(String name, String icon, String unit) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.icon = icon;
        this.unit = unit;
    }

    public void enableSearch() {
        this.isSearchable = true;
    }

    public void disableSearch() {
        this.isSearchable = false;
    }
}
