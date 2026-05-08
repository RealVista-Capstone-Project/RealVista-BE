package com.sep.realvista.domain.listing.analytics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregated listing views per calendar day (Vietnam), updated when a view is recorded.
 */
@Entity
@Table(name = "listing_daily_view_buckets")
@IdClass(ListingDailyViewBucketId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingDailyViewBucket {

    @Id
    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @Id
    @Column(name = "bucket_date", nullable = false)
    private LocalDate bucketDate;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    public void incrementBy(long delta) {
        this.viewCount += delta;
    }
}
