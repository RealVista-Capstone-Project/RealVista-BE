package com.sep.realvista.application.engagement.mapper;

import com.sep.realvista.application.engagement.dto.EngagementDto;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
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
            try {
                var listing = listingRepository.findById(engagement.getListingId()).orElse(null);
                if (listing != null) {
                    listingTitle = Optional.ofNullable(listing.getName()).orElse("");
                    if (listing.getProperty() != null) {
                        propertyAddress = Optional.ofNullable(listing.getProperty().getStreetAddress())
                                .orElse("");
                    }
                    propertyImageUrl = listingRepository
                            .findThumbnailByListingId(listing.getListingId())
                            .orElse("");
                }
            } catch (Exception ex) {
                log.warn(
                        "Failed to enrich engagement {} with listing {}: {}",
                        engagement.getEngagementId(),
                        engagement.getListingId(),
                        ex.toString());
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
