package com.sep.realvista.unit.presentation.rest.listing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.auth.service.TokenService;
import com.sep.realvista.application.listing.ListingSearchService;
import com.sep.realvista.application.listing.dto.AdvancedSearchRequest;
import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.presentation.rest.listing.ListingSearchController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ListingSearchController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit test
@DisplayName("ListingSearchController Unit Test")
class ListingSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListingSearchService listingSearchService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return 200 OK and paginated results")
    void shouldReturnOkAndResults() throws Exception {
        // Given
        AdvancedSearchRequest request = AdvancedSearchRequest.builder()
                .propertyType(UUID.randomUUID())
                .build();

        ListingSearchResponse responseDTO = ListingSearchResponse.builder()
                .listingId(UUID.randomUUID())
                .title("Sample Listing")
                .build();

        Page<ListingSearchResponse> page = new PageImpl<>(Collections.singletonList(responseDTO));
        when(listingSearchService.search(any(AdvancedSearchRequest.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(post("/api/v1/listings/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Sample Listing"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
