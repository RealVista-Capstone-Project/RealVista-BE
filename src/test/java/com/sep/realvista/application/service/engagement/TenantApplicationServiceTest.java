package com.sep.realvista.application.service.engagement;

import com.sep.realvista.application.listing.dto.TenantApplicationDto;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.engagement.rental.TenantApplication;
import com.sep.realvista.domain.engagement.rental.TenantApplicationStatus;
import com.sep.realvista.domain.engagement.rental.repository.TenantApplicationRepository;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.application.engagement.mapper.TenantApplicationMapper;
import com.sep.realvista.domain.engagement.rental.TenantRentalProfileRepository;
import com.sep.realvista.domain.engagement.rental.TenantRentalProfile;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantApplicationServiceTest {

    @Mock
    private TenantApplicationRepository tenantApplicationRepository;

    @Mock
    private TenantApplicationMapper tenantApplicationMapper;

    @Mock
    private TenantRentalProfileRepository tenantRentalProfileRepository;

    @Mock
    private ListingRepository listingRepository;

    @InjectMocks
    private TenantApplicationService tenantApplicationService;

    @Test
    void getMyApplications_ShouldReturnDtoList() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        TenantApplication application = TenantApplication.builder()
                .tenantApplicationId(applicationId)
                .userId(userId)
                .listingId(listingId)
                .monthlyIncome(BigDecimal.valueOf(5000))
                .status(TenantApplicationStatus.DRAFT)
                .build();

        TenantApplicationDto dto = TenantApplicationDto.builder()
                .tenantApplicationId(applicationId)
                .title("Test Listing")
                .propertyAddress("123 Main St")
                .propertyImageUrl("http://image.url")
                .build();

        when(tenantApplicationRepository.findByUserId(userId)).thenReturn(List.of(application));
        when(tenantApplicationMapper.toDto(application)).thenReturn(dto);

        // Act
        List<TenantApplicationDto> result = tenantApplicationService.getMyApplications(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(applicationId, result.get(0).getTenantApplicationId());
        assertEquals("Test Listing", result.get(0).getTitle());
        assertEquals("123 Main St", result.get(0).getPropertyAddress());
        assertEquals("http://image.url", result.get(0).getPropertyImageUrl());

        verify(tenantApplicationRepository).findByUserId(userId);
        verify(tenantApplicationMapper).toDto(application);
    }

    @Test
    void softDeleteApplication_ShouldDelete_WhenUserIsOwner() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        TenantApplication application = TenantApplication.builder()
                .tenantApplicationId(applicationId)
                .userId(userId)
                .status(TenantApplicationStatus.DRAFT)
                .build();

        when(tenantApplicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        // Act
        tenantApplicationService.softDeleteApplication(applicationId, userId);

        // Assert
        verify(tenantApplicationRepository).delete(application);
    }

    @Test
    void softDeleteApplication_ShouldThrowSecurityException_WhenUserIsNotOwner() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        TenantApplication application = TenantApplication.builder()
                .tenantApplicationId(applicationId)
                .userId(otherUserId) // Different user
                .build();

        when(tenantApplicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        // Act & Assert
        assertThrows(SecurityException.class, () ->
                tenantApplicationService.softDeleteApplication(applicationId, userId));
        
        verify(tenantApplicationRepository, never()).delete(any());
    }

    @Test
    void softDeleteApplication_ShouldThrowResourceNotFoundException_WhenApplicationNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        when(tenantApplicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                tenantApplicationService.softDeleteApplication(applicationId, userId));
    }

    @Test
    void submitApplication_ShouldCreateAndReturnDto_WhenValid() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        TenantRentalProfile profile = TenantRentalProfile.builder()
                .profileId(profileId)
                .userId(userId)
                .title("My Profile")
                .monthlyIncome(BigDecimal.valueOf(5000))
                .build();

        Listing listing = Listing.builder()
                .listingId(listingId)
                .name("Luxury Apartment")
                .build();

        TenantApplicationDto expectedDto = TenantApplicationDto.builder()
                .title("Luxury Apartment - My Profile")
                .build();

        when(tenantRentalProfileRepository.findByProfileIdAndUserIdAndDeletedFalse(profileId, userId))
                .thenReturn(Optional.of(profile));
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(tenantApplicationRepository.save(any(TenantApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tenantApplicationMapper.toDto(any(TenantApplication.class))).thenReturn(expectedDto);

        // Act
        TenantApplicationDto result = tenantApplicationService.submitApplication(listingId, profileId, userId);

        // Assert
        assertNotNull(result);
        assertEquals("Luxury Apartment - My Profile", result.getTitle());
        verify(tenantApplicationRepository).save(any(TenantApplication.class));
    }
}
