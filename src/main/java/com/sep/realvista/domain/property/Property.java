package com.sep.realvista.domain.property;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.common.value.PriceRangeVO;
import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.domain.user.User;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "properties", indexes = {
        @Index(name = "idx_property_owner", columnList = "owner_id"),
        @Index(name = "idx_property_location", columnList = "location_id"),
        @Index(name = "idx_property_type", columnList = "property_type_id"),
        @Index(name = "idx_property_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Property extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "property_id")
    private UUID propertyId;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private User owner;

    @Column(name = "location_id", nullable = false)
    private UUID locationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", insertable = false, updatable = false)
    private Location location;

    @Column(name = "property_type_id", nullable = false)
    private UUID propertyTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_type_id", insertable = false, updatable = false)
    private PropertyType propertyType;

    @Column(name = "street_address", nullable = false)
    private String streetAddress;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "land_size_m2", precision = 10, scale = 2)
    private BigDecimal landSizeM2;

    @Column(name = "usable_size_m2", precision = 10, scale = 2)
    private BigDecimal usableSizeM2;

    @Column(name = "width_m", precision = 6, scale = 2)
    private BigDecimal widthM;

    @Column(name = "length_m", precision = 6, scale = 2)
    private BigDecimal lengthM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private PropertyStatus status = PropertyStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String descriptions;

    @Column(unique = true)
    private String slug;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_attributes", columnDefinition = "jsonb")
    private Map<String, Object> extraAttributes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "price_range", columnDefinition = "jsonb")
    private PriceRangeVO priceRange;

    public void publish() {
        if (this.status != PropertyStatus.DRAFT && this.status != PropertyStatus.VERIFIED) {
            throw new IllegalStateException("Only draft or verified properties can be published");
        }
        this.status = PropertyStatus.AVAILABLE;
    }

    public void verifyByAgent() {
        if (this.status != PropertyStatus.PENDING) {
            throw new IllegalStateException("Only pending properties can be verified by agent");
        }
        this.status = PropertyStatus.VERIFIED;
    }

    public void reserve() {
        if (this.status != PropertyStatus.AVAILABLE) {
            throw new IllegalStateException("Only available properties can be reserved");
        }
        this.status = PropertyStatus.RESERVED;
    }

    public void markAsSold() {
        if (this.status == PropertyStatus.SOLD) {
            throw new IllegalStateException("Property is already sold");
        }
        this.status = PropertyStatus.SOLD;
    }

    public void markAsRented() {
        if (this.status == PropertyStatus.RENTED) {
            throw new IllegalStateException("Property is already rented");
        }
        this.status = PropertyStatus.RENTED;
    }

    public void markAsAvailable() {
        this.status = PropertyStatus.AVAILABLE;
    }

    public void cancelReservation() {
        if (this.status != PropertyStatus.RESERVED) {
            throw new IllegalStateException("Only reserved properties can have reservation cancelled");
        }
        this.status = PropertyStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return this.status == PropertyStatus.AVAILABLE;
    }

    public void updateStatus(PropertyStatus newStatus) {
        if (newStatus != null) {
            this.status = newStatus;
        }
    }

    public void updateLocationAndType(UUID locationId, UUID propertyTypeId) {
        if (locationId != null) {
            this.locationId = locationId;
        }
        if (propertyTypeId != null) {
            this.propertyTypeId = propertyTypeId;
        }
    }

    public void updateDetails(String streetAddress, String descriptions, String slug) {
        if (streetAddress != null && !streetAddress.isBlank()) {
            this.streetAddress = streetAddress;
        }
        if (descriptions != null) {
            this.descriptions = descriptions;
        }
        if (slug != null) {
            this.slug = slug;
        }
    }

    public void updateDimensions(BigDecimal landSizeM2, BigDecimal usableSizeM2,
                                 BigDecimal widthM, BigDecimal lengthM) {
        this.landSizeM2 = landSizeM2;
        this.usableSizeM2 = usableSizeM2;
        this.widthM = widthM;
        this.lengthM = lengthM;
    }

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY,
            cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.sep.realvista.domain.property.attribute.PropertyAttributeValue> attributeValues =
            new java.util.ArrayList<>();

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY,
               cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.SQLRestriction("is_property_standard = true")
    @Builder.Default
    private List<PropertyMedia> mediaList = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY,
            cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.sep.realvista.domain.property.amenity.PropertyAmenity> amenities =
            new java.util.ArrayList<>();

    public void updateMedia(List<PropertyMedia> newMedia) {
        // Remove those not in new list (by URL)
        this.mediaList.removeIf(existing ->
                newMedia.stream().noneMatch(n -> n.getMediaUrl().equals(existing.getMediaUrl())));

        // Add new ones or update existing ones
        if (newMedia != null) {
            for (var m : newMedia) {
                this.mediaList.stream()
                        .filter(existing -> existing.getMediaUrl().equals(m.getMediaUrl()))
                        .findFirst()
                        .ifPresentOrElse(
                                existing -> {
                                    existing.updateMetadata(m.getMediaType(), m.getThumbnailUrl(), m.getIsPrimary());
                                },
                                () -> this.mediaList.add(m)
                        );
            }
        }
    }

    public void updateAmenities(List<com.sep.realvista.domain.property.amenity.PropertyAmenity> newAmenities) {
        // Remove those not in new list
        this.amenities.removeIf(existing ->
                newAmenities.stream().noneMatch(n -> n.getAmenityId().equals(existing.getAmenityId())));

        // Add those not in existing list
        if (newAmenities != null) {
            for (var newAmenity : newAmenities) {
                if (this.amenities.stream().noneMatch(existing ->
                        existing.getAmenityId().equals(newAmenity.getAmenityId()))) {
                    this.amenities.add(newAmenity);
                }
            }
        }
    }

    public void updateAttributes(
            List<com.sep.realvista.domain.property.attribute.PropertyAttributeValue> newAttributes) {
        // Remove those not in new list
        this.attributeValues.removeIf(existing ->
            newAttributes.stream().noneMatch(n ->
                n.getPropertyAttributeId().equals(existing.getPropertyAttributeId())));

        // Add or Update
        if (newAttributes != null) {
            for (var newAttr : newAttributes) {
                this.attributeValues.stream()
                    .filter(existing -> existing.getPropertyAttributeId().equals(newAttr.getPropertyAttributeId()))
                    .findFirst()
                    .ifPresentOrElse(
                        existing -> {
                            // Update only provided values to prevent clearing others
                            if (newAttr.getValueNumber() != null) {
                                existing.updateNumberValue(newAttr.getValueNumber());
                            } else if (newAttr.getValueText() != null) {
                                existing.updateTextValue(newAttr.getValueText());
                            } else if (newAttr.getValueBoolean() != null) {
                                existing.updateBooleanValue(newAttr.getValueBoolean());
                            }
                        },
                        () -> {
                            this.attributeValues.add(newAttr);
                        }
                    );
            }
        }
    }

    public void updateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updateExtraAttributes(Map<String, Object> extraAttributes) {
        if (extraAttributes != null) {
            this.extraAttributes = extraAttributes;
        }
    }

    public void updatePriceRange(PriceRangeVO priceRange) {
        if (priceRange != null) {
            this.priceRange = priceRange;
        }
    }
}
