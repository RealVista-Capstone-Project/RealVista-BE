package com.sep.realvista.application.listing.bookmark.mapper;

import com.sep.realvista.application.listing.bookmark.dto.BookmarkListingCardDTO;
import com.sep.realvista.application.listing.bookmark.dto.BookmarkResponse;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.domain.listing.bookmark.Bookmark;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingMedia;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Bookmark operations.
 */
@Mapper(componentModel = "spring")
public interface BookmarkMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "listingId", source = "listing.listingId")
    @Mapping(target = "userEmail", source = "user.email.value")
    @Mapping(target = "userFullName", source = "user", qualifiedByName = "getFullName")
    @Mapping(target = "listingType", source = "listing.listingType")
    @Mapping(target = "propertyAddress", source = "listing", qualifiedByName = "getPropertyAddress")
    @Mapping(target = "bookmarked", source = "bookmarked")
    @Mapping(target = "actionTimestamp", source = "actionTimestamp")
    BookmarkResponse toResponse(User user, Listing listing, boolean bookmarked, LocalDateTime actionTimestamp);

    /**
     * Maps a Bookmark to a BookmarkListingCardDTO with media and attributes.
     *
     * @param bookmark the bookmark entity
     * @param primaryMedia the primary media for the listing (optional)
     * @param attributes list of property attributes
     * @return BookmarkListingCardDTO
     */
    default BookmarkListingCardDTO toListingCard(
            Bookmark bookmark,
            ListingMedia primaryMedia,
            List<PropertyAttributeValue> attributes
    ) {
        if (bookmark == null || bookmark.getListing() == null) {
            return null;
        }

        Listing listing = bookmark.getListing();
        var property = listing.getProperty();
        var location = property != null ? property.getLocation() : null;
        var propertyType = property != null ? property.getPropertyType() : null;

        BookmarkListingCardDTO.BookmarkListingCardDTOBuilder builder = BookmarkListingCardDTO.builder()
                .listingId(listing.getListingId())
                .title(listing.getName())
                .price(listing.getPrice())
                .listingType(listing.getListingType())
                .isNegotiable(listing.getIsNegotiable())
                .status(listing.getStatus())
                .bookmarkedAt(bookmark.getCreatedAt());

        // Set primary image URL
        if (primaryMedia != null && primaryMedia.getPropertyMedia() != null) {
            builder.primaryImageUrl(primaryMedia.getPropertyMedia().getMediaUrl());
        }

        // Set address information
        if (property != null) {
            builder.streetAddress(property.getStreetAddress());
            builder.usableSizeM2(property.getUsableSizeM2());

            // Calculate area in sqft
            if (property.getUsableSizeM2() != null) {
                BigDecimal areaSqft = property.getUsableSizeM2()
                        .multiply(new BigDecimal("10.764"))
                        .setScale(2, RoundingMode.HALF_UP);
                builder.areaSqft(areaSqft);
            }
        }

        // Set location information
        if (location != null) {
            builder.cityName(getLocationName(location, "CITY"))
                    .districtName(getLocationName(location, "DISTRICT"))
                    .wardName(getLocationName(location, "WARD"));
        }

        // Set property type information
        if (propertyType != null) {
            builder.propertyTypeName(propertyType.getName());
            if (propertyType.getPropertyCategory() != null) {
                builder.propertyCategoryName(propertyType.getPropertyCategory().getName());
            }
        }

        // Map attributes: sort by priority, then re-number from 1
        if (attributes != null && !attributes.isEmpty()) {
            List<PropertyAttributeValue> sorted = attributes.stream()
                    .sorted(Comparator.comparingInt(
                            pav -> pav.getPriority() != null ? pav.getPriority() : Integer.MAX_VALUE))
                    .collect(Collectors.toList());

            List<PropertyAttributeDTO> attributeDTOs = new ArrayList<>();
            for (int i = 0; i < sorted.size(); i++) {
                PropertyAttributeDTO dto = toAttributeDTO(sorted.get(i));
                if (dto != null) {
                    dto.setPriority(i + 1);
                    attributeDTOs.add(dto);
                }
            }
            builder.attributes(attributeDTOs);
        }

        return builder.build();
    }

    /**
     * Gets location name by traversing up the location hierarchy.
     */
    default String getLocationName(com.sep.realvista.domain.property.location.Location location, String type) {
        com.sep.realvista.domain.property.location.Location current = location;
        while (current != null) {
            if (current.getType().name().equals(type)) {
                return current.getName();
            }
            current = current.getParent();
        }
        return null;
    }

    /**
     * Maps PropertyAttributeValue to PropertyAttributeDTO.
     */
    default PropertyAttributeDTO toAttributeDTO(PropertyAttributeValue attributeValue) {
        if (attributeValue == null || attributeValue.getPropertyAttribute() == null) {
            return null;
        }
        var attribute = attributeValue.getPropertyAttribute();
        return PropertyAttributeDTO.builder()
                .attributeId(attribute.getPropertyAttributeId())
                .attributeCode(attribute.getCode())
                .attributeName(attribute.getName())
                .dataType(attribute.getDataType() != null ? attribute.getDataType().name() : null)
                .icon(attribute.getIcon())
                .unit(attribute.getUnit())
                .priority(attributeValue.getPriority())
                .valueNumber(attributeValue.getValueNumber())
                .valueText(attributeValue.getValueText())
                .valueBoolean(attributeValue.getValueBoolean())
                .build();
    }

    @org.mapstruct.Named("getFullName")
    default String getFullName(User user) {
        return user.getFullName();
    }

    @org.mapstruct.Named("getPropertyAddress")
    default String getPropertyAddress(Listing listing) {
        return listing.getProperty() != null
            ? listing.getProperty().getStreetAddress()
            : "Address not available";
    }
}