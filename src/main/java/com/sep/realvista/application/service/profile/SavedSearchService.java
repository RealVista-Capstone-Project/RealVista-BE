package com.sep.realvista.application.service.profile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.profile.dto.SaveSearchRequest;
import com.sep.realvista.application.profile.dto.SavedSearchDto;
import com.sep.realvista.application.profile.mapper.SavedSearchMapper;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.profile.CustomerProfile;
import com.sep.realvista.domain.profile.SavedSearch;
import com.sep.realvista.domain.profile.repository.CustomerProfileRepository;
import com.sep.realvista.domain.profile.repository.SavedSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavedSearchService {

    private final SavedSearchRepository savedSearchRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final SavedSearchMapper savedSearchMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public SavedSearchDto saveSearch(UUID userId, SaveSearchRequest request) {
        CustomerProfile profile;
        if (request.getProfileId() != null) {
            profile = customerProfileRepository.findById(request.getProfileId())
                    .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
            if (!profile.getUserId().equals(userId)) {
                throw new org.springframework.security.access.AccessDeniedException("Profile does not belong to user");
            }
        } else {
            profile = customerProfileRepository.findByUserIdAndIsActiveTrueAndDeletedFalse(userId)
                    .orElseGet(() -> {
                        CustomerProfile newProfile = CustomerProfile.builder()
                                .userId(userId)
                                .profileName("Default Profile")
                                .build();
                        return customerProfileRepository.save(newProfile);
                    });
        }

        String criteriaJson;
        try {
            criteriaJson = objectMapper.writeValueAsString(request.getCriteria());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid criteria format");
        }

        // Check for existing saved search with the same criteria
        Optional<SavedSearch> existingOpt = savedSearchRepository
                .findByProfileIdAndSearchTypeAndCriteriaAndDeletedFalse(
                        profile.getCustomerProfileId(), request.getSearchType(), criteriaJson);

        if (existingOpt.isPresent()) {
            throw new BusinessConflictException(
                    "Tìm kiếm này đã được lưu trước đó",
                    "SAVED_SEARCH_DUPLICATE"
            );
        }

        SavedSearch savedSearch = SavedSearch.builder()
                .profileId(profile.getCustomerProfileId())
                .searchType(request.getSearchType())
                .criteria(criteriaJson)
                .boardId(request.getBoardId())
                .isRecommendation(request.isRecommendation())
                .build();

        savedSearch = savedSearchRepository.save(savedSearch);
        return savedSearchMapper.toDto(savedSearch);
    }

    @Transactional(readOnly = true)
    public List<SavedSearchDto> getMySavedSearches(UUID userId) {
        List<CustomerProfile> profiles = customerProfileRepository
                .findAllByUserIdAndDeletedFalse(userId);
        if (profiles.isEmpty()) {
            return List.of();
        }

        List<UUID> profileIds = profiles.stream()
                .map(CustomerProfile::getCustomerProfileId)
                .toList();

        return savedSearchRepository.findByProfileIdInAndDeletedFalse(profileIds)
                .stream()
                .map(savedSearchMapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteSavedSearch(UUID savedSearchId, UUID userId) {
        List<CustomerProfile> profiles = customerProfileRepository
                .findAllByUserIdAndDeletedFalse(userId);
        if (profiles.isEmpty()) {
            throw new ResourceNotFoundException("Profile", userId);
        }

        List<UUID> profileIds = profiles.stream()
                .map(CustomerProfile::getCustomerProfileId)
                .toList();

        SavedSearch savedSearch = savedSearchRepository.findById(savedSearchId)
                .filter(s -> Boolean.FALSE.equals(s.getDeleted()) && profileIds.contains(s.getProfileId()))
                .orElseThrow(() -> new ResourceNotFoundException("Saved Search", savedSearchId));

        savedSearch.markAsDeleted();
        savedSearchRepository.save(savedSearch);
    }
}
