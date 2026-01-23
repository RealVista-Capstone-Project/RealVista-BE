package com.sep.realvista.domain.property;

import com.sep.realvista.domain.common.entity.BaseEntity;
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

import java.util.UUID;

@Entity
@Table(name = "property_types", indexes = {
        @Index(name = "idx_property_type_category", columnList = "property_category_id"),
        @Index(name = "idx_property_type_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PropertyType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "property_type_id")
    private UUID propertyTypeId;

    @Column(name = "property_category_id", nullable = false)
    private UUID propertyCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_category_id", insertable = false, updatable = false)
    private PropertyCategory propertyCategory;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, length = 50)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PropertyTypeStatus status = PropertyTypeStatus.ACTIVE;

    public void activate() {
        this.status = PropertyTypeStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = PropertyTypeStatus.INACTIVE;
    }

    public boolean isActive() {
        return this.status == PropertyTypeStatus.ACTIVE;
    }

    public void update(String name, String code, String description) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (code != null) {
            this.code = code;
        }
        if (description != null) {
            this.description = description;
        }
    }
}
