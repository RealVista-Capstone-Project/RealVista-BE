package com.sep.realvista.domain.property.amenity;

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
@Table(name = "amenities", indexes = {
        @Index(name = "idx_amenity_type", columnList = "amenity_type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Amenity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "amenity_id")
    private UUID amenityId;

    @Column(name = "amenity_name", nullable = false, length = 100)
    private String amenityName;

    @Enumerated(EnumType.STRING)
    @Column(name = "amenity_type", nullable = false, length = 20)
    private AmenityType amenityType;

    @Column(columnDefinition = "TEXT")
    private String description;

    public void update(String name, String description) {
        if (name != null && !name.isBlank()) {
            this.amenityName = name;
        }
        this.description = description;
    }

    public boolean isOnsite() {
        return amenityType == AmenityType.ONSITE;
    }

    public boolean isOffsite() {
        return amenityType == AmenityType.OFFSITE;
    }
}
