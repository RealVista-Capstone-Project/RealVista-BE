package com.sep.realvista.component.presentation.rest.listing;

import com.sep.realvista.application.auth.service.TokenService;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.application.listing.dto.SimilarListingDTO;
import com.sep.realvista.application.listing.dto.SimilarListingsResponse;
import com.sep.realvista.application.listing.service.ListingApplicationService;
import com.sep.realvista.domain.listing.ListingType;
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
 * Component tests for similar listings endpoint in ListingController.
 * <p>
 * Tests the web layer (controller) for GET /api/v1/listings/{id}/similar
 * while mocking the business layer (services).
 */
@WebMvcTest(controllers = ListingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("ListingController Similar Listings Component Tests")
class ListingControllerSimilarComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListingApplicationService listingApplicationService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private SimilarListingsResponse mockResponse;
    private UUID listingId;

    @BeforeEach
    void setUp() {
        listingId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();

        // Create mock attribute DTOs
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

        // Create mock similar listing DTOs
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

        mockResponse = SimilarListingsResponse.builder()
                .listings(List.of(similarListing1, similarListing2))
                .total(2)
                .limit(5)
                .build();
    }

    @Test
    @DisplayName("Should return 200 OK when getting similar listings with valid ID")
    void getSimilarListings_withValidId_shouldReturnOk() throws Exception {
        // Arrange
        when(listingApplicationService.getSimilarListings(any(UUID.class), any(int.class)))
                .thenReturn(mockResponse);

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
                .thenReturn(mockResponse);

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
                .listings(List.of(mockResponse.getListings().get(0)))
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
    @DisplayName("Should return 404 Not Found when listing does not exist")
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
