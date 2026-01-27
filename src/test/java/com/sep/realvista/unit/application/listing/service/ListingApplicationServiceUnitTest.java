package com.sep.realvista.unit.application.listing.service;

import com.sep.realvista.application.listing.dto.ListingDetailResponse;
import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.application.listing.service.ListingApplicationService;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingMedia;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.repository.ListingMediaRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ListingApplicationService.
 * <p>
 * Tests the business logic layer in isolation with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ListingApplicationService Unit Tests")
class ListingApplicationServiceUnitTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ListingMediaRepository listingMediaRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private ListingMapper listingMapper;

    @InjectMocks
    private ListingApplicationService listingApplicationService;

    private Listing testListing;
    private Property testProperty;
    private ListingMedia testMedia;
    private UUID listingId;
    private UUID propertyId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        listingId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // Create test property
        testProperty = Property.builder()
                .propertyId(propertyId)
                .ownerId(userId)
                .streetAddress("123 Main St")
                .latitude(new BigDecimal("10.776389"))
                .longitude(new BigDecimal("106.701944"))
                .landSizeM2(new BigDecimal("100.50"))
                .usableSizeM2(new BigDecimal("85.00"))
                .descriptions("Beautiful property")
                .build();

        // Create test listing
        testListing = Listing.builder()
                .listingId(listingId)
                .propertyId(propertyId)
                .userId(userId)
                .listingType(ListingType.RENT)
                .status(ListingStatus.PUBLISHED)
                .price(new BigDecimal("2700.00"))
                .isNegotiable(false)
                .build();

        // Create test media
        testMedia = ListingMedia.builder()
                .listingMediaId(UUID.randomUUID())
                .listingId(listingId)
                .propertyMediaId(UUID.randomUUID())
                .displayOrder(1)
                .isPrimary(true)
                .build();
    }

    @Test
    @DisplayName("Should return listing detail when listing exists")
    void getListingDetail_whenListingExists_shouldReturnDetail() {
        // Arrange
        ListingDetailResponse expectedResponse = ListingDetailResponse.builder()
                .listingId(listingId)
                .propertyId(propertyId)
                .userId(userId)
                .listingType(ListingType.RENT)
                .status(ListingStatus.PUBLISHED)
                .price(new BigDecimal("2700.00"))
                .build();

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(testProperty));
        when(listingMediaRepository.findByListingIdOrderByDisplayOrderAsc(listingId))
                .thenReturn(List.of(testMedia));
        when(listingMapper.toDetailResponseWithMedia(any(Listing.class), anyList())).thenReturn(expectedResponse);

        // Act
        ListingDetailResponse actualResponse = listingApplicationService.getListingDetail(listingId);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getListingId()).isEqualTo(listingId);
        assertThat(actualResponse.getListingType()).isEqualTo(ListingType.RENT);
        assertThat(actualResponse.getStatus()).isEqualTo(ListingStatus.PUBLISHED);

        verify(listingRepository).findById(listingId);
        verify(propertyRepository).findById(propertyId);
        verify(listingMediaRepository).findByListingIdOrderByDisplayOrderAsc(listingId);
        verify(listingMapper).toDetailResponseWithMedia(any(Listing.class), anyList());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when listing does not exist")
    void getListingDetail_whenListingDoesNotExist_shouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(listingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> listingApplicationService.getListingDetail(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Listing")
                .hasMessageContaining(nonExistentId.toString());

        verify(listingRepository).findById(nonExistentId);
        verify(propertyRepository, never()).findById(any());
        verify(listingMapper, never()).toDetailResponse(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when property does not exist")
    void getListingDetail_whenPropertyDoesNotExist_shouldThrowException() {
        // Arrange
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> listingApplicationService.getListingDetail(listingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Property")
                .hasMessageContaining(propertyId.toString());

        verify(listingRepository).findById(listingId);
        verify(propertyRepository).findById(propertyId);
        verify(listingMediaRepository, never()).findByListingIdOrderByDisplayOrderAsc(any());
        verify(listingMapper, never()).toDetailResponse(any());
    }

    @Test
    @DisplayName("Should pass media list to mapper when media exists")
    void getListingDetail_whenMediaExists_shouldPassMediaToMapper() {
        // Arrange
        ListingDetailResponse expectedResponse = ListingDetailResponse.builder()
                .listingId(listingId)
                .media(List.of()) // Empty media list is fine for this test
                .build();

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(testProperty));
        when(listingMediaRepository.findByListingIdOrderByDisplayOrderAsc(listingId))
                .thenReturn(List.of(testMedia));
        when(listingMapper.toDetailResponseWithMedia(any(Listing.class), anyList())).thenReturn(expectedResponse);

        // Act
        ListingDetailResponse actualResponse = listingApplicationService.getListingDetail(listingId);

        // Assert
        assertThat(actualResponse).isNotNull();
        verify(listingMapper).toDetailResponseWithMedia(any(Listing.class), anyList());
    }
}
