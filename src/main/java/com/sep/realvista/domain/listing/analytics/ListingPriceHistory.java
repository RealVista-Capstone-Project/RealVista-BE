package com.sep.realvista.domain.listing.analytics;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.listing.Listing;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "listing_price_histories", indexes = {
        @Index(name = "idx_listing_price_history_listing", columnList = "listing_id"),
        @Index(name = "idx_listing_price_history_changed_by", columnList = "changed_by")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ListingPriceHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "listing_price_history_id")
    private UUID listingPriceHistoryId;

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private Listing listing;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @Column(name = "min_price", precision = 14, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 14, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "changed_by", nullable = false)
    private UUID changedBy;
}
