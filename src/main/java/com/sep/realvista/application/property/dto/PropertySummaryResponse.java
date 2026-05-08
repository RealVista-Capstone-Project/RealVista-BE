package com.sep.realvista.application.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.application.listing.dto.AmenityDTO;
import com.sep.realvista.application.listing.dto.LocationInfoDTO;
import com.sep.realvista.application.listing.dto.MediaDTO;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.application.listing.dto.PropertyTypeInfoDTO;
import com.sep.realvista.domain.common.value.PriceRangeVO;
import com.sep.realvista.domain.property.PropertyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertySummaryResponse {
    @JsonProperty("property_id")
    private UUID propertyId;

    @JsonProperty("owner_id")
    private UUID ownerId;

    @JsonProperty("property_type_id")
    private UUID propertyTypeId;

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

    @JsonProperty("area_sqft")
    private BigDecimal areaSqft;

    private String description;

    private PropertyStatus status;

    private List<PropertyAttributeDTO> attributes;

    @JsonProperty("property_type_info")
    private PropertyTypeInfoDTO propertyTypeInfo;

    @JsonProperty("location_info")
    private LocationInfoDTO locationInfo;

    private List<MediaDTO> media;

    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    private List<AmenityDTO> amenities;

    @JsonProperty("owner_name")
    private String ownerName;

    @JsonProperty("owner_email")
    private String ownerEmail;

    @JsonProperty("owner_avatar_url")
    private String ownerAvatarUrl;

    @JsonProperty("owner_phone")
    private String ownerPhone;

    @JsonProperty("owner_phone_display")
    private String ownerPhoneDisplay;

    @JsonProperty("is_owner_phone_hidden")
    private Boolean isOwnerPhoneHidden;

    @JsonProperty("has_3d")
    private boolean has3d;

    @JsonProperty("price_range")
    private PriceRangeVO priceRange;

    @JsonProperty("sold_by_user_id")
    private UUID soldByUserId;

    @JsonProperty("sold_by_name")
    private String soldByName;

    @JsonProperty("sold_by_phone")
    private String soldByPhone;

    @JsonProperty("sold_by_role")
    private String soldByRole;

    @JsonProperty("sold_at")
    private LocalDateTime soldAt;

    @JsonProperty("rented_by_user_id")
    private UUID rentedByUserId;

    @JsonProperty("rented_by_name")
    private String rentedByName;

    @JsonProperty("rented_by_phone")
    private String rentedByPhone;

    @JsonProperty("rented_by_role")
    private String rentedByRole;

    @JsonProperty("rented_at")
    private LocalDateTime rentedAt;

    @JsonProperty("allow_rent_listing_when_rented")
    private Boolean allowRentListingWhenRented;

    @JsonProperty("flagged_for_admin_review")
    private Boolean flaggedForAdminReview;

    @JsonProperty("duplicate_override_reason")
    private String duplicateOverrideReason;
}
