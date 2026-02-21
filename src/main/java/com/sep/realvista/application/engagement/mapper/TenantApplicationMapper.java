package com.sep.realvista.application.engagement.mapper;

import com.sep.realvista.application.listing.dto.TenantApplicationDto;
import com.sep.realvista.domain.engagement.rental.TenantApplication;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantApplicationMapper {

    private final ListingRepository listingRepository;

    public TenantApplicationDto toDto(TenantApplication app) {
        if (app == null) {
            return null;
        }

        var listing = listingRepository.findById(app.getListingId()).orElse(null);
        String propertyAddress = "";
        String propertyImageUrl = "";
        String title = app.getTitle();

        if (listing != null) {
            if (listing.getProperty() != null) {
                propertyAddress = listing.getProperty().getStreetAddress();
            }
            title = listing.getName();
            
            // Get Thumbnail
            propertyImageUrl = listingRepository.findThumbnailByListingId(listing.getListingId()).orElse("");
        }

        return TenantApplicationDto.builder()
                .tenantApplicationId(app.getTenantApplicationId())
                .userId(app.getUserId())
                .rentalProfileId(app.getRentalProfileId())
                .listingId(app.getListingId())
                .title(title)
                .propertyAddress(propertyAddress)
                .propertyImageUrl(propertyImageUrl)
                .monthlyIncome(app.getMonthlyIncome())
                .moveInDate(app.getMoveInDate())
                .leaseTermMonths(app.getLeaseTermMonths())
                .status(app.getStatus())
                .note(app.getNote())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}