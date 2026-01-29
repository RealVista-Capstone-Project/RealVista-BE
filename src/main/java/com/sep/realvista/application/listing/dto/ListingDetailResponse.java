package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("listing_id")
    private UUID listingId;
    @JsonProperty("property_id")
    private UUID propertyId;
    @JsonProperty("user_id")
    private UUID userId;
    @JsonProperty("listing_type")
    private ListingType listingType;
    private ListingStatus status;
    private BigDecimal price;
    @JsonProperty("min_price")
    private BigDecimal minPrice;
    @JsonProperty("max_price")
    private BigDecimal maxPrice;
    @JsonProperty("is_negotiable")
    private Boolean isNegotiable;
    @JsonProperty("available_from")
    private LocalDate availableFrom;
    @JsonProperty("published_at")
    private LocalDateTime publishedAt;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
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
    @JsonProperty("total_photos")
    private Integer totalPhotos;
    @JsonProperty("total_videos")
    private Integer totalVideos;
    @JsonProperty("total_3d_tours")
    private Integer total3DTours;

    /**
     * Property information nested DTO.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyInfoDTO {
        @JsonProperty("property_id")
        private UUID propertyId;
        @JsonProperty("street_address")
        private String streetAddress;
        @JsonProperty("land_size_m2")
        private BigDecimal landSizeM2;
        @JsonProperty("usable_size_m2")
        private BigDecimal usableSizeM2;
        @JsonProperty("width_m")
        private BigDecimal widthM;
        @JsonProperty("length_m")
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
        @JsonProperty("location_id")
        private UUID locationId;
        @JsonProperty("city_name")
        private String cityName;
        @JsonProperty("district_name")
        private String districtName;
        @JsonProperty("ward_name")
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
        @JsonProperty("property_type_id")
        private UUID propertyTypeId;
        @JsonProperty("property_type_name")
        private String propertyTypeName;
        @JsonProperty("property_type_code")
        private String propertyTypeCode;
        @JsonProperty("property_category_id")
        private UUID propertyCategoryId;
        @JsonProperty("property_category_name")
        private String propertyCategoryName;
        @JsonProperty("property_category_code")
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
        @JsonProperty("media_id")
        private UUID mediaId;
        @JsonProperty("media_type")
        private MediaType mediaType;
        @JsonProperty("media_url")
        private String mediaUrl;
        @JsonProperty("thumbnail_url")
        private String thumbnailUrl;
        @JsonProperty("is_primary")
        private Boolean isPrimary;
        @JsonProperty("display_order")
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
        @JsonProperty("user_id")
        private UUID userId;
        @JsonProperty("first_name")
        private String firstName;
        @JsonProperty("last_name")
        private String lastName;
        @JsonProperty("full_name")
        private String fullName;
        @JsonProperty("business_name")
        private String businessName;
        private String email;
        private String phone;
        @JsonProperty("avatar_url")
        private String avatarUrl;
        private String company;
        @JsonProperty("is_verified")
        private Boolean isVerified;
    }
}
