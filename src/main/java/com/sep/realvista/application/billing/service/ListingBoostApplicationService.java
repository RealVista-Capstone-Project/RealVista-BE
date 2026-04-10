package com.sep.realvista.application.billing.service;

import com.sep.realvista.application.billing.dto.ListingBoostResponseDto;
import com.sep.realvista.domain.billing.boost.BoostType;
import com.sep.realvista.domain.billing.boost.ListingBoost;
import com.sep.realvista.domain.billing.boost.ListingBoostStatus;
import com.sep.realvista.domain.billing.boost.UserListingBoostPackage;
import com.sep.realvista.domain.billing.boost.repository.ListingBoostRepository;
import com.sep.realvista.domain.billing.boost.repository.UserListingBoostPackageRepository;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ListingBoostApplicationService {

    private final ListingBoostRepository listingBoostRepository;
    private final ListingRepository listingRepository;
    private final UserListingBoostPackageRepository userBoostPackageRepository;

    @Transactional(readOnly = true)
    public List<ListingBoostResponseDto> getActiveBoostsForListing(UUID listingId) {
        return listingBoostRepository.findActiveByListingId(listingId).stream()
                .map(this::toDto)
                .toList();
    }

    public ListingBoostResponseDto applyBoostToListing(UUID userId, UUID listingId, String boostTypeStr) {
        BoostType boostType = parseBoostType(boostTypeStr);

        // Verify listing exists
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", listingId));

        // Verify listing belongs to user
        if (!listing.getUserId().equals(userId)) {
            throw new BusinessConflictException("Listing does not belong to the current user");
        }

        // Only published listings can have boosts applied
        if (listing.getStatus() != ListingStatus.PUBLISHED) {
            throw new BusinessConflictException("Boosts can only be applied to published listings");
        }

        // Check no active boost of same type already exists
        listingBoostRepository.findActiveByListingIdAndBoostType(listingId, boostType)
                .ifPresent(existing -> {
                    throw new BusinessConflictException("Listing already has an active " + boostType + " boost");
                });

        // Find user's active boost packages
        List<UserListingBoostPackage> activePackages = userBoostPackageRepository.findAllActiveByUserId(userId);

        // Pick first usable package
        UserListingBoostPackage userBoostPackage = activePackages.stream()
                .filter(UserListingBoostPackage::isUsable)
                .findFirst()
                .orElseThrow(() -> new BusinessConflictException("No active boost package found"));

        // Check and decrement quota
        if (boostType == BoostType.FEATURED) {
            if (userBoostPackage.getRemainingFeaturedQuota() == null
                    || userBoostPackage.getRemainingFeaturedQuota() <= 0) {
                throw new BusinessConflictException("Insufficient quota");
            }
            userBoostPackage.decrementFeaturedQuota();
        } else {
            if (userBoostPackage.getRemainingHotBadgeQuota() == null
                    || userBoostPackage.getRemainingHotBadgeQuota() <= 0) {
                throw new BusinessConflictException("Insufficient quota");
            }
            userBoostPackage.decrementHotBadgeQuota();
        }

        // Save updated UserListingBoostPackage
        userBoostPackageRepository.save(userBoostPackage);

        // Determine end date
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = userBoostPackage.getEndDate() != null
                ? userBoostPackage.getEndDate()
                : startDate.plusDays(userBoostPackage.getBoostPackage().getDurationDays());

        // Create and save ListingBoost
        ListingBoost listingBoost = ListingBoost.builder()
                .boostPackageId(userBoostPackage.getBoostPackageId())
                .listingId(listingId)
                .userId(userId)
                .boostType(boostType)
                .startDate(startDate)
                .endDate(endDate)
                .status(ListingBoostStatus.ACTIVE)
                .build();

        ListingBoost saved = listingBoostRepository.save(listingBoost);
        return toDto(saved);
    }

    public void removeBoostFromListing(UUID userId, UUID listingId, String boostTypeStr) {
        BoostType boostType = parseBoostType(boostTypeStr);

        // Find active boost
        ListingBoost listingBoost = listingBoostRepository.findActiveByListingIdAndBoostType(listingId, boostType)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active " + boostType + " boost found for this listing"));

        // Verify it belongs to user
        if (!listingBoost.getUserId().equals(userId)) {
            throw new BusinessConflictException("Boost does not belong to the current user");
        }

        // Cancel the boost
        listingBoost.cancel();
        listingBoostRepository.save(listingBoost);

        // Return quota to the user's boost package
        UUID boostPackageId = listingBoost.getBoostPackageId();
        List<UserListingBoostPackage> activePackages = userBoostPackageRepository.findAllActiveByUserId(userId);
        activePackages.stream()
                .filter(pkg -> pkg.getBoostPackageId().equals(boostPackageId))
                .findFirst()
                .ifPresent(userBoostPackage -> {
                    if (boostType == BoostType.FEATURED) {
                        userBoostPackage.incrementFeaturedQuota();
                    } else {
                        userBoostPackage.incrementHotBadgeQuota();
                    }
                    userBoostPackageRepository.save(userBoostPackage);
                });
    }

    private BoostType parseBoostType(String boostTypeStr) {
        try {
            return BoostType.valueOf(boostTypeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid boost type: " + boostTypeStr
                    + ". Must be FEATURED or HOT_BADGE");
        }
    }

    private ListingBoostResponseDto toDto(ListingBoost listingBoost) {
        return ListingBoostResponseDto.builder()
                .listingBoostId(listingBoost.getListingBoostId())
                .listingId(listingBoost.getListingId())
                .boostType(listingBoost.getBoostType().name())
                .startDate(listingBoost.getStartDate())
                .endDate(listingBoost.getEndDate())
                .status(listingBoost.getStatus().name())
                .build();
    }
}
