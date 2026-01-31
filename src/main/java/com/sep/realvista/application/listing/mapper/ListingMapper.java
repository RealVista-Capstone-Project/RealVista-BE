package com.sep.realvista.application.listing.mapper;

import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.domain.listing.Listing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for Listing entity and Search DTOs.
 */
@Mapper(componentModel = "spring")
public interface ListingMapper {

    @Mapping(target = "listingId", source = "listingId")
    @Mapping(target = "title", source = ".", qualifiedByName = "formatTitle")
    @Mapping(target = "propertyType", source = "property.propertyType.name")
    @Mapping(target = "location", source = "property.location.name")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "area", source = "property.usableSizeM2")
    @Mapping(target = "thumbnailUrl", ignore = true) // Will be implemented when media is ready
    ListingSearchResponse toSearchResponse(Listing listing);

    @Named("formatTitle")
    default String formatTitle(Listing listing) {
        if (listing.getProperty() == null) {
            return "Untitled Listing";
        }
        return "Property at " + listing.getProperty().getStreetAddress();
    }
}
