package com.sep.realvista.application.listing.mapper;

import com.sep.realvista.application.listing.dto.AgentInfoDTO;
import com.sep.realvista.application.listing.dto.ListingDetailResponse;
import com.sep.realvista.application.listing.dto.LocationInfoDTO;
import com.sep.realvista.application.listing.dto.MediaDTO;
import com.sep.realvista.application.listing.dto.PropertyInfoDTO;
import com.sep.realvista.application.listing.dto.PropertyTypeInfoDTO;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingMedia;
import com.sep.realvista.domain.property.PropertyType;
import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.domain.property.location.LocationType;
import com.sep.realvista.domain.property.MediaType;
import com.sep.realvista.domain.property.PropertyMedia;
import com.sep.realvista.domain.user.UserStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
    @Mapping(target = "totalPhotos", ignore = true)
    @Mapping(target = "totalVideos", ignore = true)
    @Mapping(target = "total3DTours", ignore = true)
    ListingDetailResponse toDetailResponse(Listing listing);

    default ListingDetailResponse toDetailResponseWithMedia(
            Listing listing,
            List<ListingMedia> mediaList) {
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

        // Map agent/user
        if (listing.getUser() != null) {
            response.setAgent(mapAgentInfo(listing.getUser()));
        }

        // Map media
        response.setMedia(toMediaList(mediaList));
        response.setTotalPhotos(countPhotos(mediaList));
        response.setTotalVideos(countVideos(mediaList));
        response.setTotal3DTours(count3DTours(mediaList));

        return response;
    }

    default PropertyInfoDTO mapPropertyInfo(com.sep.realvista.domain.property.Property property) {
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
                .slug(property.getSlug())
                .build();
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

    default AgentInfoDTO mapAgentInfo(com.sep.realvista.domain.user.User user) {
        if (user == null) {
            return null;
        }
        return AgentInfoDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .businessName(user.getBusinessName())
                .email(user.getEmail() != null ? user.getEmail().getValue() : null)
                .phone(user.getPhone())
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
}
