package com.sep.realvista.application.property.mapper;

import java.util.UUID;
import com.sep.realvista.application.listing.dto.AmenityDTO;
import com.sep.realvista.application.listing.dto.MediaDTO;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.application.property.dto.PropertyDetailResponse;
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

    public PropertySummaryResponse toSummaryResponse(Property property, String thumbnailUrl, boolean has3d) {
        return PropertySummaryResponse.builder()
                .propertyId(property.getPropertyId())
                .propertyTypeId(property.getPropertyTypeId())
                .streetAddress(property.getStreetAddress())
                .status(property.getStatus())
                .landSizeM2(property.getLandSizeM2())
                .thumbnailUrl(thumbnailUrl)
                .has3d(has3d)
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
                .displayOrder(0)
                .metadata(pm.getMetadata())
                .build();
    }
}