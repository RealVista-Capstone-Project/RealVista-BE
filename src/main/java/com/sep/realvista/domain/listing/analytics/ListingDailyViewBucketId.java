package com.sep.realvista.domain.listing.analytics;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite key for {@link ListingDailyViewBucket}.
 */
public class ListingDailyViewBucketId implements Serializable {

    private UUID listingId;
    private LocalDate bucketDate;

    public ListingDailyViewBucketId() {
    }

    public ListingDailyViewBucketId(UUID listingId, LocalDate bucketDate) {
        this.listingId = listingId;
        this.bucketDate = bucketDate;
    }

    public UUID getListingId() {
        return listingId;
    }

    public void setListingId(UUID listingId) {
        this.listingId = listingId;
    }

    public LocalDate getBucketDate() {
        return bucketDate;
    }

    public void setBucketDate(LocalDate bucketDate) {
        this.bucketDate = bucketDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ListingDailyViewBucketId that = (ListingDailyViewBucketId) o;
        return Objects.equals(listingId, that.listingId) && Objects.equals(bucketDate, that.bucketDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listingId, bucketDate);
    }
}
