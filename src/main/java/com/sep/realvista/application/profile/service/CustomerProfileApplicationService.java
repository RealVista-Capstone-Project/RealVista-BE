package com.sep.realvista.application.profile.service;

import com.sep.realvista.application.profile.dto.CreateCustomerProfileRequest;
import com.sep.realvista.application.profile.dto.CustomerProfileResponse;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.profile.CustomerProfile;
import com.sep.realvista.domain.profile.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomerProfileApplicationService {

    private final CustomerProfileRepository customerProfileRepository;

    public void createDefaultProfile(UUID userId, String profileName) {
        if (customerProfileRepository.existsByUserId(userId)) {
            log.warn("CustomerProfile already exists for userId: {}", userId);
            return;
        }
        CustomerProfile profile = CustomerProfile.builder()
                .userId(userId)
                .profileName(profileName)
                .isActive(true)
                .build();
        customerProfileRepository.save(profile);
        log.info("Default CustomerProfile created for userId: {}", userId);
    }

    @Transactional(readOnly = true)
    public List<CustomerProfileResponse> getAllProfiles(UUID userId) {
        return customerProfileRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CustomerProfileResponse createProfile(UUID userId, CreateCustomerProfileRequest request) {
        CustomerProfile profile = CustomerProfile.builder()
                .userId(userId)
                .profileName(request.getProfileName())
                .isActive(false)
                .build();
        CustomerProfile saved = customerProfileRepository.save(profile);
        log.info("CustomerProfile created: {} for userId: {}", saved.getCustomerProfileId(), userId);
        return toResponse(saved);
    }

    public void deleteProfile(UUID profileId, UUID userId) {
        CustomerProfile profile = customerProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerProfile not found: " + profileId));

        if (!profile.getUserId().equals(userId)) {
            throw new BusinessConflictException("Profile does not belong to user", "PROFILE_OWNERSHIP_VIOLATION");
        }

        if (profile.isActive()) {
            throw new BusinessConflictException("Cannot delete the active profile", "CANNOT_DELETE_ACTIVE_PROFILE");
        }

        customerProfileRepository.deleteById(profileId);
        log.info("CustomerProfile deleted: {} for userId: {}", profileId, userId);
    }

    public CustomerProfileResponse switchMainProfile(UUID profileId, UUID userId) {
        CustomerProfile newActive = customerProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerProfile not found: " + profileId));

        if (!newActive.getUserId().equals(userId)) {
            throw new BusinessConflictException("Profile does not belong to user", "PROFILE_OWNERSHIP_VIOLATION");
        }

        // Deactivate current active profile
        customerProfileRepository.findByUserIdAndIsActiveTrue(userId)
                .ifPresent(current -> {
                    current.deactivate();
                    customerProfileRepository.save(current);
                });

        // Activate selected profile
        newActive.activate();
        CustomerProfile saved = customerProfileRepository.save(newActive);
        log.info("CustomerProfile switched to: {} for userId: {}", profileId, userId);
        return toResponse(saved);
    }

    private CustomerProfileResponse toResponse(CustomerProfile profile) {
        return CustomerProfileResponse.builder()
                .customerProfileId(profile.getCustomerProfileId())
                .userId(profile.getUserId())
                .profileName(profile.getProfileName())
                .isActive(profile.getIsActive())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
