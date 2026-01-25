package com.sep.realvista.domain.property.location;

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
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "locations", indexes = {
        @Index(name = "idx_location_parent", columnList = "parent_id"),
        @Index(name = "idx_location_type", columnList = "type"),
        @Index(name = "idx_location_code", columnList = "code")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = {"parent"})
public class Location extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "parent_id")
    private UUID parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Location parent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LocationType type;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String code;

    @Column(name = "north_lat", nullable = false, precision = 9, scale = 6)
    private BigDecimal northLat;

    @Column(name = "south_lat", nullable = false, precision = 9, scale = 6)
    private BigDecimal southLat;

    @Column(name = "east_lng", nullable = false, precision = 9, scale = 6)
    private BigDecimal eastLng;

    @Column(name = "west_lng", nullable = false, precision = 9, scale = 6)
    private BigDecimal westLng;

    public boolean isCity() {
        return this.type == LocationType.CITY;
    }

    public boolean isDistrict() {
        return this.type == LocationType.DISTRICT;
    }

    public boolean isWard() {
        return this.type == LocationType.WARD;
    }

    public boolean containsCoordinate(BigDecimal latitude, BigDecimal longitude) {
        return latitude.compareTo(southLat) >= 0 
                && latitude.compareTo(northLat) <= 0
                && longitude.compareTo(westLng) >= 0 
                && longitude.compareTo(eastLng) <= 0;
    }

    public void update(String name, String code) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (code != null) {
            this.code = code;
        }
    }

    public void updateBounds(BigDecimal northLat, BigDecimal southLat, 
                             BigDecimal eastLng, BigDecimal westLng) {
        this.northLat = northLat;
        this.southLat = southLat;
        this.eastLng = eastLng;
        this.westLng = westLng;
    }
}
