package com.sep.realvista.application.property.mapper;

import java.util.UUID;
import com.sep.realvista.application.listing.dto.AmenityDTO;
import com.sep.realvista.application.listing.dto.LocationInfoDTO;
import com.sep.realvista.application.listing.dto.MediaDTO;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.application.listing.dto.PropertyTypeInfoDTO;
import com.sep.realvista.application.property.dto.PropertyDetailResponse;
import com.sep.realvista.application.property.dto.PropertyFeedItemResponse;
import com.sep.realvista.application.property.dto.PropertySummaryResponse;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyMedia;
import com.sep.realvista.domain.property.amenity.PropertyAmenity;
import com.sep.realvista.domain.property.attribute.PropertyAttribute;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PropertyMapper {

    public PropertyDetailResponse toDetailResponse(Property property, 
                                                   List<PropertyMedia> media, 
                                                   List<PropertyAttributeValue> attributes, 
                                                   List<PropertyAmenity> amenities) {
        
        UUID districtId = null;
        UUID cityId = null;
        
        if (property.getLocation() != null && property.getLocation().getParent() != null) {
            districtId = property.getLocation().getParent().getLocationId();
            if (property.getLocation().getParent().getParent() != null) {
                cityId = property.getLocation().getParent().getParent().getLocationId();
            }
        }

        return PropertyDetailResponse.builder()
                .propertyId(property.getPropertyId())
                .ownerId(property.getOwnerId())
                .locationId(property.getLocationId())
                .districtId(districtId)
                .cityId(cityId)
                .propertyTypeId(property.getPropertyTypeId())
                .propertyTypeCode(property.getPropertyType() != null ? property.getPropertyType().getCode() : null)
                .streetAddress(property.getStreetAddress())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .landSizeM2(property.getLandSizeM2())
                .usableSizeM2(property.getUsableSizeM2())
                .widthM(property.getWidthM())
                .lengthM(property.getLengthM())
                .status(property.getStatus())
                .descriptions(property.getDescriptions())
                .slug(property.getSlug())
                .extraAttributes(property.getExtraAttributes())
                .attributes(attributes != null ? attributes.stream()
                        .map(this::mapAttribute).collect(Collectors.toList()) : null)
                .amenities(amenities != null ? amenities.stream()
                        .map(this::mapAmenity).collect(Collectors.toList()) : null)
                .media(media != null ? media.stream()
                        .map(this::mapMedia).collect(Collectors.toList()) : null)
                .build();
    }

    public PropertySummaryResponse toSummaryResponse(Property property, List<PropertyMedia> media,
                                                     List<PropertyAttributeValue> attributes,
                                                     List<PropertyAmenity> amenities) {
        java.math.BigDecimal areaSqft = null;
        if (property.getUsableSizeM2() != null) {
            areaSqft = property.getUsableSizeM2().multiply(new java.math.BigDecimal("10.764"))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }

        return PropertySummaryResponse.builder()
                .propertyId(property.getPropertyId())
                .propertyTypeId(property.getPropertyTypeId())
                .streetAddress(property.getStreetAddress())
                .status(property.getStatus())
                .landSizeM2(property.getLandSizeM2())
                .usableSizeM2(property.getUsableSizeM2())
                .widthM(property.getWidthM())
                .lengthM(property.getLengthM())
                .areaSqft(areaSqft)
                .description(property.getDescriptions())
                .media(media != null ? media.stream()
                        .map(this::mapMedia).collect(Collectors.toList()) : null)
                .attributes(attributes != null ? attributes.stream()
                        .map(this::mapAttribute).collect(Collectors.toList()) : null)
                .amenities(amenities != null ? amenities.stream()
                        .map(this::mapAmenity).collect(Collectors.toList()) : null)
                .propertyTypeInfo(mapPropertyType(property))
                .locationInfo(mapLocation(property))
                .build();
    }

    public PropertySummaryResponse toSummaryResponse(Property property, String thumbnailUrl) {
        return PropertySummaryResponse.builder()
                .propertyId(property.getPropertyId())
                .propertyTypeId(property.getPropertyTypeId())
                .streetAddress(property.getStreetAddress())
                .status(property.getStatus())
                .landSizeM2(property.getLandSizeM2())
                .usableSizeM2(property.getUsableSizeM2())
                .widthM(property.getWidthM())
                .lengthM(property.getLengthM())
                .description(property.getDescriptions())
                .media(thumbnailUrl != null ? List.of(MediaDTO.builder()
                        .thumbnailUrl(thumbnailUrl)
                        .isPrimary(true)
                        .build()) : null)
                .propertyTypeInfo(mapPropertyType(property))
                .locationInfo(mapLocation(property))
                .build();
    }

    /**
     * Maps a Property and its related collections to a PropertyFeedItemResponse.
     *
     * @param property          the property entity (with eagerly fetched type and location)
     * @param media             media items for this property
     * @param attributes        attribute values for this property
     * @param amenities         amenities for this property
     * @param hasActiveProposal whether the agent has already submitted a proposal
     * @return the feed item response DTO
     */
    public PropertyFeedItemResponse toFeedItemResponse(
            Property property,
            List<PropertyMedia> media,
            List<PropertyAttributeValue> attributes,
            List<PropertyAmenity> amenities,
            boolean hasActiveProposal) {

        return PropertyFeedItemResponse.builder()
                .propertyId(property.getPropertyId())
                .ownerId(property.getOwnerId())
                .streetAddress(property.getStreetAddress())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .landSizeM2(property.getLandSizeM2())
                .usableSizeM2(property.getUsableSizeM2())
                .widthM(property.getWidthM())
                .lengthM(property.getLengthM())
                .status(property.getStatus())
                .descriptions(property.getDescriptions())
                .priceRange(property.getPriceRange())
                .propertyTypeInfo(mapPropertyType(property))
                .locationInfo(mapLocation(property))
                .media(media != null ? media.stream()
                        .map(this::mapMedia).collect(Collectors.toList()) : null)
                .attributes(attributes != null ? attributes.stream()
                        .map(this::mapAttribute).collect(Collectors.toList()) : null)
                .amenities(amenities != null ? amenities.stream()
                        .map(this::mapAmenity).collect(Collectors.toList()) : null)
                .hasActiveProposal(hasActiveProposal)
                .build();
    }

    private PropertyTypeInfoDTO mapPropertyType(Property property) {
        var pt = property.getPropertyType();
        if (pt == null) {
            return null;
        }
        var cat = pt.getPropertyCategory();
        return PropertyTypeInfoDTO.builder()
                .propertyTypeId(pt.getPropertyTypeId())
                .propertyTypeName(pt.getName())
                .propertyTypeCode(pt.getCode())
                .propertyCategoryId(cat != null ? cat.getPropertyCategoryId() : null)
                .propertyCategoryName(cat != null ? cat.getName() : null)
                .propertyCategoryCode(cat != null ? cat.getCode() : null)
                .build();
    }

    private LocationInfoDTO mapLocation(Property property) {
        var loc = property.getLocation();
        if (loc == null) {
            return null;
        }

        String wardName = null;
        String districtName = null;
        String cityName = null;

        if (loc.isWard()) {
            wardName = loc.getName();
            if (loc.getParent() != null) {
                districtName = loc.getParent().getName();
                if (loc.getParent().getParent() != null) {
                    cityName = loc.getParent().getParent().getName();
                }
            }
        } else if (loc.isDistrict()) {
            districtName = loc.getName();
            if (loc.getParent() != null) {
                cityName = loc.getParent().getName();
            }
        } else if (loc.isCity()) {
            cityName = loc.getName();
        }

        return LocationInfoDTO.builder()
                .locationId(loc.getLocationId())
                .wardName(wardName)
                .districtName(districtName)
                .cityName(cityName)
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .build();
    }

    private AmenityDTO mapAmenity(PropertyAmenity pa) {
        var amenity = pa.getAmenity();
        return AmenityDTO.builder()
                .amenityId(amenity.getAmenityId())
                .amenityName(amenity.getAmenityName())
                .build();
    }

    private PropertyAttributeDTO mapAttribute(PropertyAttributeValue pav) {
        PropertyAttribute attr = pav.getPropertyAttribute();
        return PropertyAttributeDTO.builder()
                .attributeId(attr.getPropertyAttributeId())
                .attributeCode(attr.getCode())
                .attributeName(attr.getName())
                .dataType(attr.getDataType().name())
                .icon(attr.getIcon())
                .unit(attr.getUnit())
                .valueNumber(pav.getValueNumber())
                .valueText(pav.getValueText())
                .valueBoolean(pav.getValueBoolean())
                .build();
    }

    private MediaDTO mapMedia(PropertyMedia pm) {
        return MediaDTO.builder()
                .mediaId(pm.getPropertyMediaId())
                .mediaUrl(pm.getMediaUrl())
                .thumbnailUrl(pm.getThumbnailUrl())
                .mediaType(pm.getMediaType())
                .isPrimary(pm.getIsPrimary())
                .isPropertyStandard(pm.getIsPropertyStandard())
                .displayOrder(0)
                .build();
    }
}