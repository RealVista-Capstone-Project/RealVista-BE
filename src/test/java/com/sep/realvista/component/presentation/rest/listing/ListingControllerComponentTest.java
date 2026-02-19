package com.sep.realvista.component.presentation.rest.listing;

import com.sep.realvista.application.auth.service.TokenService;
import com.sep.realvista.application.listing.dto.AgentInfoDTO;
import com.sep.realvista.application.listing.dto.AmenityDTO;
import com.sep.realvista.application.listing.dto.ListingDetailResponse;
import com.sep.realvista.application.listing.dto.LocationInfoDTO;
import com.sep.realvista.application.listing.dto.MediaDTO;
import com.sep.realvista.application.listing.dto.PriceChangeType;
import com.sep.realvista.application.listing.dto.PriceHistoryDTO;
import com.sep.realvista.application.listing.dto.PriceHistoryResponse;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.application.listing.dto.PropertyInfoDTO;
import com.sep.realvista.application.listing.dto.PropertyTypeInfoDTO;
import com.sep.realvista.application.listing.dto.SimilarListingDTO;
import com.sep.realvista.application.listing.dto.SimilarListingsResponse;
import com.sep.realvista.application.listing.service.ListingApplicationService;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.property.MediaType;
import com.sep.realvista.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.sep.realvista.presentation.exception.GlobalExceptionHandler;
import com.sep.realvista.presentation.rest.listing.ListingController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Component tests for ListingController.
 * <p>
 * Tests the web layer (controller) with Spring MVC infrastructure while mocking
 * the business layer (services).
 */
