package com.sep.realvista.unit.application.listing.service;

import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.application.listing.dto.SimilarListingDTO;
import com.sep.realvista.application.listing.dto.SimilarListingsResponse;
import com.sep.realvista.application.listing.service.ListingApplicationService;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.listing.similarity.SimilarListing;
import com.sep.realvista.domain.property.attribute.AttributeDataType;
import com.sep.realvista.domain.property.attribute.PropertyAttribute;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.infrastructure.persistence.property.attribute.PropertyAttributeValueJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for similar listings feature in ListingApplicationService.
 * <p>
 * Tests the business logic for getSimilarListings method in isolation
 * with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ListingApplicationService Similar Listings Unit Tests")
class ListingApplicationServiceSimilarUnitTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private PropertyAttributeValueJpaRepository propertyAttributeValueJpaRepository;

    @InjectMocks
    private ListingApplicationService listingApplicationService;

    private UUID listingId;
    private List<SimilarListing> mockSimilarListings;
    private List<PropertyAttributeValue> mockAttributes;

    @BeforeEach
    void setUp() {
        listingId = UUID.randomUUID();
        UUID propertyId1 = UUID.randomUUID();
        UUID propertyId2 = UUID.randomUUID();

        // Create mock similar listings
        SimilarListing similarListing1 = SimilarListing.builder()
                .listingId(UUID.randomUUID())
                .propertyId(propertyId1)
                .propertyTypeId(UUID.randomUUID())
                .locationId(UUID.randomUUID())
                .name("Luxury Apartment")
                .slug("luxury-apartment")
                .listingType(ListingType.RENT)
                .status(com.sep.realvista.domain.listing.ListingStatus.PUBLISHED)
                .price(new BigDecimal("2800.00"))
                .area(new BigDecimal("90.00"))
                .locationName("District 1")
                .propertyTypeName("Apartment")
                .thumbnailUrl("https://example.com/thumb1.jpg")
                .publishedAt(LocalDateTime.now())
                .similarityScore(0.85)
                .build();

        SimilarListing similarListing2 = SimilarListing.builder()
                .listingId(UUID.randomUUID())
                .propertyId(propertyId2)
                .propertyTypeId(UUID.randomUUID())
                .locationId(UUID.randomUUID())
                .name("Modern Studio")
                .slug("modern-studio")
                .listingType(ListingType.RENT)
                .status(com.sep.realvista.domain.listing.ListingStatus.PUBLISHED)
                .price(new BigDecimal("2600.00"))
                .area(new BigDecimal("80.00"))
                .locationName("Binh Thanh")
                .propertyTypeName("Studio")
                .thumbnailUrl("https://example.com/thumb2.jpg")
                .publishedAt(LocalDateTime.now())
                .similarityScore(0.78)
                .build();

        mockSimilarListings = List.of(similarListing1, similarListing2);

        // Create mock property attributes
        PropertyAttribute bedroomsAttr = PropertyAttribute.builder()
                .propertyAttributeId(UUID.randomUUID())
                .code("bedrooms")
                .name("Bedrooms")
                .dataType(AttributeDataType.NUMBER)
                .unit("room")
                .icon("bed")
                .build();

        PropertyAttribute bathroomsAttr = PropertyAttribute.builder()
                .propertyAttributeId(UUID.randomUUID())
                .code("bathrooms")
                .name("Bathrooms")
                .dataType(AttributeDataType.NUMBER)
                .unit("room")
                .icon("bath")
                .build();

        PropertyAttributeValue attributeValue1 = PropertyAttributeValue.builder()
                .propertyAttributeId(bedroomsAttr.getPropertyAttributeId())
                .propertyId(propertyId1)
                .propertyAttribute(bedroomsAttr)
                .valueNumber(new BigDecimal("3.0"))
                .build();

        PropertyAttributeValue attributeValue2 = PropertyAttributeValue.builder()
                .propertyAttributeId(bathroomsAttr.getPropertyAttributeId())
                .propertyId(propertyId1)
                .propertyAttribute(bathroomsAttr)
                .valueNumber(new BigDecimal("2.0"))
                .build();

        mockAttributes = List.of(attributeValue1, attributeValue2);
    }

    @Test
    @DisplayName("Should return similar listings when listing exists")
    void getSimilarListings_whenListingExists_shouldReturnSimilarListings() {
        // Arrange
        when(listingRepository.existsById(listingId)).thenReturn(true);
        when(listingRepository.findSimilarListings(listingId, 5)).thenReturn(mockSimilarListings);
        when(propertyAttributeValueJpaRepository.findRequiredAttributesByPropertyIds(any()))
                .thenReturn(mockAttributes);

        // Act
        SimilarListingsResponse response = listingApplicationService.getSimilarListings(listingId, 5);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getListings()).hasSize(2);
        assertThat(response.getTotal()).isEqualTo(2);
        assertThat(response.getLimit()).isEqualTo(5);

        // Verify first similar listing
        SimilarListingDTO firstListing = response.getListings().get(0);
        assertThat(firstListing.getListingId()).isNotNull();
        assertThat(firstListing.getName()).isEqualTo("Luxury Apartment");
        assertThat(firstListing.getListingType()).isEqualTo(ListingType.RENT);
        assertThat(firstListing.getPropertyTypeName()).isEqualTo("Apartment");
        assertThat(firstListing.getPrice()).isEqualTo(new BigDecimal("2800.00"));
        assertThat(firstListing.getSimilarityScore()).isEqualTo(85); // 0.85 * 100

        // Verify second similar listing
        SimilarListingDTO secondListing = response.getListings().get(1);
        assertThat(secondListing.getName()).isEqualTo("Modern Studio");
        assertThat(secondListing.getSimilarityScore()).isEqualTo(78); // 0.78 * 100

        verify(listingRepository).existsById(listingId);
        verify(listingRepository).findSimilarListings(listingId, 5);
        verify(propertyAttributeValueJpaRepository).findRequiredAttributesByPropertyIds(any());
    }

    @Test
    @DisplayName("Should return empty list when no similar listings found")
    void getSimilarListings_whenNoSimilarListings_shouldReturnEmptyList() {
        // Arrange
        when(listingRepository.existsById(listingId)).thenReturn(true);
        when(listingRepository.findSimilarListings(listingId, 5)).thenReturn(List.of());

        // Act
        SimilarListingsResponse response = listingApplicationService.getSimilarListings(listingId, 5);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getListings()).isEmpty();
        assertThat(response.getTotal()).isEqualTo(0);
        assertThat(response.getLimit()).isEqualTo(5);

        verify(listingRepository).existsById(listingId);
        verify(listingRepository).findSimilarListings(listingId, 5);
        // JPA repository not called when similar listings is empty (early return in fetchRequiredAttributes)
        verify(propertyAttributeValueJpaRepository, never()).findRequiredAttributesByPropertyIds(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when listing does not exist")
    void getSimilarListings_whenListingDoesNotExist_shouldThrowException() {
        // Arrange
        when(listingRepository.existsById(listingId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> listingApplicationService.getSimilarListings(listingId, 5))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Listing")
                .hasMessageContaining(listingId.toString());

        verify(listingRepository).existsById(listingId);
        verify(listingRepository, never()).findSimilarListings(any(), anyInt());
    }

    @Test
    @DisplayName("Should validate and adjust limit to minimum of 1")
    void getSimilarListings_withLimitBelow1_shouldAdjustTo1() {
        // Arrange
        when(listingRepository.existsById(listingId)).thenReturn(true);
        when(listingRepository.findSimilarListings(listingId, 1)).thenReturn(mockSimilarListings.subList(0, 1));
        when(propertyAttributeValueJpaRepository.findRequiredAttributesByPropertyIds(any()))
                .thenReturn(new ArrayList<>());

        // Act
        SimilarListingsResponse response = listingApplicationService.getSimilarListings(listingId, 0);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getLimit()).isEqualTo(1);

        verify(listingRepository).existsById(listingId);
        verify(listingRepository).findSimilarListings(listingId, 1);
    }

    @Test
    @DisplayName("Should validate and adjust limit to maximum of 10")
    void getSimilarListings_withLimitAbove10_shouldAdjustTo10() {
        // Arrange
        when(listingRepository.existsById(listingId)).thenReturn(true);
        when(listingRepository.findSimilarListings(listingId, 10)).thenReturn(mockSimilarListings);
        when(propertyAttributeValueJpaRepository.findRequiredAttributesByPropertyIds(any()))
                .thenReturn(mockAttributes);

        // Act
        SimilarListingsResponse response = listingApplicationService.getSimilarListings(listingId, 15);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getLimit()).isEqualTo(10);

        verify(listingRepository).existsById(listingId);
        verify(listingRepository).findSimilarListings(listingId, 10);
    }

    @Test
    @DisplayName("Should include property attributes in similar listing DTOs")
    void getSimilarListings_whenAttributesExist_shouldIncludeAttributesInDTOs() {
        // Arrange
        when(listingRepository.existsById(listingId)).thenReturn(true);
        when(listingRepository.findSimilarListings(listingId, 5)).thenReturn(mockSimilarListings);
        when(propertyAttributeValueJpaRepository.findRequiredAttributesByPropertyIds(any()))
                .thenReturn(mockAttributes);

        // Act
        SimilarListingsResponse response = listingApplicationService.getSimilarListings(listingId, 5);

        // Assert
        assertThat(response).isNotNull();
        SimilarListingDTO firstListing = response.getListings().get(0);
        assertThat(firstListing.getAttributes()).isNotEmpty();

        PropertyAttributeDTO firstAttribute = firstListing.getAttributes().get(0);
        assertThat(firstAttribute.getAttributeCode()).isIn("bedrooms", "bathrooms");
        assertThat(firstAttribute.getDataType()).isEqualTo("NUMBER");
        assertThat(firstAttribute.getUnit()).isEqualTo("room");
        assertThat(firstAttribute.getValueNumber()).isIn(new BigDecimal("3.0"), new BigDecimal("2.0"));

        verify(propertyAttributeValueJpaRepository).findRequiredAttributesByPropertyIds(any());
    }

    @Test
    @DisplayName("Should return empty attributes list when no attributes found")
    void getSimilarListings_whenNoAttributes_shouldReturnEmptyAttributesList() {
        // Arrange
        when(listingRepository.existsById(listingId)).thenReturn(true);
        when(listingRepository.findSimilarListings(listingId, 5)).thenReturn(mockSimilarListings);
        when(propertyAttributeValueJpaRepository.findRequiredAttributesByPropertyIds(any()))
                .thenReturn(new ArrayList<>());

        // Act
        SimilarListingsResponse response = listingApplicationService.getSimilarListings(listingId, 5);

        // Assert
        assertThat(response).isNotNull();
        SimilarListingDTO firstListing = response.getListings().get(0);
        assertThat(firstListing.getAttributes()).isNotNull().isEmpty();

        verify(propertyAttributeValueJpaRepository).findRequiredAttributesByPropertyIds(any());
    }

    @Test
    @DisplayName("Should correctly calculate similarity percentage from score")
    void getSimilarListings_shouldCalculateSimilarityPercentageCorrectly() {
        // Arrange
        SimilarListing listingWithScore = SimilarListing.builder()
                .listingId(UUID.randomUUID())
                .propertyId(UUID.randomUUID())
                .propertyTypeId(UUID.randomUUID())
                .locationId(UUID.randomUUID())
                .name("Test Listing")
                .slug("test-listing")
                .listingType(ListingType.RENT)
                .status(com.sep.realvista.domain.listing.ListingStatus.PUBLISHED)
                .price(new BigDecimal("2500.00"))
                .area(new BigDecimal("75.00"))
                .locationName("Test Location")
                .propertyTypeName("Test Type")
                .thumbnailUrl("https://example.com/thumb.jpg")
                .publishedAt(LocalDateTime.now())
                .similarityScore(0.92)
                .build();

        when(listingRepository.existsById(listingId)).thenReturn(true);
        when(listingRepository.findSimilarListings(listingId, 5)).thenReturn(List.of(listingWithScore));
        when(propertyAttributeValueJpaRepository.findRequiredAttributesByPropertyIds(any()))
                .thenReturn(new ArrayList<>());

        // Act
        SimilarListingsResponse response = listingApplicationService.getSimilarListings(listingId, 5);

        // Assert
        assertThat(response).isNotNull();
        SimilarListingDTO listingDTO = response.getListings().get(0);
        assertThat(listingDTO.getSimilarityScore()).isEqualTo(92); // 0.92 * 100 = 92%
    }
}
