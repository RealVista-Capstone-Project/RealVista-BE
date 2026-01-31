package com.sep.realvista.component.presentation.rest.listing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.auth.service.TokenService;
import com.sep.realvista.application.listing.ListingSearchService;
import com.sep.realvista.application.listing.dto.AdvancedSearchRequest;
import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.sep.realvista.presentation.exception.GlobalExceptionHandler;
import com.sep.realvista.presentation.rest.listing.ListingSearchController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Component tests for ListingSearchController.
 */
@WebMvcTest(ListingSearchController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("ListingSearchController Component Tests")
class ListingSearchControllerComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListingSearchService listingSearchService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private ListingSearchResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = ListingSearchResponse.builder()
                .listingId(UUID.randomUUID())
                .title("Sample Apartment")
                .propertyType("Apartment")
                .location("Quận 7")
                .price(new BigDecimal("7500000000"))
                .area(new BigDecimal("85.5"))
                .build();
    }

    @Test
    @DisplayName("Should return 200 OK and paginated results for valid search")
    void search_withValidRequest_shouldReturnOk() throws Exception {
        Page<ListingSearchResponse> page = new PageImpl<>(Collections.singletonList(sampleResponse));
        when(listingSearchService.search(any(AdvancedSearchRequest.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        AdvancedSearchRequest request = AdvancedSearchRequest.builder()
                .propertyType(UUID.randomUUID())
                .location("Quận 7")
                .build();

        mockMvc.perform(post("/api/v1/listings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Sample Apartment"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("Should return 200 OK with empty result set when no matches found")
    void search_withNoResults_shouldReturnOkWithEmptyList() throws Exception {
        Page<ListingSearchResponse> page = new PageImpl<>(Collections.emptyList());
        when(listingSearchService.search(any(AdvancedSearchRequest.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        AdvancedSearchRequest request = AdvancedSearchRequest.builder()
                .location("Nonexistent Location")
                .build();

        mockMvc.perform(post("/api/v1/listings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
    @Test
    @DisplayName("Should return 200 OK for full advanced search criteria")
    void search_withFullCriteria_shouldReturnOk() throws Exception {
        Page<ListingSearchResponse> page = new PageImpl<>(Collections.singletonList(sampleResponse));
        when(listingSearchService.search(any(AdvancedSearchRequest.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        AdvancedSearchRequest request = AdvancedSearchRequest.builder()
                .listingType("SALE")
                .bedrooms(3)
                .bathrooms(2)
                .location("Quận 1")
                .build();

        mockMvc.perform(post("/api/v1/listings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty());
    }
}
