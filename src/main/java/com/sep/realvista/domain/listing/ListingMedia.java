package com.sep.realvista.domain.listing;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.property.PropertyMedia;
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
@Table(name = "listing_medias", 
        indexes = {
                @Index(name = "idx_listing_media_listing", columnList = "listing_id"),
                @Index(name = "idx_listing_media_order", columnList = "display_order"),
                @Index(name = "idx_listing_media_deleted", columnList = "deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"listing_id", "property_media_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ListingMedia extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "listing_media_id")
    private UUID listingMediaId;

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private Listing listing;

    @Column(name = "property_media_id", nullable = false)
    private UUID propertyMediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_media_id", insertable = false, updatable = false)
    private PropertyMedia propertyMedia;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = true;

    public void updateDisplayOrder(Integer order) {
        this.displayOrder = order;
    }

    public void markAsPrimary() {
        this.isPrimary = true;
    }

    public void removePrimary() {
        this.isPrimary = false;
    }

    public static ListingMedia create(UUID listingId, UUID propertyMediaId, Integer order, boolean isPrimary) {
        return ListingMedia.builder()
                .listingId(listingId)
                .propertyMediaId(propertyMediaId)
                .displayOrder(order)
                .isPrimary(isPrimary)
                .build();
    }
}
