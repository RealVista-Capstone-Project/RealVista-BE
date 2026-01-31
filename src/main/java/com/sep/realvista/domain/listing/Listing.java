package com.sep.realvista.domain.listing;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.property.Property;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "listings", indexes = {
        @Index(name = "idx_listing_property", columnList = "property_id"),
        @Index(name = "idx_listing_user", columnList = "user_id"),
        @Index(name = "idx_listing_type", columnList = "listing_type"),
        @Index(name = "idx_listing_status", columnList = "status"),
        @Index(name = "idx_listing_published", columnList = "published_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Listing extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "listing_id")
    private UUID listingId;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", insertable = false, updatable = false)
    private Property property;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "listing_type", nullable = false, length = 20)
    private ListingType listingType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private ListingStatus status = ListingStatus.DRAFT;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @Column(name = "min_price", precision = 14, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 14, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "is_negotiable", nullable = false)
    @Builder.Default
    private Boolean isNegotiable = false;

    /**
     * Move-in availability date for RENT listings.
     * NULL or past/today date = Available immediately.
     * Future date = Available from that specific date.
     * Only meaningful when listingType = RENT.
     */
    @Column(name = "available_from")
    private LocalDate availableFrom;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(nullable = false, length = 500)
    private String name;

    public void submitForReview() {
        if (this.status != ListingStatus.DRAFT) {
            throw new IllegalStateException("Only draft listings can be submitted for review");
        }
        this.status = ListingStatus.PENDING;
    }

    public void publish() {
        if (this.status != ListingStatus.PENDING && this.status != ListingStatus.DRAFT) {
            throw new IllegalStateException("Only pending or draft listings can be published");
        }
        this.status = ListingStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markAsSold() {
        if (this.listingType != ListingType.SALE) {
            throw new IllegalStateException("Only sale listings can be marked as sold");
        }
        if (this.status != ListingStatus.PUBLISHED) {
            throw new IllegalStateException("Only published listings can be marked as sold");
        }
        this.status = ListingStatus.SOLD;
    }

    public void markAsRented() {
        if (this.listingType != ListingType.RENT) {
            throw new IllegalStateException("Only rent listings can be marked as rented");
        }
        if (this.status != ListingStatus.PUBLISHED) {
            throw new IllegalStateException("Only published listings can be marked as rented");
        }
        this.status = ListingStatus.RENTED;
    }

    public void expire() {
        if (this.status != ListingStatus.PUBLISHED) {
            throw new IllegalStateException("Only published listings can be expired");
        }
        this.status = ListingStatus.EXPIRED;
    }

    public void unpublish() {
        if (this.status != ListingStatus.PUBLISHED) {
            throw new IllegalStateException("Only published listings can be unpublished");
        }
        this.status = ListingStatus.DRAFT;
        this.publishedAt = null;
    }

    public boolean isActive() {
        return this.status == ListingStatus.PUBLISHED;
    }

    public boolean isForSale() {
        return this.listingType == ListingType.SALE;
    }

    public boolean isForRent() {
        return this.listingType == ListingType.RENT;
    }

    public void updatePricing(BigDecimal price, BigDecimal minPrice,
                               BigDecimal maxPrice, Boolean isNegotiable) {
        if (price != null) {
            this.price = price;
        }
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        if (isNegotiable != null) {
            this.isNegotiable = isNegotiable;
        }
    }

    /**
     * Attach property entity for DTO mapping purposes.
     * This does not persist changes - property relationship is managed by JPA.
     *
     * @param property the property entity to attach
     */
    public void attachProperty(Property property) {
        this.property = property;
    }

    /**
     * Attach user entity for DTO mapping purposes.
     * This does not persist changes - user relationship is managed by JPA.
     *
     * @param user the user entity to attach
     */
    public void attachUser(User user) {
        this.user = user;
    }

}
