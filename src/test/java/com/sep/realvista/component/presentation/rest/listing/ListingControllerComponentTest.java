package com.sep.realvista.component.presentation.rest.listing;

import com.sep.realvista.application.auth.service.TokenService;
import com.sep.realvista.application.listing.dto.AgentInfoDTO;
import com.sep.realvista.application.listing.dto.ListingDetailResponse;
import com.sep.realvista.application.listing.dto.LocationInfoDTO;
import com.sep.realvista.application.listing.dto.MediaDTO;
import com.sep.realvista.application.listing.dto.PropertyInfoDTO;
import com.sep.realvista.application.listing.dto.PropertyTypeInfoDTO;
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

        @BeforeEach
        void setUp() {
                UUID listingId = UUID.randomUUID();
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
                                .totalPhotos(1)
                                .totalVideos(1)
                                .total3DTours(0)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
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
}
