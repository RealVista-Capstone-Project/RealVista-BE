package com.sep.realvista.unit.application.listing.service;

import com.sep.realvista.application.listing.dto.ListingResponse;
import com.sep.realvista.application.listing.dto.ListingDetailResponse;
import com.sep.realvista.application.listing.dto.PriceChangeType;
import com.sep.realvista.application.listing.dto.PriceHistoryResponse;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.application.listing.dto.SimilarListingDTO;
import com.sep.realvista.application.listing.dto.SimilarListingsResponse;
import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.application.listing.service.CostBreakdownService;
import com.sep.realvista.application.listing.service.ListingApplicationService;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.billing.subscription.repository.UserFeatureSubscriptionRepository;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementRepository;
import com.sep.realvista.domain.engagement.EngagementStatus;
import com.sep.realvista.domain.engagement.EngagementType;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingMedia;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.analytics.ListingPriceHistory;
import com.sep.realvista.domain.listing.repository.ListingMediaRepository;
import com.sep.realvista.domain.listing.repository.ListingPriceHistoryRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.listing.similarity.SimilarListing;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyStatus;
import com.sep.realvista.domain.property.amenity.Amenity;
import com.sep.realvista.domain.property.amenity.AmenityType;
import com.sep.realvista.domain.property.amenity.PropertyAmenity;
import com.sep.realvista.domain.property.attribute.AttributeDataType;
import com.sep.realvista.domain.property.attribute.PropertyAttribute;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.property.repository.PropertyAmenityRepository;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.domain.listing.bookmark.BookmarkRepository;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeValueRepository;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserStatus;
import com.sep.realvista.domain.user.preference.SettingPreference;
import com.sep.realvista.domain.user.preference.SettingPreferenceRepository;
import com.sep.realvista.application.listing.service.ListingAnalyticsService;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

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
        private ListingPriceHistoryRepository listingPriceHistoryRepository;

        @Mock
        private PropertyRepository propertyRepository;

        @Mock
        private PropertyAttributeValueRepository propertyAttributeValueRepository;

        @Mock
        private PropertyAmenityRepository propertyAmenityRepository;

        @Mock
        private ListingMapper listingMapper;

        @Mock
        private CostBreakdownService costBreakdownService;

        @Mock
        private BookmarkRepository bookmarkRepository;

        @Mock
        private SettingPreferenceRepository settingPreferenceRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private ListingAnalyticsService listingAnalyticsService;

        @Mock
        private NotificationApplicationService notificationApplicationService;

        @Mock
        private com.sep.realvista.infrastructure.service.NotificationMessageService notificationMessageService;

        @Mock
        private com.sep.realvista.application.appointment.service.AppointmentApplicationService appointmentApplicationService;

        @Mock
        private UserFeatureSubscriptionRepository userFeatureSubscriptionRepository;

        @Mock
        private EngagementRepository engagementRepository;

        @InjectMocks
        private ListingApplicationService listingApplicationService;

        private Listing testListing;
        private Property testProperty;
        private ListingMedia testMedia;
        private UUID listingId;
        private UUID propertyId;
        private UUID userId;
        private List<SimilarListing> mockSimilarListings;
        private List<PropertyAttributeValue> mockAttributes;
        private List<PropertyAmenity> mockAmenities;

        @BeforeEach
        void setUp() {
                setField(listingApplicationService, "self", listingApplicationService);

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
                                .slug("test-listing-slug")
                                .name("Test Listing Name")
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

                // Setup similar listings mock data
                UUID propertyId1 = UUID.randomUUID();
                UUID propertyId2 = UUID.randomUUID();

                SimilarListing similarListing1 = SimilarListing.builder()
                                .listingId(UUID.randomUUID())
                                .propertyId(propertyId1)
                                .propertyTypeId(UUID.randomUUID())
                                .locationId(UUID.randomUUID())
                                .name("Luxury Apartment")
                                .slug("luxury-apartment")
                                .listingType(ListingType.RENT)
                                .status(ListingStatus.PUBLISHED)
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
                                .status(ListingStatus.PUBLISHED)
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

                // Create mock amenities
                Amenity gymAmenity = Amenity.builder()
                                .amenityId(UUID.randomUUID())
                                .amenityName("Gym")
                                .amenityType(AmenityType.ONSITE)
                                .description("Fitness center")
                                .build();

                Amenity poolAmenity = Amenity.builder()
                                .amenityId(UUID.randomUUID())
                                .amenityName("Swimming Pool")
                                .amenityType(AmenityType.ONSITE)
                                .description("Outdoor swimming pool")
                                .build();

                Amenity nearMrtAmenity = Amenity.builder()
                                .amenityId(UUID.randomUUID())
                                .amenityName("Near MRT Station")
                                .amenityType(AmenityType.OFFSITE)
                                .description("Within 500m of MRT")
                                .build();

                PropertyAmenity propertyAmenity1 = PropertyAmenity.builder()
                                .propertyAmenityId(UUID.randomUUID())
                                .propertyId(propertyId)
                                .amenityId(gymAmenity.getAmenityId())
                                .amenity(gymAmenity)
                                .build();

                PropertyAmenity propertyAmenity2 = PropertyAmenity.builder()
                                .propertyAmenityId(UUID.randomUUID())
                                .propertyId(propertyId)
                                .amenityId(poolAmenity.getAmenityId())
                                .amenity(poolAmenity)
                                .build();

                PropertyAmenity propertyAmenity3 = PropertyAmenity.builder()
                                .propertyAmenityId(UUID.randomUUID())
                                .propertyId(propertyId)
                                .amenityId(nearMrtAmenity.getAmenityId())
                                .amenity(nearMrtAmenity)
                                .build();

                mockAmenities = List.of(propertyAmenity1, propertyAmenity2, propertyAmenity3);
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
                                .slug("test-listing-slug")
                                .name("Test Listing Name")
                                .price(new BigDecimal("2700.00"))
                                .build();

                when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
                when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(testProperty));
                when(listingMediaRepository.findByListingIdOrderByDisplayOrderAsc(listingId))
                                .thenReturn(List.of(testMedia));
                when(propertyAttributeValueRepository.findByPropertyIdWithAttribute(propertyId))
                                .thenReturn(new ArrayList<>());
                when(propertyAmenityRepository.findByPropertyIdWithAmenity(propertyId))
                                .thenReturn(new ArrayList<>());
                when(settingPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
                when(listingMapper.toDetailResponseWithMediaAttributesAndAmenities(
                                any(Listing.class), anyList(), anyList(), anyList(), any()))
                                .thenReturn(expectedResponse);
                when(costBreakdownService.calculateCostBreakdown(any(Listing.class))).thenReturn(null);

                // Act
                ListingDetailResponse actualResponse = listingApplicationService.getListingDetail(listingId, null, false);

                // Assert
                assertThat(actualResponse).isNotNull();
                assertThat(actualResponse.getListingId()).isEqualTo(listingId);
                assertThat(actualResponse.getListingType()).isEqualTo(ListingType.RENT);
                assertThat(actualResponse.getStatus()).isEqualTo(ListingStatus.PUBLISHED);

                verify(listingRepository).findById(listingId);
                verify(propertyRepository).findById(propertyId);
                verify(listingMediaRepository).findByListingIdOrderByDisplayOrderAsc(listingId);
                verify(propertyAttributeValueRepository).findByPropertyIdWithAttribute(propertyId);
                verify(propertyAmenityRepository).findByPropertyIdWithAmenity(propertyId);
                verify(settingPreferenceRepository).findByUserId(userId);
                verify(listingMapper).toDetailResponseWithMediaAttributesAndAmenities(
                                any(Listing.class), anyList(), anyList(), anyList(), any());
                verify(costBreakdownService).calculateCostBreakdown(any(Listing.class));
        }

        @Test
        @DisplayName("Should return draft listing detail when requester is active creator")
        void getListingDetail_whenDraftAndRequesterIsActiveCreator_shouldReturnDetail() {
                // Arrange
                ListingDetailResponse expectedResponse = ListingDetailResponse.builder()
                                .listingId(listingId)
                                .propertyId(propertyId)
                                .userId(userId)
                                .listingType(ListingType.RENT)
                                .status(ListingStatus.DRAFT)
                                .slug("test-listing-slug")
                                .name("Test Listing Name")
                                .price(new BigDecimal("2700.00"))
                                .build();

                when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
                when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(testProperty));
                when(listingMediaRepository.findByListingIdOrderByDisplayOrderAsc(listingId))
                                .thenReturn(List.of(testMedia));
                when(propertyAttributeValueRepository.findByPropertyIdWithAttribute(propertyId))
                                .thenReturn(new ArrayList<>());
                when(propertyAmenityRepository.findByPropertyIdWithAmenity(propertyId))
                                .thenReturn(new ArrayList<>());
                when(settingPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
                when(listingMapper.toDetailResponseWithMediaAttributesAndAmenities(
                                any(Listing.class), anyList(), anyList(), anyList(), any()))
                                .thenReturn(expectedResponse);
                when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder()
                                .userId(userId)
                                .status(UserStatus.ACTIVE)
                                .build()));
                when(bookmarkRepository.existsByUserIdAndListingId(userId, listingId)).thenReturn(false);
                when(costBreakdownService.calculateCostBreakdown(any(Listing.class))).thenReturn(null);

                // Act
                ListingDetailResponse actualResponse = listingApplicationService.getListingDetail(listingId, userId, false);

                // Assert
                assertThat(actualResponse).isNotNull();
                assertThat(actualResponse.getStatus()).isEqualTo(ListingStatus.DRAFT);
                verify(userRepository, atLeastOnce()).findById(userId);
                verify(bookmarkRepository).existsByUserIdAndListingId(userId, listingId);
        }

        @Test
        @DisplayName("Should hide draft listing detail when requester is banned creator")
        void getListingDetail_whenDraftAndRequesterIsBannedCreator_shouldThrowNotFound() {
                // Arrange
                ListingDetailResponse expectedResponse = ListingDetailResponse.builder()
                                .listingId(listingId)
                                .propertyId(propertyId)
                                .userId(userId)
                                .status(ListingStatus.DRAFT)
                                .build();

                when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
                when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(testProperty));
                when(listingMediaRepository.findByListingIdOrderByDisplayOrderAsc(listingId))
                                .thenReturn(Collections.emptyList());
                when(propertyAttributeValueRepository.findByPropertyIdWithAttribute(propertyId))
                                .thenReturn(Collections.emptyList());
                when(propertyAmenityRepository.findByPropertyIdWithAmenity(propertyId))
                                .thenReturn(Collections.emptyList());
                when(settingPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
                when(listingMapper.toDetailResponseWithMediaAttributesAndAmenities(any(), any(), any(), any(), any()))
                                .thenReturn(expectedResponse);
                when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder()
                                .userId(userId)
                                .status(UserStatus.BANNED)
                                .build()));
                when(costBreakdownService.calculateCostBreakdown(any(Listing.class))).thenReturn(null);

                // Act & Assert
                assertThatThrownBy(() -> listingApplicationService.getListingDetail(listingId, userId, true))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Listing")
                                .hasMessageContaining(listingId.toString());

                verify(bookmarkRepository, never()).existsByUserIdAndListingId(any(), any());
                verify(listingAnalyticsService, never()).recordView(any(), any());
        }

        @Test
        @DisplayName("Should hide draft listing detail when requester is not creator")
        void getListingDetail_whenDraftAndRequesterIsNotCreator_shouldThrowNotFound() {
                // Arrange
                UUID otherUserId = UUID.randomUUID();
                ListingDetailResponse expectedResponse = ListingDetailResponse.builder()
                                .listingId(listingId)
                                .propertyId(propertyId)
                                .userId(userId)
                                .status(ListingStatus.DRAFT)
                                .build();

                when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
                when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(testProperty));
                when(listingMediaRepository.findByListingIdOrderByDisplayOrderAsc(listingId))
                                .thenReturn(Collections.emptyList());
                when(propertyAttributeValueRepository.findByPropertyIdWithAttribute(propertyId))
                                .thenReturn(Collections.emptyList());
                when(propertyAmenityRepository.findByPropertyIdWithAmenity(propertyId))
                                .thenReturn(Collections.emptyList());
                when(settingPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
                when(listingMapper.toDetailResponseWithMediaAttributesAndAmenities(any(), any(), any(), any(), any()))
                                .thenReturn(expectedResponse);
                when(costBreakdownService.calculateCostBreakdown(any(Listing.class))).thenReturn(null);

                // Act & Assert
                assertThatThrownBy(() -> listingApplicationService.getListingDetail(listingId, otherUserId, false))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Listing")
                                .hasMessageContaining(listingId.toString());

                verify(userRepository, never()).findById(otherUserId);
                verify(bookmarkRepository, never()).existsByUserIdAndListingId(any(), any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when listing does not exist")
        void getListingDetail_whenListingDoesNotExist_shouldThrowException() {
                // Arrange
                UUID nonExistentId = UUID.randomUUID();
                when(listingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> listingApplicationService.getListingDetail(nonExistentId, null, false))
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
                assertThatThrownBy(() -> listingApplicationService.getListingDetail(listingId, null, false))
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
                                .userId(userId)
                                .status(ListingStatus.PUBLISHED)
                                .slug("test-listing-slug")
                                .name("Test Listing Name")
                                .media(List.of()) // Empty media list is fine for this test
                                .build();

                when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
                when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(testProperty));
                when(listingMediaRepository.findByListingIdOrderByDisplayOrderAsc(listingId))
                                .thenReturn(List.of(testMedia));
                when(propertyAttributeValueRepository.findByPropertyIdWithAttribute(propertyId))
                                .thenReturn(new ArrayList<>());
                when(propertyAmenityRepository.findByPropertyIdWithAmenity(propertyId))
                                .thenReturn(new ArrayList<>());
                when(settingPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
                when(listingMapper.toDetailResponseWithMediaAttributesAndAmenities(
                                any(Listing.class), anyList(), anyList(), anyList(), any()))
                                .thenReturn(expectedResponse);
                when(costBreakdownService.calculateCostBreakdown(any(Listing.class))).thenReturn(null);

                // Act
                ListingDetailResponse actualResponse = listingApplicationService.getListingDetail(listingId, null, false);

                // Assert
                assertThat(actualResponse).isNotNull();
                verify(listingMapper).toDetailResponseWithMediaAttributesAndAmenities(
                                any(Listing.class), anyList(), anyList(), anyList(), any());
                verify(costBreakdownService).calculateCostBreakdown(any(Listing.class));
        }

        @Test
        @DisplayName("Should fetch and include amenities when listing has amenities")
        void getListingDetail_whenAmenitiesExist_shouldIncludeAmenities() {
                // Arrange
                ListingDetailResponse expectedResponse = ListingDetailResponse.builder()
                                .listingId(listingId)
                                .userId(userId)
                                .status(ListingStatus.PUBLISHED)
                                .slug("test-listing-slug")
                                .name("Test Listing Name")
                                .build();

                when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
                when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(testProperty));
                when(listingMediaRepository.findByListingIdOrderByDisplayOrderAsc(listingId))
                                .thenReturn(List.of(testMedia));
                when(propertyAttributeValueRepository.findByPropertyIdWithAttribute(propertyId))
                                .thenReturn(new ArrayList<>());
                when(propertyAmenityRepository.findByPropertyIdWithAmenity(propertyId))
                                .thenReturn(mockAmenities);
                when(settingPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
                when(listingMapper.toDetailResponseWithMediaAttributesAndAmenities(
                                any(Listing.class), anyList(), anyList(), anyList(), any()))
                                .thenReturn(expectedResponse);
                when(costBreakdownService.calculateCostBreakdown(any(Listing.class))).thenReturn(null);

                // Act
                ListingDetailResponse actualResponse = listingApplicationService.getListingDetail(listingId, null, false);

                // Assert
                assertThat(actualResponse).isNotNull();
                verify(propertyAmenityRepository).findByPropertyIdWithAmenity(propertyId);
                verify(listingMapper).toDetailResponseWithMediaAttributesAndAmenities(
                                any(Listing.class), anyList(), anyList(), anyList(), any());
        }

        @Test
        @DisplayName("Should return empty amenities when property has no amenities")
        void getListingDetail_whenNoAmenities_shouldReturnEmptyAmenities() {
                // Arrange
                ListingDetailResponse expectedResponse = ListingDetailResponse.builder()
                                .listingId(listingId)
                                .userId(userId)
                                .status(ListingStatus.PUBLISHED)
                                .slug("test-listing-slug")
                                .name("Test Listing Name")
                                .build();

                when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
                when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(testProperty));
                when(listingMediaRepository.findByListingIdOrderByDisplayOrderAsc(listingId))
                                .thenReturn(List.of(testMedia));
                when(propertyAttributeValueRepository.findByPropertyIdWithAttribute(propertyId))
                                .thenReturn(new ArrayList<>());
                when(propertyAmenityRepository.findByPropertyIdWithAmenity(propertyId))
                                .thenReturn(new ArrayList<>());
                when(settingPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
                when(listingMapper.toDetailResponseWithMediaAttributesAndAmenities(
                                any(Listing.class), anyList(), anyList(), anyList(), any()))
                                .thenReturn(expectedResponse);
                when(costBreakdownService.calculateCostBreakdown(any(Listing.class))).thenReturn(null);

                // Act
                ListingDetailResponse actualResponse = listingApplicationService.getListingDetail(listingId, null, false);

                // Assert
                assertThat(actualResponse).isNotNull();
                verify(propertyAmenityRepository).findByPropertyIdWithAmenity(propertyId);
        }

        // ==================== Price History Tests ====================

        @Test
        @DisplayName("Should return price history when listing exists")
        void getPriceHistory_whenListingExists_shouldReturnHistory() {
                // Arrange
                ListingPriceHistory history1 = ListingPriceHistory.builder()
                                .listingPriceHistoryId(UUID.randomUUID())
                                .listingId(listingId)
                                .price(new BigDecimal("2800.00"))
                                .changedBy(userId)
                                .build();
                setField(history1, "createdAt", LocalDateTime.now().minusDays(1));

                ListingPriceHistory history2 = ListingPriceHistory.builder()
                                .listingPriceHistoryId(UUID.randomUUID())
                                .listingId(listingId)
                                .price(new BigDecimal("2700.00"))
                                .changedBy(userId)
                                .build();
                setField(history2, "createdAt", LocalDateTime.now().minusDays(30));

                when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
                when(listingPriceHistoryRepository.findByListingIdOrderByCreatedAtDesc(listingId))
                                .thenReturn(List.of(history1, history2));

                // Act
                PriceHistoryResponse response = listingApplicationService.getPriceHistory(listingId);

                // Assert
                assertThat(response).isNotNull();
                assertThat(response.getListingId()).isEqualTo(listingId);
                assertThat(response.getCurrentPrice()).isEqualTo(new BigDecimal("2700.00"));
                assertThat(response.getPriceHistory()).hasSize(2);

                verify(listingRepository).findById(listingId);
                verify(listingPriceHistoryRepository).findByListingIdOrderByCreatedAtDesc(listingId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when listing does not exist for price history")
        void getPriceHistory_whenListingDoesNotExist_shouldThrowException() {
                // Arrange
                UUID nonExistentId = UUID.randomUUID();
                when(listingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> listingApplicationService.getPriceHistory(nonExistentId))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Listing")
                                .hasMessageContaining(nonExistentId.toString());

                verify(listingRepository).findById(nonExistentId);
                verify(listingPriceHistoryRepository, never()).findByListingIdOrderByCreatedAtDesc(any());
        }

        @Test
        @DisplayName("Should return empty price history when no history exists")
        void getPriceHistory_whenNoHistoryExists_shouldReturnEmptyList() {
                // Arrange
                when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
                when(listingPriceHistoryRepository.findByListingIdOrderByCreatedAtDesc(listingId))
                                .thenReturn(Collections.emptyList());

                // Act
                PriceHistoryResponse response = listingApplicationService.getPriceHistory(listingId);

                // Assert
                assertThat(response).isNotNull();
                assertThat(response.getListingId()).isEqualTo(listingId);
                assertThat(response.getCurrentPrice()).isEqualTo(new BigDecimal("2700.00"));
                assertThat(response.getPriceHistory()).isEmpty();

                verify(listingRepository).findById(listingId);
                verify(listingPriceHistoryRepository).findByListingIdOrderByCreatedAtDesc(listingId);
        }

        @Test
        @DisplayName("Should calculate price change correctly for increased price")
        void getPriceHistory_whenPriceIncreased_shouldShowIncrease() {
                // Arrange
                ListingPriceHistory recentHistory = ListingPriceHistory.builder()
                                .listingPriceHistoryId(UUID.randomUUID())
                                .listingId(listingId)
                                .price(new BigDecimal("3000.00"))
                                .changedBy(userId)
                                .build();
                setField(recentHistory, "createdAt", LocalDateTime.now().minusDays(1));

                ListingPriceHistory oldHistory = ListingPriceHistory.builder()
                                .listingPriceHistoryId(UUID.randomUUID())
                                .listingId(listingId)
                                .price(new BigDecimal("2700.00"))
                                .changedBy(userId)
                                .build();
                setField(oldHistory, "createdAt", LocalDateTime.now().minusDays(30));

                when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
                when(listingPriceHistoryRepository.findByListingIdOrderByCreatedAtDesc(listingId))
                                .thenReturn(List.of(recentHistory, oldHistory));

                // Act
                PriceHistoryResponse response = listingApplicationService.getPriceHistory(listingId);

                // Assert
                assertThat(response.getPriceHistory()).hasSize(2);
                assertThat(response.getPriceHistory().get(0).getChangeType()).isEqualTo(PriceChangeType.INCREASED);
                assertThat(response.getPriceHistory().get(0).getPriceChange())
                                .isEqualByComparingTo(new BigDecimal("300.00"));
                assertThat(response.getPriceHistory().get(0).getPriceChangePercent()).isCloseTo(11.11,
                                org.assertj.core.data.Offset.offset(0.01));
                assertThat(response.getPriceHistory().get(1).getChangeType()).isEqualTo(PriceChangeType.INITIAL);
        }

        @Test
        @DisplayName("Should calculate price change correctly for decreased price")
        void getPriceHistory_whenPriceDecreased_shouldShowDecrease() {
                // Arrange
                ListingPriceHistory recentHistory = ListingPriceHistory.builder()
                                .listingPriceHistoryId(UUID.randomUUID())
                                .listingId(listingId)
                                .price(new BigDecimal("2500.00"))
                                .changedBy(userId)
                                .build();
                setField(recentHistory, "createdAt", LocalDateTime.now().minusDays(1));

                ListingPriceHistory oldHistory = ListingPriceHistory.builder()
                                .listingPriceHistoryId(UUID.randomUUID())
                                .listingId(listingId)
                                .price(new BigDecimal("2700.00"))
                                .changedBy(userId)
                                .build();
                setField(oldHistory, "createdAt", LocalDateTime.now().minusDays(30));

                when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
                when(listingPriceHistoryRepository.findByListingIdOrderByCreatedAtDesc(listingId))
                                .thenReturn(List.of(recentHistory, oldHistory));

                // Act
                PriceHistoryResponse response = listingApplicationService.getPriceHistory(listingId);

                // Assert
                assertThat(response.getPriceHistory()).hasSize(2);
                assertThat(response.getPriceHistory().get(0).getChangeType()).isEqualTo(PriceChangeType.DECREASED);
                assertThat(response.getPriceHistory().get(0).getPriceChange())
                                .isEqualByComparingTo(new BigDecimal("-200.00"));
                assertThat(response.getPriceHistory().get(0).getPriceChangePercent()).isCloseTo(-7.41,
                                org.assertj.core.data.Offset.offset(0.01));

        }
        // ==================== Similar Listings Tests ====================

        @Test
        @DisplayName("Should return similar listings when listing exists")
        void getSimilarListings_whenListingExists_shouldReturnSimilarListings() {
                // Arrange
                when(listingRepository.existsById(listingId)).thenReturn(true);
                when(listingRepository.findSimilarListings(listingId, 5)).thenReturn(mockSimilarListings);
                when(propertyAttributeValueRepository.findAllAttributesByPropertyIds(any()))
                                .thenReturn(mockAttributes);

                // Act
                SimilarListingsResponse response = listingApplicationService.getSimilarListings(listingId, 5, null);

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
                verify(propertyAttributeValueRepository).findAllAttributesByPropertyIds(any());
        }

        @Test
        @DisplayName("Should return empty list when no similar listings found")
        void getSimilarListings_whenNoSimilarListings_shouldReturnEmptyList() {
                // Arrange
                when(listingRepository.existsById(listingId)).thenReturn(true);
                when(listingRepository.findSimilarListings(listingId, 5)).thenReturn(List.of());

                // Act
                SimilarListingsResponse response = listingApplicationService.getSimilarListings(listingId, 5, null);

                // Assert
                assertThat(response).isNotNull();
                assertThat(response.getListings()).isEmpty();
                assertThat(response.getTotal()).isEqualTo(0);
                assertThat(response.getLimit()).isEqualTo(5);

                verify(listingRepository).existsById(listingId);
                verify(listingRepository).findSimilarListings(listingId, 5);
                // JPA repository not called when similar listings is empty (early return in
                // fetchRequiredAttributes)
                verify(propertyAttributeValueRepository, never()).findAllAttributesByPropertyIds(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when listing does not exist")
        void getSimilarListings_whenListingDoesNotExist_shouldThrowException() {
                // Arrange
                when(listingRepository.existsById(listingId)).thenReturn(false);

                // Act & Assert
                assertThatThrownBy(() -> listingApplicationService.getSimilarListings(listingId, 5, null))
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
                when(propertyAttributeValueRepository.findAllAttributesByPropertyIds(any()))
                                .thenReturn(new ArrayList<>());

                // Act
                SimilarListingsResponse response = listingApplicationService.getSimilarListings(listingId, 0, null);

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
                when(propertyAttributeValueRepository.findAllAttributesByPropertyIds(any()))
                                .thenReturn(mockAttributes);

                // Act
                SimilarListingsResponse response = listingApplicationService.getSimilarListings(listingId, 15, null);

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
                when(propertyAttributeValueRepository.findAllAttributesByPropertyIds(any()))
                                .thenReturn(mockAttributes);

                // Act
                SimilarListingsResponse response = listingApplicationService.getSimilarListings(listingId, 5, null);

                // Assert
                assertThat(response).isNotNull();
                SimilarListingDTO firstListing = response.getListings().get(0);
                assertThat(firstListing.getAttributes()).isNotEmpty();

                PropertyAttributeDTO firstAttribute = firstListing.getAttributes().get(0);
                assertThat(firstAttribute.getAttributeCode()).isIn("bedrooms", "bathrooms");
                assertThat(firstAttribute.getDataType()).isEqualTo("NUMBER");
                assertThat(firstAttribute.getUnit()).isEqualTo("room");
                assertThat(firstAttribute.getValueNumber()).isIn(new BigDecimal("3.0"), new BigDecimal("2.0"));

                verify(propertyAttributeValueRepository).findAllAttributesByPropertyIds(any());
        }

        @Test
        @DisplayName("Should return empty attributes list when no attributes found")
        void getSimilarListings_whenNoAttributes_shouldReturnEmptyAttributesList() {
                // Arrange
                when(listingRepository.existsById(listingId)).thenReturn(true);
                when(listingRepository.findSimilarListings(listingId, 5)).thenReturn(mockSimilarListings);
                when(propertyAttributeValueRepository.findAllAttributesByPropertyIds(any()))
                                .thenReturn(new ArrayList<>());

                // Act
                SimilarListingsResponse response = listingApplicationService.getSimilarListings(listingId, 5, null);

                // Assert
                assertThat(response).isNotNull();
                SimilarListingDTO firstListing = response.getListings().get(0);
                assertThat(firstListing.getAttributes()).isNotNull().isEmpty();

                verify(propertyAttributeValueRepository).findAllAttributesByPropertyIds(any());
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
                                .status(ListingStatus.PUBLISHED)
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
                when(propertyAttributeValueRepository.findAllAttributesByPropertyIds(any()))
                                .thenReturn(new ArrayList<>());

                // Act
                SimilarListingsResponse response = listingApplicationService.getSimilarListings(listingId, 5, null);

                // Assert
                assertThat(response).isNotNull();
                SimilarListingDTO listingDTO = response.getListings().get(0);
                assertThat(listingDTO.getSimilarityScore()).isEqualTo(92); // 0.92 * 100 = 92%
        }

        @Test
        @DisplayName("Should fetch and pass preference to mapper when getting listing detail")
        void getListingDetail_shouldPassPreferenceToMapper() {
                // Arrange
                SettingPreference preference = SettingPreference.builder()
                                .userId(userId)
                                .hidePhoneNumber(false)
                                .build();

                when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
                when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(testProperty));
                when(listingMediaRepository.findByListingIdOrderByDisplayOrderAsc(listingId))
                                .thenReturn(Collections.emptyList());
                when(propertyAttributeValueRepository.findByPropertyIdWithAttribute(propertyId))
                                .thenReturn(Collections.emptyList());
                when(propertyAmenityRepository.findByPropertyIdWithAmenity(propertyId))
                                .thenReturn(Collections.emptyList());
                when(settingPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));
                ListingDetailResponse response = ListingDetailResponse.builder()
                                .listingId(listingId)
                                .userId(userId)
                                .status(ListingStatus.PUBLISHED)
                                .build();
                when(listingMapper.toDetailResponseWithMediaAttributesAndAmenities(any(), any(), any(), any(), any()))
                                .thenReturn(response);

                // Act
                listingApplicationService.getListingDetail(listingId, null, false);

                // Assert
                verify(settingPreferenceRepository).findByUserId(userId);
                verify(listingMapper).toDetailResponseWithMediaAttributesAndAmenities(
                                eq(testListing), anyList(), anyList(), anyList(), eq(preference));
        }

        @Test
        @DisplayName("Should record view when recordView is true and userId is not null")
        void getListingDetail_whenRecordViewTrue_shouldRecordView() {
                // Arrange
                when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
                when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(testProperty));
                when(listingMediaRepository.findByListingIdOrderByDisplayOrderAsc(listingId))
                                .thenReturn(Collections.emptyList());
                when(propertyAttributeValueRepository.findByPropertyIdWithAttribute(propertyId))
                                .thenReturn(Collections.emptyList());
                when(propertyAmenityRepository.findByPropertyIdWithAmenity(propertyId))
                                .thenReturn(Collections.emptyList());
                when(bookmarkRepository.existsByUserIdAndListingId(userId, listingId)).thenReturn(false);
                when(listingMapper.toDetailResponseWithMediaAttributesAndAmenities(any(), any(), any(), any(), any()))
                                .thenReturn(ListingDetailResponse.builder()
                                                .listingId(listingId)
                                                .userId(userId)
                                                .status(ListingStatus.PUBLISHED)
                                                .build());

                // Act
                listingApplicationService.getListingDetail(listingId, userId, true);

                // Assert
                verify(listingAnalyticsService).recordView(listingId, userId);
        }

        @Test
        @DisplayName("Should NOT record view when recordView is false even if userId is not null")
        void getListingDetail_whenRecordViewFalse_shouldNotRecordView() {
                // Arrange
                when(listingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
                when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(testProperty));
                when(listingMediaRepository.findByListingIdOrderByDisplayOrderAsc(listingId))
                                .thenReturn(Collections.emptyList());
                when(propertyAttributeValueRepository.findByPropertyIdWithAttribute(propertyId))
                                .thenReturn(Collections.emptyList());
                when(propertyAmenityRepository.findByPropertyIdWithAmenity(propertyId))
                                .thenReturn(Collections.emptyList());
                when(bookmarkRepository.existsByUserIdAndListingId(userId, listingId)).thenReturn(false);
                when(listingMapper.toDetailResponseWithMediaAttributesAndAmenities(any(), any(), any(), any(), any()))
                                .thenReturn(ListingDetailResponse.builder()
                                                .listingId(listingId)
                                                .userId(userId)
                                                .status(ListingStatus.PUBLISHED)
                                                .build());

                // Act
                listingApplicationService.getListingDetail(listingId, userId, false);

                // Assert
                verify(listingAnalyticsService, never()).recordView(listingId, userId);
        }

        @Test
        @DisplayName("markAsSold: listing creator closes — ACCEPTED AGENT_PROPOSAL gets listing_id")
        void markAsSold_whenActorIsListingCreator_shouldSyncEngagementListingId() {
                UUID agentId = UUID.randomUUID();
                UUID ownerId = UUID.randomUUID();
                Listing saleListing = Listing.builder()
                                .listingId(listingId)
                                .propertyId(propertyId)
                                .userId(agentId)
                                .listingType(ListingType.SALE)
                                .status(ListingStatus.PUBLISHED)
                                .slug("sale-slug")
                                .name("Sale Listing")
                                .price(new BigDecimal("100000"))
                                .isNegotiable(false)
                                .build();

                Property property = Property.builder()
                                .propertyId(propertyId)
                                .ownerId(ownerId)
                                .streetAddress("1 Agent St")
                                .latitude(new BigDecimal("10.0"))
                                .longitude(new BigDecimal("106.0"))
                                .landSizeM2(BigDecimal.ONE)
                                .usableSizeM2(BigDecimal.ONE)
                                .descriptions("desc")
                                .status(PropertyStatus.AVAILABLE)
                                .build();

                Engagement engagement = Engagement.builder()
                                .engagementId(UUID.randomUUID())
                                .initiatorId(agentId)
                                .receiverId(ownerId)
                                .engagementType(EngagementType.AGENT_PROPOSAL)
                                .propertyId(propertyId)
                                .status(EngagementStatus.ACCEPTED)
                                .build();

                when(listingRepository.findById(listingId)).thenReturn(Optional.of(saleListing));
                when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
                when(listingRepository.findByPropertyId(propertyId)).thenReturn(Collections.emptyList());
                when(engagementRepository.findByListingIdInOrPropertyIdIn(anyList(), anyList()))
                                .thenReturn(List.of(engagement));
                when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(listingMapper.toListingResponse(any(Listing.class)))
                                .thenAnswer(invocation -> {
                                        Listing l = invocation.getArgument(0);
                                        return ListingResponse.builder()
                                                        .listingId(l.getListingId())
                                                        .propertyId(l.getPropertyId())
                                                        .userId(l.getUserId())
                                                        .listingType(l.getListingType())
                                                        .status(l.getStatus())
                                                        .build();
                                });

                listingApplicationService.markAsSold(listingId, agentId);

                assertThat(engagement.getListingId()).isEqualTo(listingId);
                verify(engagementRepository).save(engagement);
        }

        @Test
        @DisplayName("markAsSold: property owner closes agent listing — engagement listing_id unchanged")
        void markAsSold_whenActorIsOwnerOnly_shouldNotSyncEngagementListingId() {
                UUID agentId = UUID.randomUUID();
                UUID ownerId = UUID.randomUUID();
                Listing saleListing = Listing.builder()
                                .listingId(listingId)
                                .propertyId(propertyId)
                                .userId(agentId)
                                .listingType(ListingType.SALE)
                                .status(ListingStatus.PUBLISHED)
                                .slug("sale-slug")
                                .name("Sale Listing")
                                .price(new BigDecimal("100000"))
                                .isNegotiable(false)
                                .build();

                Property property = Property.builder()
                                .propertyId(propertyId)
                                .ownerId(ownerId)
                                .streetAddress("1 Agent St")
                                .latitude(new BigDecimal("10.0"))
                                .longitude(new BigDecimal("106.0"))
                                .landSizeM2(BigDecimal.ONE)
                                .usableSizeM2(BigDecimal.ONE)
                                .descriptions("desc")
                                .status(PropertyStatus.AVAILABLE)
                                .build();

                Engagement engagement = Engagement.builder()
                                .engagementId(UUID.randomUUID())
                                .initiatorId(agentId)
                                .receiverId(ownerId)
                                .engagementType(EngagementType.AGENT_PROPOSAL)
                                .propertyId(propertyId)
                                .status(EngagementStatus.ACCEPTED)
                                .build();

                when(listingRepository.findById(listingId)).thenReturn(Optional.of(saleListing));
                when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
                when(listingRepository.findByPropertyId(propertyId)).thenReturn(Collections.emptyList());
                when(engagementRepository.findByListingIdInOrPropertyIdIn(anyList(), anyList()))
                                .thenReturn(List.of(engagement));
                when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(listingMapper.toListingResponse(any(Listing.class)))
                                .thenAnswer(invocation -> {
                                        Listing l = invocation.getArgument(0);
                                        return ListingResponse.builder()
                                                        .listingId(l.getListingId())
                                                        .propertyId(l.getPropertyId())
                                                        .userId(l.getUserId())
                                                        .listingType(l.getListingType())
                                                        .status(l.getStatus())
                                                        .build();
                                });

                listingApplicationService.markAsSold(listingId, ownerId);

                assertThat(engagement.getListingId()).isNull();
                verify(engagementRepository, never()).save(any(Engagement.class));
        }
}
