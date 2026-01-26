package com.sep.realvista.domain.property.amenity;

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

import java.util.UUID;

@Entity
@Table(name = "property_amenities", 
        indexes = {
                @Index(name = "idx_property_amenity_property", columnList = "property_id"),
                @Index(name = "idx_property_amenity_amenity", columnList = "amenity_id"),
                @Index(name = "idx_property_amenity_deleted", columnList = "deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"property_id", "amenity_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PropertyAmenity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "property_amenity_id")
    private UUID propertyAmenityId;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", insertable = false, updatable = false)
    private Property property;

    @Column(name = "amenity_id", nullable = false)
    private UUID amenityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amenity_id", insertable = false, updatable = false)
    private Amenity amenity;

    public static PropertyAmenity create(UUID propertyId, UUID amenityId) {
        return PropertyAmenity.builder()
                .propertyId(propertyId)
                .amenityId(amenityId)
                .build();
    }
}