@WebMvcTest(controllers = ListingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("ListingController Component Tests (Web Layer)")
class ListingControllerComponentTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ListingApplicationService listingApplicationService;

        @MockitoBean
        private TokenService tokenService;

        @MockitoBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        private ListingDetailResponse mockListingResponse;
        private SimilarListingsResponse mockSimilarListingsResponse;
        private UUID listingId;

        @BeforeEach
        void setUp() {
                listingId = UUID.randomUUID();
                UUID propertyId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                // Prepare test media
                MediaDTO media1 = MediaDTO.builder()
                                .mediaId(UUID.randomUUID())
                                .mediaType(MediaType.IMAGE)
                                .mediaUrl("https://example.com/image1.jpg")
                                .thumbnailUrl("https://example.com/thumb1.jpg")
                                .isPrimary(true)
                                .displayOrder(1)
                                .build();

                MediaDTO media2 = MediaDTO.builder()
                                .mediaId(UUID.randomUUID())
                                .mediaType(MediaType.VIDEO)
                                .mediaUrl("https://example.com/video1.mp4")
                                .thumbnailUrl("https://example.com/video-thumb.jpg")
                                .isPrimary(false)
                                .displayOrder(2)
                                .build();

                // Prepare property info
                PropertyInfoDTO propertyInfo = PropertyInfoDTO.builder()
                                .propertyId(propertyId)
                                .streetAddress("123 Main St")
                                .landSizeM2(new BigDecimal("100.50"))
                                .usableSizeM2(new BigDecimal("85.00"))
                                .description("Beautiful property")
                                .bedrooms(3)
                                .bathrooms(2)
                                .areaSqft(new BigDecimal("915.00"))
                                .build();

                // Prepare location info
                LocationInfoDTO locationInfo = LocationInfoDTO.builder()
                                .locationId(UUID.randomUUID())
                                .cityName("Ho Chi Minh City")
                                .districtName("District 1")
                                .wardName("Ward 1")
                                .latitude(new BigDecimal("10.776389"))
                                .longitude(new BigDecimal("106.701944"))
                                .build();

                // Prepare property type info
                PropertyTypeInfoDTO propertyTypeInfo = PropertyTypeInfoDTO
                                .builder()
                                .propertyTypeId(UUID.randomUUID())
                                .propertyTypeName("Apartment")
                                .propertyTypeCode("APT")
                                .propertyCategoryId(UUID.randomUUID())
                                .propertyCategoryName("Residential")
                                .propertyCategoryCode("RES")
                                .build();

                // Prepare agent info
                AgentInfoDTO agentInfo = AgentInfoDTO.builder()
                                .userId(userId)
                                .firstName("John")
                                .lastName("Doe")
                                .fullName("John Doe")
                                .businessName("ABC Real Estate")
                                .email("john@example.com")
                                .phone("0123456789")
                                .avatarUrl("https://example.com/avatar.jpg")
                                .company("ABC Real Estate")
                                .isVerified(true)
                                .build();

                // Prepare amenities
                AmenityDTO amenity1 = AmenityDTO.builder()
                                .amenityId(UUID.randomUUID())
                                .amenityName("Swimming Pool")
                                .amenityType("ONSITE")
                                .description("Outdoor swimming pool")
                                .build();

                AmenityDTO amenity2 = AmenityDTO.builder()
                                .amenityId(UUID.randomUUID())
                                .amenityName("Gym")
                                .amenityType("ONSITE")
                                .description("Fitness center")
                                .build();

                AmenityDTO amenity3 = AmenityDTO.builder()
                                .amenityId(UUID.randomUUID())
                                .amenityName("Near MRT Station")
                                .amenityType("OFFSITE")
                                .description("Within 500m of MRT")
                                .build();

                // Prepare main listing response
                mockListingResponse = ListingDetailResponse.builder()
                                .listingId(listingId)
                                .propertyId(propertyId)
                                .userId(userId)
                                .listingType(ListingType.RENT)
                                .status(ListingStatus.PUBLISHED)
                                .slug("luxury-2-bedroom-apartment-nguyen-hue")
                                .name("Luxury 2-Bedroom Apartment in District 1")
                                .price(new BigDecimal("2700.00"))
                                .isNegotiable(false)
                                .property(propertyInfo)
                                .location(locationInfo)
                                .propertyType(propertyTypeInfo)
                                .media(List.of(media1, media2))
                                .agent(agentInfo)
                                .attributes(List.of())
                                .amenities(List.of(amenity1, amenity2, amenity3))
                                .totalPhotos(1)
                                .totalVideos(1)
                                .total3DTours(0)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                // Prepare similar listings response
                PropertyAttributeDTO attribute1 = PropertyAttributeDTO.builder()
                                .attributeId(UUID.randomUUID())
                                .attributeCode("bedrooms")
                                .attributeName("Bedrooms")
                                .dataType("NUMBER")
                                .unit("room")
                                .valueNumber(new BigDecimal("3.0"))
                                .build();

                PropertyAttributeDTO attribute2 = PropertyAttributeDTO.builder()
                                .attributeId(UUID.randomUUID())
                                .attributeCode("bathrooms")
                                .attributeName("Bathrooms")
                                .dataType("NUMBER")
                                .unit("room")
                                .valueNumber(new BigDecimal("2.0"))
                                .build();

                SimilarListingDTO similarListing1 = SimilarListingDTO.builder()
                                .listingId(UUID.randomUUID())
                                .slug("luxury-apartment-district-1")
                                .name("Luxury Apartment in District 1")
                                .listingType(ListingType.RENT)
                                .propertyTypeName("Apartment")
                                .price(new BigDecimal("2800.00"))
                                .area(new BigDecimal("90.00"))
                                .locationName("District 1, Ho Chi Minh City")
                                .thumbnailUrl("https://example.com/thumb1.jpg")
                                .similarityScore(85)
                                .publishedAt(LocalDateTime.now().minusDays(5))
                                .attributes(List.of(attribute1, attribute2))
                                .build();

                SimilarListingDTO similarListing2 = SimilarListingDTO.builder()
                                .listingId(UUID.randomUUID())
                                .slug("modern-studio-binh-thanh")
                                .name("Modern Studio in Binh Thanh")
                                .listingType(ListingType.RENT)
                                .propertyTypeName("Studio")
                                .price(new BigDecimal("2600.00"))
                                .area(new BigDecimal("80.00"))
                                .locationName("Binh Thanh District, Ho Chi Minh City")
                                .thumbnailUrl("https://example.com/thumb2.jpg")
                                .similarityScore(78)
                                .publishedAt(LocalDateTime.now().minusDays(3))
                                .attributes(List.of(attribute1))
                                .build();

                mockSimilarListingsResponse = SimilarListingsResponse.builder()
                                .listings(List.of(similarListing1, similarListing2))
                                .total(2)
                                .limit(5)
                                .build();
        }

        @Test
        @DisplayName("Should return 200 OK when getting listing detail with valid ID")
        void getListingDetail_withValidId_shouldReturnOk() throws Exception {
                // Arrange
                UUID listingId = mockListingResponse.getListingId();
                when(listingApplicationService.getListingDetail(any(UUID.class)))
                                .thenReturn(mockListingResponse);

                // Act & Assert
                mockMvc.perform(get("/api/v1/listings/{id}", listingId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Listing retrieved successfully"))
                                .andExpect(jsonPath("$.data.listing_id").value(listingId.toString()))
                                .andExpect(jsonPath("$.data.listing_type").value("RENT"))
                                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                                .andExpect(jsonPath("$.data.slug").value("luxury-2-bedroom-apartment-nguyen-hue"))
                                .andExpect(jsonPath("$.data.name").value("Luxury 2-Bedroom Apartment in District 1"))
                                .andExpect(jsonPath("$.data.price").value(2700.00))
                                .andExpect(jsonPath("$.data.property.street_address").value("123 Main St"))
                                .andExpect(jsonPath("$.data.property.bedrooms").value(3))
                                .andExpect(jsonPath("$.data.property.bathrooms").value(2))
                                .andExpect(jsonPath("$.data.location.city_name").value("Ho Chi Minh City"))
                                .andExpect(jsonPath("$.data.media").isArray())
                                .andExpect(jsonPath("$.data.media.length()").value(2))
                                .andExpect(jsonPath("$.data.attributes").isArray())
                                .andExpect(jsonPath("$.data.amenities").isArray())
                                .andExpect(jsonPath("$.data.amenities.length()").value(3))
                                .andExpect(jsonPath("$.data.amenities[0].amenity_name").value("Swimming Pool"))
                                .andExpect(jsonPath("$.data.amenities[0].amenity_type").value("ONSITE"))
                                .andExpect(jsonPath("$.data.amenities[2].amenity_type").value("OFFSITE"))
                                .andExpect(jsonPath("$.data.total_photos").value(1))
                                .andExpect(jsonPath("$.data.total_videos").value(1))
                                .andExpect(jsonPath("$.data.agent.full_name").value("John Doe"));
        }

        @Test
        @DisplayName("Should return 404 Not Found when listing does not exist")
        void getListingDetail_withNonExistentId_shouldReturnNotFound() throws Exception {
                // Arrange
                UUID nonExistentId = UUID.randomUUID();
                when(listingApplicationService.getListingDetail(any(UUID.class)))
                                .thenThrow(new com.sep.realvista.domain.common.exception.ResourceNotFoundException(
                                                "Listing", nonExistentId));

                // Act & Assert
                mockMvc.perform(get("/api/v1/listings/{id}", nonExistentId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").exists())
                                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        }

        // ==================== Price History Tests ====================

        @Test
        @DisplayName("Should return 200 OK when getting price history with valid ID")
        void getPriceHistory_withValidId_shouldReturnOk() throws Exception {
                // Arrange
                UUID listingId = UUID.randomUUID();

                PriceHistoryDTO history1 = PriceHistoryDTO.builder()
                                .priceHistoryId(UUID.randomUUID())
                                .price(new BigDecimal("1250000000.00"))
                                .changedAt(LocalDateTime.now().minusDays(15))
                                .priceChange(new BigDecimal("50000000.00"))
                                .priceChangePercent(4.17)
                                .changeType(PriceChangeType.INCREASED)
                                .build();

                PriceHistoryDTO history2 = PriceHistoryDTO.builder()
                                .priceHistoryId(UUID.randomUUID())
                                .price(new BigDecimal("1200000000.00"))
                                .changedAt(LocalDateTime.now().minusDays(60))
                                .priceChange(null)
                                .priceChangePercent(null)
                                .changeType(PriceChangeType.INITIAL)
                                .build();

                PriceHistoryResponse mockResponse = PriceHistoryResponse.builder()
                                .listingId(listingId)
                                .currentPrice(new BigDecimal("1250000000.00"))
                                .priceHistory(List.of(history1, history2))
                                .build();

                when(listingApplicationService.getPriceHistory(any(UUID.class)))
                                .thenReturn(mockResponse);

                // Act & Assert
                mockMvc.perform(get("/api/v1/listings/{id}/price-history", listingId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Price history retrieved successfully"))
                                .andExpect(jsonPath("$.data.listing_id").value(listingId.toString()))
                                .andExpect(jsonPath("$.data.current_price").value(1.25E9))
                                .andExpect(jsonPath("$.data.price_history").isArray())
                                .andExpect(jsonPath("$.data.price_history.length()").value(2))
                                .andExpect(jsonPath("$.data.price_history[0].price").value(1.25E9))
                                .andExpect(jsonPath("$.data.price_history[0].change_type").value("INCREASED"))
                                .andExpect(jsonPath("$.data.price_history[0].price_change").value(5.0E7))
                                .andExpect(jsonPath("$.data.price_history[0].price_change_percent").value(4.17))
                                .andExpect(jsonPath("$.data.price_history[1].change_type").value("INITIAL"));
        }

        @Test
        @DisplayName("Should return 404 Not Found when listing does not exist for price history")
        void getPriceHistory_withNonExistentId_shouldReturnNotFound() throws Exception {
                // Arrange
                UUID nonExistentId = UUID.randomUUID();
                when(listingApplicationService.getPriceHistory(any(UUID.class)))
                                .thenThrow(new com.sep.realvista.domain.common.exception.ResourceNotFoundException(
                                                "Listing", nonExistentId));

                // Act & Assert
                mockMvc.perform(get("/api/v1/listings/{id}/price-history", nonExistentId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").exists())
                                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        }

        @Test
        @DisplayName("Should return empty price history when no history exists")
        void getPriceHistory_withNoHistory_shouldReturnEmptyList() throws Exception {
                // Arrange
                UUID listingId = UUID.randomUUID();

                PriceHistoryResponse mockResponse = PriceHistoryResponse.builder()
                                .listingId(listingId)
                                .currentPrice(new BigDecimal("2700.00"))
                                .priceHistory(List.of())
                                .build();

                when(listingApplicationService.getPriceHistory(any(UUID.class)))
                                .thenReturn(mockResponse);

                // Act & Assert
                mockMvc.perform(get("/api/v1/listings/{id}/price-history", listingId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.listing_id").value(listingId.toString()))
                                .andExpect(jsonPath("$.data.current_price").value(2700.00))
                                .andExpect(jsonPath("$.data.price_history").isArray())
                                .andExpect(jsonPath("$.data.price_history.length()").value(0));
        }

        // ==================== Similar Listings Tests ====================

        @Test
        @DisplayName("Should return 200 OK when getting similar listings with valid ID")
        void getSimilarListings_withValidId_shouldReturnOk() throws Exception {
                // Arrange
                when(listingApplicationService.getSimilarListings(any(UUID.class), any(int.class)))
                                .thenReturn(mockSimilarListingsResponse);

                // Act & Assert
                mockMvc.perform(get("/api/v1/listings/{id}/similar", listingId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Similar listings retrieved successfully"))
                                .andExpect(jsonPath("$.data.total").value(2))
                                .andExpect(jsonPath("$.data.limit").value(5))
                                .andExpect(jsonPath("$.data.listings").isArray())
                                .andExpect(jsonPath("$.data.listings.length()").value(2))
                                .andExpect(jsonPath("$.data.listings[0].listing_type").value("RENT"))
                                .andExpect(jsonPath("$.data.listings[0].property_type_name").value("Apartment"))
                                .andExpect(jsonPath("$.data.listings[0].price").value(2800.00))
                                .andExpect(jsonPath("$.data.listings[0].similarity_score").value(85))
                                .andExpect(jsonPath("$.data.listings[0].attributes").isArray())
                                .andExpect(jsonPath("$.data.listings[0].attributes.length()").value(2))
                                .andExpect(jsonPath("$.data.listings[1].property_type_name").value("Studio"))
                                .andExpect(jsonPath("$.data.listings[1].similarity_score").value(78));
        }

        @Test
        @DisplayName("Should return 200 OK with default limit when limit parameter not provided")
        void getSimilarListings_withoutLimit_shouldUseDefaultLimit() throws Exception {
                // Arrange
                when(listingApplicationService.getSimilarListings(any(UUID.class), any(int.class)))
                                .thenReturn(mockSimilarListingsResponse);

                // Act & Assert
                mockMvc.perform(get("/api/v1/listings/{id}/similar", listingId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.total").value(2))
                                .andExpect(jsonPath("$.data.limit").value(5));
        }

        @Test
        @DisplayName("Should pass custom limit parameter to service")
        void getSimilarListings_withCustomLimit_shouldPassLimitToService() throws Exception {
                // Arrange
                SimilarListingsResponse responseWithLimit3 = SimilarListingsResponse.builder()
                                .listings(List.of(mockSimilarListingsResponse.getListings().get(0)))
                                .total(1)
                                .limit(3)
                                .build();

                when(listingApplicationService.getSimilarListings(any(UUID.class), any(int.class)))
                                .thenReturn(responseWithLimit3);

                // Act & Assert
                mockMvc.perform(get("/api/v1/listings/{id}/similar", listingId)
                                .param("limit", "3"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.limit").value(3));
        }

        @Test
        @DisplayName("Should return empty list when no similar listings found")
        void getSimilarListings_withNoSimilarListings_shouldReturnEmptyList() throws Exception {
                // Arrange
                SimilarListingsResponse emptyResponse = SimilarListingsResponse.builder()
                                .listings(List.of())
                                .total(0)
                                .limit(5)
                                .build();

                when(listingApplicationService.getSimilarListings(any(UUID.class), any(int.class)))
                                .thenReturn(emptyResponse);

                // Act & Assert
                mockMvc.perform(get("/api/v1/listings/{id}/similar", listingId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.total").value(0))
                                .andExpect(jsonPath("$.data.listings").isArray())
                                .andExpect(jsonPath("$.data.listings.length()").value(0));
        }

        @Test
        @DisplayName("Should return 404 Not Found when listing does not exist for similar listings")
        void getSimilarListings_withNonExistentId_shouldReturnNotFound() throws Exception {
                // Arrange
                UUID nonExistentId = UUID.randomUUID();
                when(listingApplicationService.getSimilarListings(any(UUID.class), any(int.class)))
                                .thenThrow(new com.sep.realvista.domain.common.exception.ResourceNotFoundException(
                                                "Listing", nonExistentId));

                // Act & Assert
                mockMvc.perform(get("/api/v1/listings/{id}/similar", nonExistentId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").exists())
                                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        }
}
