package com.sep.realvista.application.service.engagement;

import com.sep.realvista.application.listing.dto.TenantApplicationDto;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.engagement.rental.TenantApplication;
import com.sep.realvista.domain.engagement.rental.repository.TenantApplicationRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantApplicationService {

    private final TenantApplicationRepository tenantApplicationRepository;
    private final ListingRepository listingRepository;

    @Transactional(readOnly = true)
    public List<TenantApplicationDto> getMyApplications(UUID userId) {
        List<TenantApplication> applications = tenantApplicationRepository.findByUserId(userId);
        return applications.stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public void softDeleteApplication(UUID applicationId, UUID userId) {
        TenantApplication application = tenantApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant Application", applicationId));

        if (!application.getUserId().equals(userId)) {
             throw new SecurityException("You are not authorized to delete this application");
        }

        tenantApplicationRepository.delete(application);
    }

    private TenantApplicationDto mapToDto(TenantApplication app) {
        // Fetch listing to get details
        var listing = listingRepository.findById(app.getListingId()).orElse(null);
        String propertyAddress = "";
        String propertyImageUrl = "";
        String title = app.getTitle();

        if (listing != null) {
            // We might need to fetch the property eagerly or rely on Lazy loading within Transaction
            // Since we are in @Transactional, lazy loading property should work if session is open
            // but for safety/performance, we might want a join fetch in repository.
            // For now, let's rely on the fact that we can access it.
            // However, Listing -> Property is Lazy.
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
