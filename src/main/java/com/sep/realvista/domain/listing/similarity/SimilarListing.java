package com.sep.realvista.domain.listing.similarity;

import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.ListingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Value object representing a similar listing with its similarity score.
 * Used by the similar listings feature to return lightweight listing information
 * along with the calculated similarity score.
 * <p>
 * NOTE: The @SqlResultSetMapping for this class is defined in Listing.java entity
 * since Hibernate only scans @Entity classes for SQL result set mappings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarListing {

    private UUID listingId;
    private UUID propertyId;
    private UUID propertyTypeId;
    private UUID locationId;
    private String name;
    private String slug;
    private ListingType listingType;
    private ListingStatus status;
    private BigDecimal price;
    private BigDecimal area;
    private String locationName;
    private String propertyTypeName;
    private String thumbnailUrl;
    private LocalDateTime publishedAt;

    /**
     * Similarity score (0.0 to 1.0).
     * Calculated based on:
     * - Property Type match (40%)
     * - Price range similarity (25%)
     * - Area similarity (20%)
     * - Common attributes similarity (15%)
     */
    private Double similarityScore;

    /**
     * Constructor for JPA ResultSetMapping.
     * This constructor is used by Hibernate to map SQL results to this class.
     */
    @SuppressWarnings("paramNumLimit")
    public SimilarListing(UUID listingId, UUID propertyId, UUID propertyTypeId, UUID locationId,
                         String name, String slug, String listingType, String status,
                         BigDecimal price, BigDecimal area, String locationName,
                         String propertyTypeName, String thumbnailUrl, LocalDateTime publishedAt,
                         Double similarityScore) {
        this.listingId = listingId;
        this.propertyId = propertyId;
        this.propertyTypeId = propertyTypeId;
        this.locationId = locationId;
        this.name = name;
        this.slug = slug;
        this.listingType = ListingType.valueOf(listingType);
        this.status = ListingStatus.valueOf(status);
        this.price = price;
        this.area = area;
        this.locationName = locationName;
        this.propertyTypeName = propertyTypeName;
        this.thumbnailUrl = thumbnailUrl;
        this.publishedAt = publishedAt;
        this.similarityScore = similarityScore;
    }

    /**
     * Get similarity score as percentage (0-100).
     */
    public int getSimilarityPercentage() {
        if (similarityScore == null) {
            return 0;
        }
        return (int) Math.round(similarityScore * 100);
    }
}
