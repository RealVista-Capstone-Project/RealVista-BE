package com.sep.realvista.component.presentation.rest.map;

import com.sep.realvista.application.auth.service.TokenService;
import com.sep.realvista.application.listing.dto.map.MapSearchRequest;
import com.sep.realvista.application.listing.dto.map.MapSearchResponse;
import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.application.listing.dto.map.PropertyMapMarker;
import com.sep.realvista.application.listing.service.MapSearchApplicationService;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.sep.realvista.presentation.exception.GlobalExceptionHandler;
import com.sep.realvista.presentation.rest.map.MapController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Component tests for MapController.
 * <p>
 * Tests the web layer (controller + validation + exception handling)
 * with Spring MVC infrastructure while mocking the business layer.
 */
@WebMvcTest(controllers = MapController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("MapController Component Tests (Web Layer)")
class MapControllerComponentTest {

        private static final String MAP_SEARCH_URL = "/api/v1/map/listings";

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private MapSearchApplicationService mapSearchService;

        @MockitoBean
        private TokenService tokenService;

        @MockitoBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        // Mocks for GlobalExceptionHandler
        @MockitoBean
        private com.sep.realvista.infrastructure.service.NotificationMessageService notificationMessageService;

        @MockitoBean
        private com.sep.realvista.domain.user.preference.SettingPreferenceRepository settingPreferenceRepository;

        // -- Shared test data --

        private ListingSearchResponse sampleMarker1;
        private ListingSearchResponse sampleMarker2;
        private MapSearchResponse singleResultResponse;
        private MapSearchResponse multiResultResponse;
        private MapSearchResponse emptyResponse;

        @BeforeEach
        void setUp() {
                // Mock GlobalExceptionHandler messages
                when(notificationMessageService.getMessage(anyString(), any()))
                                .thenReturn("Mocked notification message");
                when(notificationMessageService.getMessage(anyString(), any(), any()))
                                .thenReturn("Mocked notification message with args");

                sampleMarker1 = ListingSearchResponse.builder()
                                .listingId(UUID.randomUUID())
                                .coordinates(com.sep.realvista.application.listing.dto.map.PropertyMapMarker.CoordinatesDTO.builder()
                                                .latitude(new BigDecimal("10.776"))
                                                .longitude(new BigDecimal("106.687"))
                                                .build())
                                .streetAddress("101 Ben Nghe, Q1")
                                .price(new BigDecimal("1258552"))
                                .listingType(ListingType.SALE)
                                .name("2BR Apartment - Ben Nghe Ward")
                                .thumbnail("https://example.com/thumb1.jpg")
                                .bedrooms(2)
                                .bathrooms(1)
                                .area(80.00)
                                .propertyTypeName("Apartment")
                                .wardName("Ben Nghe Ward")
                                .isFavorite(false)
                                .build();

                sampleMarker2 = ListingSearchResponse.builder()
                                .listingId(UUID.randomUUID())
                                .coordinates(com.sep.realvista.application.listing.dto.map.PropertyMapMarker.CoordinatesDTO.builder()
                                                .latitude(new BigDecimal("10.800"))
                                                .longitude(new BigDecimal("106.700"))
                                                .build())
                                .streetAddress("55 Nguyen Hue, Q1")
                                .price(new BigDecimal("3500000"))
                                .listingType(ListingType.RENT)
                                .name("Studio near Nguyen Hue Walking Street")
                                .thumbnail("https://example.com/thumb2.jpg")
                                .bedrooms(0)
                                .bathrooms(1)
                                .area(35.00)
                                .propertyTypeName("Studio")
                                .wardName("Ben Nghe Ward")
                                .isFavorite(true)
                                .build();

                singleResultResponse = createMapSearchResponse(
                        List.of(sampleMarker1), 1L,
                        new BigDecimal("10.85"), new BigDecimal("10.70"),
                        new BigDecimal("106.75"), new BigDecimal("106.60"));

                multiResultResponse = createMapSearchResponse(
                        List.of(sampleMarker1, sampleMarker2), 2L,
                        new BigDecimal("10.85"), new BigDecimal("10.70"),
                        new BigDecimal("106.75"), new BigDecimal("106.60"));

                emptyResponse = createMapSearchResponse(
                        List.of(), 0L,
                        new BigDecimal("10.85"), new BigDecimal("10.70"),
                        new BigDecimal("106.75"), new BigDecimal("106.60"));
        }

        private MapSearchResponse createMapSearchResponse(List<ListingSearchResponse> content, Long totalElements,
                        BigDecimal northLat, BigDecimal southLat, BigDecimal eastLng, BigDecimal westLng) {
                MapSearchResponse response = new MapSearchResponse();
                response.setContent(content);
                response.setPage(1);
                response.setSize(20);
                response.setTotalElements(totalElements);
                response.setTotalPages(totalElements > 0 ? 1 : 0);
                response.setFirst(true);
                response.setLast(true);
                response.setBounds(MapSearchResponse.MapBoundsDTO.builder()
                                .northLat(northLat)
                                .southLat(southLat)
                                .eastLng(eastLng)
                                .westLng(westLng)
                                .build());
                response.setFilterMetadata(MapSearchResponse.FilterMetadataDTO.builder()
                                .appliedFilters(MapSearchResponse.AppliedFiltersDTO.builder().build())
                                .build());
                return response;
        }

        // =========================================================================
        // Happy Path Tests
        // =========================================================================

        @Nested
        @DisplayName("Happy Path Tests")
        class HappyPathTests {

                @Test
                @DisplayName("Should return 200 OK when searching with valid map bounds")
                void searchWithMapBounds_shouldReturnOk() throws Exception {
                        when(mapSearchService.searchPropertiesOnMap(any(MapSearchRequest.class), any()))
                                        .thenReturn(singleResultResponse);

                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": 10.70,
                                            "east_lng": 106.75,
                                            "west_lng": 106.60
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message").value(
                                                        "Properties retrieved successfully"))
                                        .andExpect(jsonPath("$.data.content").isArray())
                                        .andExpect(jsonPath("$.data.total_elements").value(1));

                        verify(mapSearchService).searchPropertiesOnMap(
                                        any(MapSearchRequest.class), any());
                }

                @Test
                @DisplayName("Should return 200 OK when searching with text only (no bounds)")
                void searchWithTextOnly_shouldReturnOk() throws Exception {
                        MapSearchResponse textResponse = createMapSearchResponse(
                                        List.of(sampleMarker1), 1L,
                                        new BigDecimal("90"), new BigDecimal("-90"),
                                        new BigDecimal("180"), new BigDecimal("-180"));
                        textResponse.setFilterMetadata(MapSearchResponse.FilterMetadataDTO.builder()
                                        .appliedFilters(MapSearchResponse.AppliedFiltersDTO.builder()
                                                        .searchText("101 Ben Nghe")
                                                        .build())
                                        .build());

                        when(mapSearchService.searchPropertiesOnMap(any(MapSearchRequest.class), any()))
                                        .thenReturn(textResponse);

                        String requestBody = """
                                        {
                                            "search_text": "101 Ben Nghe"
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.content", hasSize(1)))
                                        .andExpect(jsonPath("$.data.filter_metadata"
                                                        + ".applied_filters.search_text")
                                                        .value("101 Ben Nghe"));
                }

                @Test
                @DisplayName("Should return 200 OK when searching with all filters")
                void searchWithAllFilters_shouldReturnOk() throws Exception {
                        MapSearchResponse filteredResponse = createMapSearchResponse(
                                        List.of(sampleMarker1), 1L,
                                        new BigDecimal("10.85"), new BigDecimal("10.70"),
                                        new BigDecimal("106.75"), new BigDecimal("106.60"));
                        filteredResponse.setSize(10);
                        filteredResponse.setFilterMetadata(MapSearchResponse.FilterMetadataDTO.builder()
                                        .appliedFilters(MapSearchResponse.AppliedFiltersDTO.builder()
                                                        .listingType("SALE")
                                                        .priceRange(MapSearchResponse.PriceRangeDTO.builder()
                                                                        .min(new BigDecimal("500000"))
                                                                        .max(new BigDecimal("2000000"))
                                                                        .build())
                                                        .bedrooms(2)
                                                        .bathrooms(1)
                                                        .area(new BigDecimal("80"))
                                                        .propertyType("apartment")
                                                        .searchText("Ben Nghe")
                                                        .build())
                                        .build());

                        when(mapSearchService.searchPropertiesOnMap(any(MapSearchRequest.class), any()))
                                        .thenReturn(filteredResponse);

                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": 10.70,
                                            "east_lng": 106.75,
                                            "west_lng": 106.60,
                                            "listing_type": "SALE",
                                            "min_price": 500000,
                                            "max_price": 2000000,
                                            "bedrooms": 2,
                                            "bathrooms": 1,
                                            "area": 80,
                                            "property_type": "apartment",
                                            "search_text": "Ben Nghe",
                                            "page": 1,
                                            "size": 10
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.content", hasSize(1)))
                                        .andExpect(jsonPath("$.data.filter_metadata"
                                                        + ".applied_filters.listing_type").value("SALE"))
                                        .andExpect(jsonPath("$.data.filter_metadata"
                                                        + ".applied_filters.bedrooms").value(2))
                                        .andExpect(jsonPath("$.data.filter_metadata"
                                                        + ".applied_filters.bathrooms").value(1));
                }

                @Test
                @DisplayName("Should return 200 OK when filtering by RENT listing type")
                void searchWithRentType_shouldReturnOk() throws Exception {
                        when(mapSearchService.searchPropertiesOnMap(any(MapSearchRequest.class), any()))
                                        .thenReturn(singleResultResponse);

                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": 10.70,
                                            "east_lng": 106.75,
                                            "west_lng": 106.60,
                                            "listing_type": "RENT"
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));
                }

                @Test
                @DisplayName("Should return 200 OK when filtering by SALE listing type")
                void searchWithSaleType_shouldReturnOk() throws Exception {
                        when(mapSearchService.searchPropertiesOnMap(any(MapSearchRequest.class), any()))
                                        .thenReturn(singleResultResponse);

                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": 10.70,
                                            "east_lng": 106.75,
                                            "west_lng": 106.60,
                                            "listing_type": "SALE"
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));
                }

                @Test
                @DisplayName("Should return 200 OK when filtering by price range")
                void searchWithPriceRange_shouldReturnOk() throws Exception {
                        when(mapSearchService.searchPropertiesOnMap(any(MapSearchRequest.class), any()))
                                        .thenReturn(singleResultResponse);

                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": 10.70,
                                            "east_lng": 106.75,
                                            "west_lng": 106.60,
                                            "min_price": 1000000,
                                            "max_price": 5000000
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));
                }

                @Test
                @DisplayName("Should return 200 OK with correct pagination metadata")
                void searchWithPagination_shouldReturnCorrectMetadata()
                                throws Exception {
                        MapSearchResponse page2Response = createMapSearchResponse(
                                        List.of(sampleMarker2), 3L,
                                        new BigDecimal("10.85"), new BigDecimal("10.70"),
                                        new BigDecimal("106.75"), new BigDecimal("106.60"));
                        page2Response.setPage(2);
                        page2Response.setSize(1);
                        page2Response.setTotalPages(3);
                        page2Response.setFirst(false);
                        page2Response.setLast(false);

                        when(mapSearchService.searchPropertiesOnMap(any(MapSearchRequest.class), any()))
                                        .thenReturn(page2Response);

                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": 10.70,
                                            "east_lng": 106.75,
                                            "west_lng": 106.60,
                                            "page": 2,
                                            "size": 1
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.page").value(2))
                                        .andExpect(jsonPath("$.data.size").value(1))
                                        .andExpect(jsonPath("$.data.total_elements").value(3))
                                        .andExpect(jsonPath("$.data.total_pages").value(3))
                                        .andExpect(jsonPath("$.data.first").value(false))
                                        .andExpect(jsonPath("$.data.last").value(false));
                }

                @Test
                @DisplayName("Should return 200 OK with empty content when no results")
                void searchWithNoResults_shouldReturnEmptyContent() throws Exception {
                        when(mapSearchService.searchPropertiesOnMap(any(MapSearchRequest.class), any()))
                                        .thenReturn(emptyResponse);

                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": 10.70,
                                            "east_lng": 106.75,
                                            "west_lng": 106.60
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.content", hasSize(0)))
                                        .andExpect(jsonPath("$.data.total_elements").value(0))
                                        .andExpect(jsonPath("$.data.total_pages").value(0))
                                        .andExpect(jsonPath("$.data.first").value(true))
                                        .andExpect(jsonPath("$.data.last").value(true));
                }

                @Test
                @DisplayName("Should return 200 OK with defaults when request body is empty")
                void searchWithEmptyBody_shouldReturnOk() throws Exception {
                        when(mapSearchService.searchPropertiesOnMap(any(MapSearchRequest.class), any()))
                                        .thenReturn(emptyResponse);

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{}"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.content").isArray());
                }

                @Test
                @DisplayName("Should return 200 OK with multiple markers in content")
                void searchWithMultipleResults_shouldReturnAllMarkers()
                                throws Exception {
                        when(mapSearchService.searchPropertiesOnMap(any(MapSearchRequest.class), any()))
                                        .thenReturn(multiResultResponse);

                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": 10.70,
                                            "east_lng": 106.75,
                                            "west_lng": 106.60
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.content", hasSize(2)))
                                        .andExpect(jsonPath("$.data.total_elements").value(2));
                }
        }

        // =========================================================================
        // Edge Case / Validation Tests
        // =========================================================================

        @Nested
        @DisplayName("Edge Case / Validation Tests")
        class EdgeCaseTests {

                @Test
                @DisplayName("Should return 400 when north_lat exceeds 90")
                void searchWithLatitudeAbove90_shouldReturn400() throws Exception {
                        String requestBody = """
                                        {
                                            "north_lat": 91,
                                            "south_lat": 10.70,
                                            "east_lng": 106.75,
                                            "west_lng": 106.60
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                                        .andExpect(jsonPath("$.errors").isArray())
                                        .andExpect(jsonPath("$.errors[0].field").value("northLat"));

                        verify(mapSearchService, never())
                                        .searchPropertiesOnMap(any(), any());
                }

                @Test
                @DisplayName("Should return 400 when south_lat is below -90")
                void searchWithLatitudeBelow90_shouldReturn400() throws Exception {
                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": -91,
                                            "east_lng": 106.75,
                                            "west_lng": 106.60
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                                        .andExpect(jsonPath("$.errors").isArray())
                                        .andExpect(jsonPath("$.errors[0].field").value("southLat"));

                        verify(mapSearchService, never())
                                        .searchPropertiesOnMap(any(), any());
                }

                @Test
                @DisplayName("Should return 400 when east_lng exceeds 180")
                void searchWithLongitudeAbove180_shouldReturn400() throws Exception {
                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": 10.70,
                                            "east_lng": 181,
                                            "west_lng": 106.60
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                                        .andExpect(jsonPath("$.errors").isArray())
                                        .andExpect(jsonPath("$.errors[0].field").value("eastLng"));

                        verify(mapSearchService, never())
                                        .searchPropertiesOnMap(any(), any());
                }

                @Test
                @DisplayName("Should return 400 when west_lng is below -180")
                void searchWithLongitudeBelow180_shouldReturn400() throws Exception {
                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": 10.70,
                                            "east_lng": 106.75,
                                            "west_lng": -181
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                                        .andExpect(jsonPath("$.errors").isArray())
                                        .andExpect(jsonPath("$.errors[0].field").value("westLng"));

                        verify(mapSearchService, never())
                                        .searchPropertiesOnMap(any(), any());
                }

                @Test
                @DisplayName("Should return 400 when min_price is negative")
                void searchWithNegativeMinPrice_shouldReturn400() throws Exception {
                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": 10.70,
                                            "east_lng": 106.75,
                                            "west_lng": 106.60,
                                            "min_price": -1
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"));

                        verify(mapSearchService, never())
                                        .searchPropertiesOnMap(any(), any());
                }

                @Test
                @DisplayName("Should return 400 when page is 0 (below minimum of 1)")
                void searchWithPageZero_shouldReturn400() throws Exception {
                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": 10.70,
                                            "east_lng": 106.75,
                                            "west_lng": 106.60,
                                            "page": 0
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"));

                        verify(mapSearchService, never())
                                        .searchPropertiesOnMap(any(), any());
                }

                @Test
                @DisplayName("Should return 400 when size exceeds maximum of 100")
                void searchWithSizeExceedingMax_shouldReturn400() throws Exception {
                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": 10.70,
                                            "east_lng": 106.75,
                                            "west_lng": 106.60,
                                            "size": 101
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"));

                        verify(mapSearchService, never())
                                        .searchPropertiesOnMap(any(), any());
                }

                @Test
                @DisplayName("Should return 400 when listing_type is invalid enum value")
                void searchWithInvalidListingType_shouldReturn400() throws Exception {
                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": 10.70,
                                            "east_lng": 106.75,
                                            "west_lng": 106.60,
                                            "listing_type": "INVALID_TYPE"
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest());

                        verify(mapSearchService, never())
                                        .searchPropertiesOnMap(any(), any());
                }

                @Test
                @DisplayName("Should return 400 when service throws IllegalArgumentException")
                void searchWhenServiceThrowsIllegalArg_shouldReturn400()
                                throws Exception {
                        when(mapSearchService.searchPropertiesOnMap(any(MapSearchRequest.class), any()))
                                        .thenThrow(new IllegalArgumentException(
                                                        "Invalid search parameters"));

                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": 10.70,
                                            "east_lng": 106.75,
                                            "west_lng": 106.60
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error_code").value("INVALID_ARGUMENT"))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Invalid search parameters"));
                }

                @Test
                @DisplayName("Should return 500 when service throws RuntimeException")
                void searchWhenServiceThrowsRuntime_shouldReturn500()
                                throws Exception {
                        when(mapSearchService.searchPropertiesOnMap(any(MapSearchRequest.class), any()))
                                        .thenThrow(new RuntimeException("Database connection failed"));

                        String requestBody = """
                                        {
                                            "north_lat": 10.85,
                                            "south_lat": 10.70,
                                            "east_lng": 106.75,
                                            "west_lng": 106.60
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isInternalServerError())
                                        .andExpect(jsonPath("$.error_code")
                                                        .value("INTERNAL_SERVER_ERROR"));
                }
        }

        // =========================================================================
        // Response Structure Tests
        // =========================================================================

        @Nested
        @DisplayName("Response Structure Tests")
        class ResponseStructureTests {

                @Test
                @DisplayName("Should return complete response structure with all fields")
                void responseStructure_shouldContainAllRequiredFields()
                                throws Exception {
                        when(mapSearchService.searchPropertiesOnMap(any(MapSearchRequest.class), any()))
                                        .thenReturn(singleResultResponse);

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{}"))
                                        .andExpect(status().isOk())
                                        // Top-level ApiResponse
                                        .andExpect(jsonPath("$.success").exists())
                                        .andExpect(jsonPath("$.message").exists())
                                        .andExpect(jsonPath("$.timestamp").exists())
                                        .andExpect(jsonPath("$.data").exists())
                                        // PageResponse fields
                                        .andExpect(jsonPath("$.data.content").isArray())
                                        .andExpect(jsonPath("$.data.page").isNumber())
                                        .andExpect(jsonPath("$.data.size").isNumber())
                                        .andExpect(jsonPath("$.data.total_elements").isNumber())
                                        .andExpect(jsonPath("$.data.total_pages").isNumber())
                                        .andExpect(jsonPath("$.data.first").isBoolean())
                                        .andExpect(jsonPath("$.data.last").isBoolean())
                                        // MapSearchResponse-specific
                                        .andExpect(jsonPath("$.data.bounds").exists())
                                        .andExpect(jsonPath("$.data.bounds.north_lat").isNumber())
                                        .andExpect(jsonPath("$.data.bounds.south_lat").isNumber())
                                        .andExpect(jsonPath("$.data.bounds.east_lng").isNumber())
                                        .andExpect(jsonPath("$.data.bounds.west_lng").isNumber())
                                        .andExpect(jsonPath("$.data.filter_metadata").exists());
                }

                @Test
                @DisplayName("Should return correct marker structure with all fields")
                void markerStructure_shouldContainAllFields() throws Exception {
                        when(mapSearchService.searchPropertiesOnMap(any(MapSearchRequest.class), any()))
                                        .thenReturn(singleResultResponse);

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{}"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.content[0].listing_id")
                                                        .value(sampleMarker1.getListingId().toString()))
                                        .andExpect(jsonPath("$.data.content[0]"
                                                        + ".coordinates.latitude").value(10.776))
                                        .andExpect(jsonPath("$.data.content[0]"
                                                        + ".coordinates.longitude").value(106.687))
                                        .andExpect(jsonPath("$.data.content[0].street_address")
                                                        .value("101 Ben Nghe, Q1"))
                                        .andExpect(jsonPath("$.data.content[0].price")
                                                        .value(1258552))
                                        .andExpect(jsonPath("$.data.content[0].listing_type")
                                                        .value("SALE"))
                                        .andExpect(jsonPath("$.data.content[0].name")
                                                        .value("2BR Apartment - Ben Nghe Ward"))
                                        .andExpect(jsonPath("$.data.content[0].thumbnail")
                                                        .value("https://example.com/thumb1.jpg"))
                                        .andExpect(jsonPath("$.data.content[0].bedrooms")
                                                        .value(2))
                                        .andExpect(jsonPath("$.data.content[0].bathrooms")
                                                        .value(1))
                                        .andExpect(jsonPath("$.data.content[0].area")
                                                        .value(80.00))
                                        .andExpect(jsonPath("$.data.content[0].property_type_name")
                                                        .value("Apartment"))
                                        .andExpect(jsonPath("$.data.content[0].ward_name")
                                                        .value("Ben Nghe Ward"))
                                        .andExpect(jsonPath("$.data.content[0].is_favorite")
                                                        .value(false));
                }

                @Test
                @DisplayName("Should echo back applied filters in filter_metadata")
                void filterMetadata_shouldEchoAppliedFilters() throws Exception {
                        MapSearchResponse responseWithFilters = createMapSearchResponse(
                                        List.of(sampleMarker1), 1L,
                                        new BigDecimal("10.85"), new BigDecimal("10.70"),
                                        new BigDecimal("106.75"), new BigDecimal("106.60"));
                        responseWithFilters.setFilterMetadata(MapSearchResponse.FilterMetadataDTO.builder()
                                        .appliedFilters(MapSearchResponse.AppliedFiltersDTO.builder()
                                                        .listingType("SALE")
                                                        .priceRange(MapSearchResponse.PriceRangeDTO.builder()
                                                                        .min(new BigDecimal("1000000"))
                                                                        .max(new BigDecimal("5000000"))
                                                                        .build())
                                                        .searchText("Apartment")
                                                        .bedrooms(2)
                                                        .bathrooms(1)
                                                        .area(new BigDecimal("80"))
                                                        .propertyType("apartment")
                                                        .rentalPeriod("1-12")
                                                        .build())
                                        .build());

                        when(mapSearchService.searchPropertiesOnMap(any(MapSearchRequest.class), any()))
                                        .thenReturn(responseWithFilters);

                        String requestBody = """
                                        {
                                            "listing_type": "SALE",
                                            "min_price": 1000000,
                                            "max_price": 5000000,
                                            "search_text": "Apartment",
                                            "bedrooms": 2,
                                            "bathrooms": 1,
                                            "area": 80,
                                            "property_type": "apartment",
                                            "rental_period": "1-12"
                                        }
                                        """;

                        mockMvc.perform(post(MAP_SEARCH_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.filter_metadata"
                                                        + ".applied_filters.listing_type").value("SALE"))
                                        .andExpect(jsonPath("$.data.filter_metadata"
                                                        + ".applied_filters.price_range.min")
                                                        .value(1000000))
                                        .andExpect(jsonPath("$.data.filter_metadata"
                                                        + ".applied_filters.price_range.max")
                                                        .value(5000000))
                                        .andExpect(jsonPath("$.data.filter_metadata"
                                                        + ".applied_filters.search_text")
                                                        .value("Apartment"))
                                        .andExpect(jsonPath("$.data.filter_metadata"
                                                        + ".applied_filters.bedrooms").value(2))
                                        .andExpect(jsonPath("$.data.filter_metadata"
                                                        + ".applied_filters.bathrooms").value(1))
                                        .andExpect(jsonPath("$.data.filter_metadata"
                                                        + ".applied_filters.area").value(80))
                                        .andExpect(jsonPath("$.data.filter_metadata"
                                                        + ".applied_filters.property_type").value("apartment"))
                                        .andExpect(jsonPath("$.data.filter_metadata"
                                                        + ".applied_filters.rental_period")
                                                        .value("1-12"));
                }
        }
}
