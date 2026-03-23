package com.sep.realvista.unit.application.profile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.profile.dto.SaveSearchRequest;
import com.sep.realvista.application.profile.dto.SavedSearchDto;
import com.sep.realvista.application.profile.mapper.SavedSearchMapper;
import com.sep.realvista.application.service.profile.SavedSearchService;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.profile.CustomerProfile;
import com.sep.realvista.domain.profile.SavedSearch;
import com.sep.realvista.domain.profile.SearchType;
import com.sep.realvista.domain.profile.repository.CustomerProfileRepository;
import com.sep.realvista.domain.profile.repository.SavedSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SavedSearchService Unit Tests")
class SavedSearchServiceUnitTest {

    @Mock
    private SavedSearchRepository savedSearchRepository;

    @Mock
    private CustomerProfileRepository customerProfileRepository;

    @Mock
    private SavedSearchMapper savedSearchMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SavedSearchService savedSearchService;

    private UUID userId;
    private UUID profileId;
    private UUID savedSearchId;
    private CustomerProfile mockProfile;
    private SavedSearch mockSavedSearch;
    private SaveSearchRequest mockRequest;
    private SavedSearchDto mockDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        profileId = UUID.randomUUID();
        savedSearchId = UUID.randomUUID();

        mockProfile = CustomerProfile.builder()
                .customerProfileId(profileId)
                .userId(userId)
                .profileName("Test Profile")
                .isActive(true)
                .build();

        mockSavedSearch = SavedSearch.builder()
                .savedSearchId(savedSearchId)
                .profileId(profileId)
                .searchType(SearchType.RENT)
                .criteria("{\"minPrice\": 1000}")
                .build();

        mockRequest = SaveSearchRequest.builder()
                .searchType(SearchType.RENT)
                .criteria(Map.of("minPrice", 1000))
                .build();

        mockDto = SavedSearchDto.builder()
                .savedSearchId(savedSearchId)
                .searchType(SearchType.RENT)
                .criteria(Map.of("minPrice", 1000))
                .build();
    }

    @Test
    @DisplayName("saveSearch should return DTO when successful")
    void saveSearch_whenValidRequest_shouldSaveAndReturnDto() throws JsonProcessingException {
        when(customerProfileRepository.findByUserIdAndIsActiveTrueAndDeletedFalse(userId))
                .thenReturn(Optional.of(mockProfile));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"minPrice\": 1000}");
        when(savedSearchRepository.save(any(SavedSearch.class))).thenReturn(mockSavedSearch);
        when(savedSearchMapper.toDto(mockSavedSearch)).thenReturn(mockDto);

        SavedSearchDto result = savedSearchService.saveSearch(userId, mockRequest);

        assertThat(result).isNotNull();
        assertThat(result.getSavedSearchId()).isEqualTo(savedSearchId);
        verify(savedSearchRepository).save(any(SavedSearch.class));
    }

    @Test
    @DisplayName("saveSearch should auto-create profile and return DTO when profile not found")
    void saveSearch_whenProfileNotFound_shouldAutoCreateAndSave() throws JsonProcessingException {
        // Profile doesn't exist → service creates one automatically
        CustomerProfile autoCreated = CustomerProfile.builder()
                .customerProfileId(profileId)
                .userId(userId)
                .profileName("Default Profile")
                .isActive(true)
                .build();

        when(customerProfileRepository.findByUserIdAndIsActiveTrueAndDeletedFalse(userId))
                .thenReturn(Optional.empty());
        when(customerProfileRepository.save(any(CustomerProfile.class))).thenReturn(autoCreated);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"minPrice\": 1000}");
        when(savedSearchRepository.findByProfileIdAndSearchTypeAndCriteriaAndDeletedFalse(
                any(), any(), any())).thenReturn(Optional.empty());
        when(savedSearchRepository.save(any(SavedSearch.class))).thenReturn(mockSavedSearch);
        when(savedSearchMapper.toDto(mockSavedSearch)).thenReturn(mockDto);

        SavedSearchDto result = savedSearchService.saveSearch(userId, mockRequest);

        assertThat(result).isNotNull();
        assertThat(result.getSavedSearchId()).isEqualTo(savedSearchId);
        verify(customerProfileRepository).save(any(CustomerProfile.class));
        verify(savedSearchRepository).save(any(SavedSearch.class));
    }

    @Test
    @DisplayName("saveSearch should throw BusinessConflictException when duplicate search exists")
    void saveSearch_whenDuplicate_shouldThrowConflict() throws JsonProcessingException {
        when(customerProfileRepository.findByUserIdAndIsActiveTrueAndDeletedFalse(userId))
                .thenReturn(Optional.of(mockProfile));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"minPrice\": 1000}");
        when(savedSearchRepository.findByProfileIdAndSearchTypeAndCriteriaAndDeletedFalse(
                profileId, SearchType.RENT, "{\"minPrice\": 1000}"))
                .thenReturn(Optional.of(mockSavedSearch));

        assertThatThrownBy(() -> savedSearchService.saveSearch(userId, mockRequest))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("đã được lưu");
    }

    @Test
    @DisplayName("getMySavedSearches should return list")
    void getMySavedSearches_whenProfileExists_shouldReturnList() {
        when(customerProfileRepository.findByUserIdAndIsActiveTrueAndDeletedFalse(userId))
                .thenReturn(Optional.of(mockProfile));
        when(savedSearchRepository.findByProfileIdAndDeletedFalse(profileId))
                .thenReturn(List.of(mockSavedSearch));
        when(savedSearchMapper.toDto(mockSavedSearch)).thenReturn(mockDto);

        List<SavedSearchDto> result = savedSearchService.getMySavedSearches(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSavedSearchId()).isEqualTo(savedSearchId);
    }

    @Test
    @DisplayName("deleteSavedSearch should mark as deleted")
    void deleteSavedSearch_whenValid_shouldMarkAsDeleted() {
        when(customerProfileRepository.findByUserIdAndIsActiveTrueAndDeletedFalse(userId))
                .thenReturn(Optional.of(mockProfile));
        when(savedSearchRepository.findBySavedSearchIdAndProfileIdAndDeletedFalse(savedSearchId, profileId))
                .thenReturn(Optional.of(mockSavedSearch));

        savedSearchService.deleteSavedSearch(savedSearchId, userId);

        assertThat(mockSavedSearch.getDeleted()).isTrue();
        verify(savedSearchRepository).save(mockSavedSearch);
    }
}
