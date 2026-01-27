package com.sep.realvista.application.listing.dto;

import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.property.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Complete listing detail response for UI.
 * Contains all information needed to populate the listing detail page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingDetailResponse {

    // Basic Listing Information
    private UUID listingId;
    private UUID propertyId;
    private UUID userId;
    private ListingType listingType;
    private ListingStatus status;
    private BigDecimal price;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean isNegotiable;
    private LocalDate availableFrom;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Property Information
    private PropertyInfoDTO property;

    // Location Information
    private LocationInfoDTO location;

    // Property Type & Category
    private PropertyTypeInfoDTO propertyType;

    // Media (Photos, Videos, 3D Tours)
    private List<MediaDTO> media;

    // Owner/Agent Information
    private AgentInfoDTO agent;

    // Statistics
    private Integer totalPhotos;
    private Integer totalVideos;
    private Integer total3DTours;

    /**
     * Property information nested DTO.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyInfoDTO {
        private UUID propertyId;
        private String streetAddress;
        private BigDecimal landSizeM2;
        private BigDecimal usableSizeM2;
        private BigDecimal widthM;
        private BigDecimal lengthM;
        private String description;
        private String slug;
    }

    /**
     * Location information nested DTO.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInfoDTO {
        private UUID locationId;
        private String cityName;
        private String districtName;
        private String wardName;
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

    /**
     * Property type and category nested DTO.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyTypeInfoDTO {
        private UUID propertyTypeId;
        private String propertyTypeName;
        private String propertyTypeCode;
        private UUID propertyCategoryId;
        private String propertyCategoryName;
        private String propertyCategoryCode;
    }

    /**
     * Media information nested DTO.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaDTO {
        private UUID mediaId;
        private MediaType mediaType;
        private String mediaUrl;
        private String thumbnailUrl;
        private Boolean isPrimary;
        private Integer displayOrder;

        // Helper methods for UI
        public boolean isImage() {
            return mediaType == MediaType.IMAGE;
        }

        public boolean isVideo() {
            return mediaType == MediaType.VIDEO;
        }

        public boolean is3D() {
            return mediaType == MediaType.THREE_D;
        }
    }

    /**
     * Agent/Owner information nested DTO.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentInfoDTO {
        private UUID userId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String businessName;
        private String email;
        private String phone;
        private String avatarUrl;
        private String company;
        private Boolean isVerified;
    }
}
