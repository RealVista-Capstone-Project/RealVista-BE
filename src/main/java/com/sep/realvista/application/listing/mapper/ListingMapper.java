package com.sep.realvista.application.listing.mapper;

import com.sep.realvista.application.listing.dto.AgentInfoDTO;
import com.sep.realvista.application.listing.dto.AmenityDTO;
import com.sep.realvista.application.listing.dto.ListingDetailResponse;
import com.sep.realvista.application.listing.dto.LocationInfoDTO;
import com.sep.realvista.application.listing.dto.MediaDTO;
import com.sep.realvista.application.listing.dto.MoneyRangeDTO;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.application.listing.dto.PropertyInfoDTO;
import com.sep.realvista.application.listing.dto.PropertyPriceRangeDTO;
import com.sep.realvista.application.listing.dto.PropertyTypeInfoDTO;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserStatus;
import com.sep.realvista.domain.user.preference.SettingPreference;
import com.sep.realvista.domain.property.amenity.Amenity;
import com.sep.realvista.domain.property.amenity.PropertyAmenity;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingMedia;
import com.sep.realvista.domain.property.PropertyType;
import com.sep.realvista.domain.property.attribute.PropertyAttribute;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.domain.property.location.LocationType;
import com.sep.realvista.domain.property.MediaType;
import com.sep.realvista.domain.property.PropertyMedia;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.common.value.PriceRangeVO;
import com.sep.realvista.domain.common.value.RangeVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Listing entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface ListingMapper {

    @Mapping(target = "property", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "propertyType", ignore = true)
    @Mapping(target = "agent", ignore = true)
    @Mapping(target = "media", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    @Mapping(target = "totalPhotos", ignore = true)
    @Mapping(target = "totalVideos", ignore = true)
    @Mapping(target = "total3DTours", ignore = true)
    @Mapping(target = "costBreakdown", ignore = true)
    @Mapping(target = "isFavorite", ignore = true)
    @Mapping(target = "isCreatedByOwner", ignore = true)
    @Mapping(target = "userType", ignore = true)
    @Mapping(target = "propertyOwner", ignore = true)
    @Mapping(target = "listingId", source = "listingId")
    @Mapping(target = "propertyId", source = "propertyId")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "listingType", source = "listingType")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "slug", source = "slug")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "minPrice", source = "minPrice")
    @Mapping(target = "maxPrice", source = "maxPrice")
    @Mapping(target = "isNegotiable", source = "isNegotiable")
    @Mapping(target = "availableFrom", source = "availableFrom")
    @Mapping(target = "publishedAt", source = "publishedAt")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    ListingDetailResponse toDetailResponse(Listing listing);

    default ListingDetailResponse toDetailResponseWithMedia(
            Listing listing,
            List<ListingMedia> mediaList,
            SettingPreference preference) {
        if (listing == null) {
            return null;
        }

        ListingDetailResponse response = toDetailResponse(listing);

        // Map property manually
        if (listing.getProperty() != null) {
            response.setProperty(mapPropertyInfo(listing.getProperty()));

            // Map location
            if (listing.getProperty().getLocation() != null) {
                response.setLocation(mapLocationInfo(listing.getProperty().getLocation()));
            }

            // Map property type
            if (listing.getProperty().getPropertyType() != null) {
                response.setPropertyType(mapPropertyTypeInfo(listing.getProperty().getPropertyType()));
            }
        }

        // Map agent/user with privacy logic
        if (listing.getUser() != null) {
            response.setAgent(mapAgentInfo(listing.getUser(), preference));
            boolean isAgent = listing.getUser().getUserRoles() != null
                    && listing.getUser().getUserRoles().stream()
                            .anyMatch(ur -> ur.getRole() != null
                                    && ur.getRole().getRoleCode() == com.sep.realvista.domain.user.role.RoleCode.AGENT);
            response.setUserType(isAgent ? "AGENT" : "OWNER");
        }

        // Map media
        response.setMedia(toMediaList(mediaList));
        response.setTotalPhotos(countPhotos(mediaList));
        response.setTotalVideos(countVideos(mediaList));
        response.setTotal3DTours(count3DTours(mediaList));

        return response;
    }

    default ListingDetailResponse toDetailResponseWithMediaAndAttributes(
            Listing listing,
            List<ListingMedia> mediaList,
            List<PropertyAttributeValue> attributeValues,
            SettingPreference preference) {
        ListingDetailResponse response = toDetailResponseWithMedia(listing, mediaList, preference);

        // Map attributes
        if (attributeValues != null && !attributeValues.isEmpty()) {
            response.setAttributes(toAttributeList(attributeValues));

            // Update property info with common attributes
            if (response.getProperty() != null) {
                Map<String, Object> attributeMap = attributeValues.stream()
                        .filter(pav -> pav.getPropertyAttribute() != null)
                        .filter(pav -> pav.getPropertyAttribute().getCode() != null)
                        .filter(pav -> getAttributeValue(pav) != null)
                        .collect(Collectors.toMap(
                                pav -> pav.getPropertyAttribute().getCode(),
                                pav -> (Object) getAttributeValue(pav),
                                (a, b) -> a));

                PropertyInfoDTO propertyInfo = response.getProperty();
                if (attributeMap.containsKey("bedrooms")) {
                    propertyInfo.setBedrooms(((Number) attributeMap.get("bedrooms")).intValue());
                }
                if (attributeMap.containsKey("bathrooms")) {
                    propertyInfo.setBathrooms(((Number) attributeMap.get("bathrooms")).intValue());
                }
            }
        }

        // Always calculate area in sqft from usable_size_m2 (not from attributes)
        if (response.getProperty() != null) {
            BigDecimal usableSizeM2 = response.getProperty().getUsableSizeM2();
            if (usableSizeM2 != null) {
                BigDecimal areaSqft = usableSizeM2.multiply(new BigDecimal("10.764"))
                        .setScale(2, RoundingMode.HALF_UP);
                response.getProperty().setAreaSqft(areaSqft);
            }
        }

        return response;
    }

    default ListingDetailResponse toDetailResponseWithMediaAttributesAndAmenities(
            Listing listing,
            List<ListingMedia> mediaList,
            List<PropertyAttributeValue> attributeValues,
            List<PropertyAmenity> propertyAmenities,
            SettingPreference preference) {
        ListingDetailResponse response = toDetailResponseWithMediaAndAttributes(
                listing, mediaList, attributeValues, preference);

        // Map amenities
        if (propertyAmenities != null && !propertyAmenities.isEmpty()) {
            response.setAmenities(toAmenityList(propertyAmenities));
        }

        return response;
    }

    private Object getAttributeValue(PropertyAttributeValue pav) {
        if (pav.getValueNumber() != null) {
            return pav.getValueNumber();
        }
        if (pav.getValueBoolean() != null) {
            return pav.getValueBoolean();
        }
        return pav.getValueText();
    }

    default PropertyInfoDTO mapPropertyInfo(Property property) {
        if (property == null) {
            return null;
        }
        return PropertyInfoDTO.builder()
                .propertyId(property.getPropertyId())
                .streetAddress(property.getStreetAddress())
                .landSizeM2(property.getLandSizeM2())
                .usableSizeM2(property.getUsableSizeM2())
                .widthM(property.getWidthM())
                .lengthM(property.getLengthM())
                .description(property.getDescriptions())
                .priceRange(toPropertyPriceRangeDto(property.getPriceRange()))
                .build();
    }

    /** Maps owner-entered expected price bands from the property aggregate. */
    default PropertyPriceRangeDTO toPropertyPriceRangeDto(PriceRangeVO vo) {
        if (vo == null) {
            return null;
        }
        MoneyRangeDTO rent = toMoneyRangeDto(vo.getRent());
        MoneyRangeDTO buy = toMoneyRangeDto(vo.getBuy());
        if (rent == null && buy == null) {
            return null;
        }
        return PropertyPriceRangeDTO.builder().rent(rent).buy(buy).build();
    }

    default MoneyRangeDTO toMoneyRangeDto(RangeVO range) {
        if (range == null) {
            return null;
        }
        if (range.getMin() == null && range.getMax() == null) {
            return null;
        }
        return MoneyRangeDTO.builder().min(range.getMin()).max(range.getMax()).build();
    }

    default LocationInfoDTO mapLocationInfo(Location location) {
        if (location == null) {
            return null;
        }

        // Traverse up the location hierarchy to collect names
        Map<LocationType, String> locationNames = new HashMap<>();
        Location current = location;
        while (current != null) {
            locationNames.put(current.getType(), current.getName());
            current = current.getParent();
        }

        return LocationInfoDTO.builder()
                .locationId(location.getLocationId())
                .cityName(locationNames.getOrDefault(LocationType.CITY, null))
                .districtName(locationNames.getOrDefault(LocationType.DISTRICT, null))
                .wardName(locationNames.getOrDefault(LocationType.WARD, null))
                .latitude(location.getNorthLat())
                .longitude(location.getEastLng())
                .build();
    }

    default PropertyTypeInfoDTO mapPropertyTypeInfo(PropertyType propertyType) {
        if (propertyType == null) {
            return null;
        }

        var builder = PropertyTypeInfoDTO.builder()
                .propertyTypeId(propertyType.getPropertyTypeId())
                .propertyTypeName(propertyType.getName())
                .propertyTypeCode(propertyType.getCode());

        if (propertyType.getPropertyCategory() != null) {
            builder.propertyCategoryId(propertyType.getPropertyCategory().getPropertyCategoryId())
                    .propertyCategoryName(propertyType.getPropertyCategory().getName())
                    .propertyCategoryCode(propertyType.getPropertyCategory().getCode());
        }

        return builder.build();
    }

    default AgentInfoDTO mapAgentInfo(User user, SettingPreference preference) {
        if (user == null) {
            return null;
        }
        
        // Privacy Logic: Only show phone if explicitly allowed by preference
        boolean showPhone = preference != null && Boolean.FALSE.equals(preference.getHidePhoneNumber());
        String phoneToShow = (showPhone && user.getPhone() != null) ? user.getPhone() : null;

        return AgentInfoDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .businessName(user.getBusinessName())
                .email(user.getEmail() != null ? user.getEmail().getValue() : null)
                .phone(phoneToShow)
                .avatarUrl(user.getAvatarUrl())
                .company(user.getBusinessName()) // Using businessName as company for now
                .isVerified(user.getStatus() == UserStatus.VERIFIED || user.isEmailVerified())
                .build();
    }

    default List<MediaDTO> toMediaList(List<ListingMedia> listingMedias) {
        if (listingMedias == null) {
            return List.of();
        }
        return listingMedias.stream()
                .filter(lm -> lm.getPropertyMedia() != null && !lm.getDeleted())
                .map(this::toMediaDTO)
                .collect(Collectors.toList());
    }

    default MediaDTO toMediaDTO(ListingMedia listingMedia) {
        if (listingMedia == null || listingMedia.getPropertyMedia() == null) {
            return null;
        }
        PropertyMedia propertyMedia = listingMedia.getPropertyMedia();
        return MediaDTO.builder()
                .mediaId(propertyMedia.getPropertyMediaId())
                .mediaType(propertyMedia.getMediaType())
                .mediaUrl(propertyMedia.getMediaUrl())
                .thumbnailUrl(propertyMedia.getThumbnailUrl())
                .isPrimary(listingMedia.getIsPrimary())
                .displayOrder(listingMedia.getDisplayOrder())
                .metadata(propertyMedia.getMetadata())
                .build();
    }

    default Integer countPhotos(List<ListingMedia> listingMedias) {
        if (listingMedias == null) {
            return 0;
        }
        return (int) listingMedias.stream()
                .filter(lm -> lm.getPropertyMedia() != null
                        && lm.getPropertyMedia().getMediaType() == MediaType.IMAGE
                        && !lm.getDeleted())
                .count();
    }

    default Integer countVideos(List<ListingMedia> listingMedias) {
        if (listingMedias == null) {
            return 0;
        }
        return (int) listingMedias.stream()
                .filter(lm -> lm.getPropertyMedia() != null
                        && lm.getPropertyMedia().getMediaType() == MediaType.VIDEO
                        && !lm.getDeleted())
                .count();
    }

    default Integer count3DTours(List<ListingMedia> listingMedias) {
        if (listingMedias == null) {
            return 0;
        }
        return (int) listingMedias.stream()
                .filter(lm -> lm.getPropertyMedia() != null
                        && lm.getPropertyMedia().getMediaType() == MediaType.THREE_D
                        && !lm.getDeleted())
                .count();
    }

    default List<PropertyAttributeDTO> toAttributeList(List<PropertyAttributeValue> attributeValues) {
        if (attributeValues == null) {
            return List.of();
        }
        // Data arrives pre-ordered by property_type_attributes.priority from the DB query.
        // Re-number from 1 so the returned list is always 1, 2, 3, ...
        // regardless of how many attributes were filtered out upstream.
        List<PropertyAttributeDTO> result = new ArrayList<>();
        for (int i = 0; i < attributeValues.size(); i++) {
            PropertyAttributeDTO dto = toAttributeDTO(attributeValues.get(i));
            if (dto != null) {
                dto.setPriority(i + 1);
                result.add(dto);
            }
        }
        return result;
    }

    default PropertyAttributeDTO toAttributeDTO(PropertyAttributeValue attributeValue) {
        if (attributeValue == null || attributeValue.getPropertyAttribute() == null) {
            return null;
        }
        PropertyAttribute attribute = attributeValue.getPropertyAttribute();
        return PropertyAttributeDTO.builder()
                .attributeId(attribute.getPropertyAttributeId())
                .attributeCode(attribute.getCode())
                .attributeName(attribute.getName())
                .dataType(attribute.getDataType() != null ? attribute.getDataType().name() : null)
                .icon(attribute.getIcon())
                .unit(attribute.getUnit())
                .valueNumber(attributeValue.getValueNumber())
                .valueText(attributeValue.getValueText())
                .valueBoolean(attributeValue.getValueBoolean())
                .build();
    }

    // Amenity mapping methods
    default List<AmenityDTO> toAmenityList(List<PropertyAmenity> propertyAmenities) {
        if (propertyAmenities == null) {
            return List.of();
        }
        return propertyAmenities.stream()
                .map(this::toAmenityDTO)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    default AmenityDTO toAmenityDTO(PropertyAmenity propertyAmenity) {
        if (propertyAmenity == null || propertyAmenity.getAmenity() == null) {
            return null;
        }
        Amenity amenity = propertyAmenity.getAmenity();
        return AmenityDTO.builder()
                .amenityId(amenity.getAmenityId())
                .amenityName(amenity.getAmenityName())
                .amenityType(amenity.getAmenityType() != null ? amenity.getAmenityType().name() : null)
                .description(amenity.getDescription())
                .build();
    }

    /**
     * Map Listing to ListingSearchResponse for search results
     */
    default com.sep.realvista.application.listing.dto.ListingSearchResponse toSearchResponse(Listing listing) {
        if (listing == null) {
            return null;
        }

        var response = com.sep.realvista.application.listing.dto.ListingSearchResponse.builder()
                .listingId(listing.getListingId())
                .name(listing.getName())
                .slug(listing.getSlug())
                .listingType(listing.getListingType())
                .status(listing.getStatus())
                .price(listing.getPrice())
                .isNegotiable(listing.getIsNegotiable())
                .content(listing.getContent())
                .publishedAt(listing.getPublishedAt());

        // Set user type
        if (listing.getUser() != null && listing.getUser().getUserRoles() != null) {
            boolean isAgent = listing.getUser().getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole() != null 
                            && ur.getRole().getRoleCode() == com.sep.realvista.domain.user.role.RoleCode.AGENT);
            response.userType(isAgent ? "AGENT" : "OWNER");
        } else {
            response.userType("USER");
        }

        // Add property details if available
        if (listing.getProperty() != null) {
            Property property = listing.getProperty();
            if (property.getUsableSizeM2() != null) {
                response.area(property.getUsableSizeM2().doubleValue());
                
                // Calculate area in sqft
                BigDecimal areaSqft = property.getUsableSizeM2()
                        .multiply(new BigDecimal("10.764"))
                        .setScale(2, RoundingMode.HALF_UP);
                response.areaSqft(areaSqft);
            }
            
            // Map coordinates
            if (property.getLatitude() != null && property.getLongitude() != null) {
                response.coordinates(
                        com.sep.realvista.application.listing.dto.map.PropertyMapMarker.CoordinatesDTO.builder()
                        .latitude(property.getLatitude())
                        .longitude(property.getLongitude())
                        .build());
            }

            // Map property types
            if (property.getPropertyType() != null) {
                response.propertyTypeName(property.getPropertyType().getName());
                if (property.getPropertyType().getPropertyCategory() != null) {
                    response.propertyCategoryName(property.getPropertyType().getPropertyCategory().getName());
                }
            }

            // Address fields (streetAddress, wardName, districtName, cityName) are populated
            // by the service layer after this mapper call to ensure lazy-loading works correctly
            // within the @Transactional boundary.
        }

        // Boost information and thumbnail will be populated by the service layer
        response.isBoosted(false);

        return response.build();
    }

    /**
     * Map Listing to ListingResponse for CRUD operations
     */
    default com.sep.realvista.application.listing.dto.ListingResponse toListingResponse(Listing listing) {
        if (listing == null) {
            return null;
        }

        var builder = com.sep.realvista.application.listing.dto.ListingResponse.builder()
                .listingId(listing.getListingId())
                .propertyId(listing.getPropertyId())
                .userId(listing.getUserId())
                .listingType(listing.getListingType())
                .status(listing.getStatus())
                .name(listing.getName())
                .slug(listing.getSlug())
                .price(listing.getPrice())
                .minPrice(listing.getMinPrice())
                .maxPrice(listing.getMaxPrice())
                .isNegotiable(listing.getIsNegotiable())
                .availableFrom(listing.getAvailableFrom())
                .content(listing.getContent())
                .publishedAt(listing.getPublishedAt())
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .soldByUserId(listing.getSoldByUserId())
                .soldAt(listing.getSoldAt())
                .rentedByUserId(listing.getRentedByUserId())
                .rentedAt(listing.getRentedAt());

        // Add address fields from property and location
        if (listing.getProperty() != null) {
            builder.streetAddress(listing.getProperty().getStreetAddress());
            
            if (listing.getProperty().getLocation() != null) {
                Location location = listing.getProperty().getLocation();
                
                // Traverse up the location hierarchy to collect names
                java.util.Map<LocationType, String> locationNames = new java.util.HashMap<>();
                Location current = location;
                while (current != null) {
                    locationNames.put(current.getType(), current.getName());
                    current = current.getParent();
                }
                
                builder.wardName(locationNames.get(LocationType.WARD))
                        .districtName(locationNames.get(LocationType.DISTRICT))
                        .cityName(locationNames.get(LocationType.CITY));
            }
        }

        return builder.build();
    }
}
