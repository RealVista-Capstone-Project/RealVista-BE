// package com.sep.realvista.integration.listing;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.sep.realvista.application.auth.service.TokenService;
// import com.sep.realvista.application.listing.dto.AdvancedSearchRequest;
// import com.sep.realvista.infrastructure.security.jwt.JwtAuthenticationFilter;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.context.TestPropertySource;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.test.web.servlet.MockMvc;

// import java.math.BigDecimal;
// import java.util.Collections;
// import java.util.List;
// import java.util.UUID;

// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest
// @AutoConfigureMockMvc(addFilters = false)
// @ActiveProfiles("test")
// @TestPropertySource(properties = {
//     "spring.jpa.hibernate.ddl-auto=none",
//     "spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false"
// })
// @DisplayName("Listing Search Integration Tests (End-to-End)")
// class ListingSearchIntegrationTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private JdbcTemplate jdbcTemplate;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @MockitoBean
//     private JwtAuthenticationFilter jwtAuthenticationFilter;

//     @MockitoBean
//     private TokenService tokenService;

//     @MockitoBean
//     private UserDetailsService userDetailsService;

//     @BeforeEach
//     void setup() {
//         jdbcTemplate.execute("CREATE ALIAS IF NOT EXISTS jsonb_extract_path_text FOR \"com.sep.realvista.integration.H2FunctionAliases.jsonbExtractPathText\"");
//     }

//     @Test
//     @DisplayName("Sanity Check: Should have listings in the database")
//     void sanityCheck_shouldHaveListings() {
//         Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM listings", Integer.class);
//         org.assertj.core.api.Assertions.assertThat(count).isGreaterThan(0);
//     }

//     @Test
//     @DisplayName("Should find Apartment in District 7 by property type and location")
//     void search_byPropertyTypeAndLocation_shouldReturnMatches() throws Exception {
//         AdvancedSearchRequest request = AdvancedSearchRequest.builder()
//                 .propertyType(UUID.fromString("880e8400-e29b-41d4-a716-446655440001"))
//                 .location("Quận 7")
//                 .build();

//         mockMvc.perform(post("/api/v1/listings/search")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andDo(print())
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.totalElements").value(1))
//                 .andExpect(jsonPath("$.content[0].title").value("Property at 15 Nguyễn Lương Bằng"));
//     }

//     @Test
//     @DisplayName("Should find Villa by price range")
//     void search_byPriceRange_shouldReturnVilla() throws Exception {
//         AdvancedSearchRequest request = AdvancedSearchRequest.builder()
//                 .price(List.of(new BigDecimal("50000000000"), new BigDecimal("60000000000")))
//                 .build();

//         mockMvc.perform(post("/api/v1/listings/search")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andDo(print())
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.totalElements").value(1))
//                 .andExpect(jsonPath("$.content[0].propertyType").value("Biệt thự"));
//     }

//     @Test
//     @DisplayName("Should find Apartment by area range")
//     void search_byAreaRange_shouldReturnApartment() throws Exception {
//         AdvancedSearchRequest request = AdvancedSearchRequest.builder()
//                 .area(List.of(new BigDecimal("80"), new BigDecimal("100")))
//                 .build();

//         mockMvc.perform(post("/api/v1/listings/search")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andDo(print())
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.totalElements").value(1))
//                 .andExpect(jsonPath("$.content[0].propertyType").value("Chung cư"));
//     }

//     @Test
//     @DisplayName("Should find properties by dynamic attribute (bedrooms)")
//     void search_byDynamicAttribute_shouldReturnMatches() throws Exception {
//         AdvancedSearchRequest request = AdvancedSearchRequest.builder()
//                 .dynamic(Collections.singletonMap("bedrooms", 5))
//                 .build();

//         mockMvc.perform(post("/api/v1/listings/search")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andDo(print())
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.totalElements").value(1))
//                 .andExpect(jsonPath("$.content[0].propertyType").value("Biệt thự"));
//     }

//     @Test
//     @DisplayName("Should not return DRAFT listings")
//     void search_shouldNotReturnDraftListings() throws Exception {
//         AdvancedSearchRequest request = AdvancedSearchRequest.builder()
//                 .location("Bình Thạnh")
//                 .build();

//         mockMvc.perform(post("/api/v1/listings/search")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andDo(print())
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.totalElements").value(0))
//                 .andExpect(jsonPath("$.content").isEmpty());
//     }
// }
