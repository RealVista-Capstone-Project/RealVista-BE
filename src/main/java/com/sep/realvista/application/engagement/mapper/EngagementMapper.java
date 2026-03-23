package com.sep.realvista.application.engagement.mapper;

import com.sep.realvista.application.engagement.dto.EngagementDto;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EngagementMapper {

    private final ListingRepository listingRepository;

    public EngagementDto toDto(Engagement engagement) {
        if (engagement == null) {
            return null;
        }

        String listingTitle = "";
        String propertyAddress = "";
        String propertyImageUrl = "";

        if (engagement.getListingId() != null) {
            var listing = listingRepository.findById(engagement.getListingId()).orElse(null);
            if (listing != null) {
                listingTitle = listing.getName();
                if (listing.getProperty() != null) {
                    propertyAddress = listing.getProperty().getStreetAddress();
                }
                propertyImageUrl = listingRepository.findThumbnailByListingId(listing.getListingId()).orElse("");
            }
        }

        return EngagementDto.builder()
                .engagementId(engagement.getEngagementId())
                .initiatorId(engagement.getInitiatorId())
                .receiverId(engagement.getReceiverId())
                .engagementType(engagement.getEngagementType())
                .content(engagement.getContent())
                .listingId(engagement.getListingId())
                .propertyId(engagement.getPropertyId())
                .status(engagement.getStatus())
                .listingTitle(listingTitle)
                .propertyAddress(propertyAddress)
                .propertyImageUrl(propertyImageUrl)
                .createdAt(engagement.getCreatedAt())
                .updatedAt(engagement.getUpdatedAt())
                .build();
    }
}
