package com.sep.realvista.component.presentation.rest.listing;

import com.sep.realvista.application.auth.service.TokenService;
import com.sep.realvista.application.listing.dto.ListingAnalyticsDTO;
import com.sep.realvista.application.listing.service.ListingAnalyticsService;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import com.sep.realvista.presentation.rest.listing.ListingAnalyticsController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Component tests for {@link ListingAnalyticsController}
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("ListingAnalyticsController Component Tests")
class ListingAnalyticsControllerComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ListingAnalyticsService listingAnalyticsService;

    @MockitoBean
    private ListingRepository listingRepository;

    @MockitoBean
    private PropertyRepository propertyRepository;

    private UUID listingId;
    private UUID ownerId;
    private UUID otherUserId;
    private Listing listing;
    private SecurityUserDetails ownerDetails;

    @BeforeEach
    void setUp() {
        listingId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();

        listing = Listing.builder()
                .listingId(listingId)
                .userId(ownerId)
                .propertyId(UUID.randomUUID())
                .listingType(ListingType.RENT)
                .status(ListingStatus.PUBLISHED)
                .name("Test Listing")
                .price(BigDecimal.valueOf(1000))
                .build();

        ownerDetails = SecurityUserDetails.builder()
                .userId(ownerId)
                .username("owner@example.com")
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/listings/{id}/analytics should return analytics for listing creator")
    @WithMockUser
    void getListingAnalytics_ListingCreator_ReturnsAnalytics() throws Exception {
        // Given
        ListingAnalyticsDTO analytics = ListingAnalyticsDTO.builder()
                .totalViews(150)
                .uniqueViewers(68)
                .tourBookings(5)
                .conversionRate(new BigDecimal("3.33"))
                .build();

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(listingAnalyticsService.getListingAnalytics(listingId)).thenReturn(analytics);

        // When & Then
        mockMvc.perform(get("/api/v1/listings/{listingId}/analytics", listingId)
                        .with(user(ownerDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total_views").value(150))
                .andExpect(jsonPath("$.data.unique_viewers").value(68))
                .andExpect(jsonPath("$.data.tour_bookings").value(5))
                .andExpect(jsonPath("$.data.conversion_rate").value(3.33));
    }

    @Test
    @DisplayName("GET /api/v1/listings/{id}/analytics should return analytics for property owner when not listing creator")
    @WithMockUser
    void getListingAnalytics_PropertyOwnerNotCreator_ReturnsAnalytics() throws Exception {
        UUID propertyId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        Listing agentListing = Listing.builder()
                .listingId(listingId)
                .userId(agentId)
                .propertyId(propertyId)
                .listingType(ListingType.RENT)
                .status(ListingStatus.PUBLISHED)
                .name("Agent Listing")
                .price(BigDecimal.valueOf(1000))
                .build();
        Property property = Property.builder()
                .propertyId(propertyId)
                .ownerId(ownerId)
                .locationId(UUID.randomUUID())
                .propertyTypeId(UUID.randomUUID())
                .streetAddress("1 Test St")
                .latitude(BigDecimal.ONE)
                .longitude(BigDecimal.ONE)
                .build();

        ListingAnalyticsDTO analytics = ListingAnalyticsDTO.builder()
                .totalViews(10)
                .uniqueViewers(4)
                .tourBookings(1)
                .conversionRate(new BigDecimal("10.00"))
                .build();

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(agentListing));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(listingAnalyticsService.getListingAnalytics(listingId)).thenReturn(analytics);

        mockMvc.perform(get("/api/v1/listings/{listingId}/analytics", listingId)
                        .with(user(ownerDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total_views").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/listings/{id}/analytics should return 403 for unrelated user")
    @WithMockUser
    void getListingAnalytics_UnrelatedUser_ReturnsForbidden() throws Exception {
        // Given
        SecurityUserDetails otherUserDetails = SecurityUserDetails.builder()
                .userId(otherUserId)
                .username("other@example.com")
                .build();

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(propertyRepository.findById(listing.getPropertyId())).thenReturn(Optional.of(
                Property.builder()
                        .propertyId(listing.getPropertyId())
                        .ownerId(ownerId)
                        .locationId(UUID.randomUUID())
                        .propertyTypeId(UUID.randomUUID())
                        .streetAddress("1 Test St")
                        .latitude(BigDecimal.ONE)
                        .longitude(BigDecimal.ONE)
                        .build()));

        // When & Then
        mockMvc.perform(get("/api/v1/listings/{listingId}/analytics", listingId)
                        .with(user(otherUserDetails)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/listings/{id}/analytics should return 404 for non-existent listing")
    @WithMockUser
    void getListingAnalytics_NonExistentListing_ReturnsNotFound() throws Exception {
        // Given
        UUID nonExistentListingId = UUID.randomUUID();
        when(listingRepository.findById(nonExistentListingId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/listings/{listingId}/analytics", nonExistentListingId)
                        .with(user(ownerDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/listings/{id}/analytics should return 403 for unauthenticated user")
    void getListingAnalytics_Unauthenticated_ReturnsForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/listings/{listingId}/analytics", listingId))
                .andExpect(status().isForbidden());
    }
}
