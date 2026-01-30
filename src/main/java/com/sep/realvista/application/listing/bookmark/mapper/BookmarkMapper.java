package com.sep.realvista.application.listing.bookmark.mapper;

import com.sep.realvista.application.listing.bookmark.dto.BookmarkResponse;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @org.mapstruct.Named("getFullName")
    default String getFullName(User user) {
        return user.getFullName();
    }

    @org.mapstruct.Named("getPropertyAddress")
    default String getPropertyAddress(Listing listing) {
        return listing.getProperty() != null ? 
            listing.getProperty().getStreetAddress() : 
            "Address not available";
    }
}