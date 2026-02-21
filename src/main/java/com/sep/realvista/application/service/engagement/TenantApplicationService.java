package com.sep.realvista.application.service.engagement;

import com.sep.realvista.application.engagement.mapper.TenantApplicationMapper;
import com.sep.realvista.application.listing.dto.TenantApplicationDto;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.engagement.rental.TenantApplication;
import com.sep.realvista.domain.engagement.rental.repository.TenantApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.sep.realvista.domain.engagement.rental.TenantApplicationStatus;
import com.sep.realvista.domain.engagement.rental.TenantRentalProfileRepository;
import com.sep.realvista.domain.engagement.rental.TenantRentalProfile;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.listing.Listing;

@Service
@RequiredArgsConstructor
public class TenantApplicationService {

    private final TenantApplicationRepository tenantApplicationRepository;
    private final TenantApplicationMapper tenantApplicationMapper;
    private final TenantRentalProfileRepository tenantRentalProfileRepository;
    private final ListingRepository listingRepository;

    @Transactional(readOnly = true)
    public List<TenantApplicationDto> getMyApplications(UUID userId) {
        List<TenantApplication> applications = tenantApplicationRepository.findByUserId(userId);
        return applications.stream()
                .map(tenantApplicationMapper::toDto)
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

    @Transactional
    public TenantApplicationDto submitApplication(UUID listingId, UUID profileId, UUID userId) {
        TenantRentalProfile profile = tenantRentalProfileRepository
                .findByProfileIdAndUserIdAndDeletedFalse(profileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant Rental Profile", profileId));

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", listingId));

        TenantApplication application = TenantApplication.builder()
                .userId(userId)
                .listingId(listingId)
                .rentalProfileId(profileId)
                .title(listing.getName() + " - " + profile.getTitle())
                .monthlyIncome(profile.getMonthlyIncome())
                .moveInDate(profile.getMoveInDate())
                .leaseTermMonths(profile.getLeaseTermMonths())
                .note(profile.getNote())
                .status(TenantApplicationStatus.ACTIVE)
                .build();

        return tenantApplicationMapper.toDto(tenantApplicationRepository.save(application));
    }
}
