package com.sep.realvista.component.presentation.rest.listing.bookmark;

import com.sep.realvista.application.auth.service.TokenService;
import com.sep.realvista.application.listing.bookmark.dto.BookmarkResponse;
import com.sep.realvista.application.listing.bookmark.service.BookmarkApplicationService;
import com.sep.realvista.application.user.service.UserApplicationService;
import com.sep.realvista.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.sep.realvista.presentation.exception.GlobalExceptionHandler;
import com.sep.realvista.presentation.rest.listing.bookmark.BookmarkController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Component tests for BookmarkController.
 * <p>
 * Tests the web layer (controller) with Spring MVC infrastructure while mocking
 * the business layer (services).
 * <p>
 * Tests bookmark toggle functionality including:
 * - Creating new bookmarks
 * - Removing existing bookmarks
 * - Error handling
 */
@WebMvcTest(controllers = BookmarkController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("BookmarkController Component Tests (Web Layer)")
class BookmarkControllerComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookmarkApplicationService bookmarkApplicationService;

    @MockitoBean
    private UserApplicationService userApplicationService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final UUID TEST_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID TEST_LISTING_ID = UUID.fromString("650e8400-e29b-41d4-a716-446655440002");
    private static final String TEST_USER_EMAIL = "buyer@example.com";

    private BookmarkResponse mockBookmarkResponse;

    @BeforeEach
    void setUp() {
        // Prepare test bookmark response - created state
        mockBookmarkResponse = BookmarkResponse.builder()
                .userId(TEST_USER_ID)
                .listingId(TEST_LISTING_ID)
                .userEmail(TEST_USER_EMAIL)
                .userFullName("John Doe")
                .listingType("APARTMENT")
                .propertyAddress("123 Main St, City")
                .bookmarked(true)
                .actionTimestamp(LocalDateTime.now())
                .build();

        // Mock user service to return user ID when email is provided
        when(userApplicationService.findUserIdByEmail(TEST_USER_EMAIL))
                .thenReturn(TEST_USER_ID);

        // Mock bookmark service to return response
        when(bookmarkApplicationService.toggleBookmark(any(UUID.class), any(UUID.class)))
                .thenReturn(mockBookmarkResponse);
    }

    /**
     * Test Case: Create new bookmark
     * Expected: 200 OK with bookmark data showing bookmarked=true
     */
    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = {"BUYER"})
    @DisplayName("Should create bookmark and return 200 OK with bookmarked=true")
    void toggleBookmark_whenNotBookmarked_shouldCreateAndReturn200() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/listings/bookmark/{listingId}", TEST_LISTING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Listing bookmarked successfully"))
                .andExpect(jsonPath("$.data.user_id").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.data.listing_id").value(TEST_LISTING_ID.toString()))
                .andExpect(jsonPath("$.data.user_email").value(TEST_USER_EMAIL))
                .andExpect(jsonPath("$.data.user_full_name").value("John Doe"))
                .andExpect(jsonPath("$.data.listing_type").value("APARTMENT"))
                .andExpect(jsonPath("$.data.property_address").value("123 Main St, City"))
                .andExpect(jsonPath("$.data.bookmarked").value(true))
                .andExpect(jsonPath("$.data.action_timestamp").exists());
    }

    /**
     * Test Case: Remove existing bookmark
     * Expected: 200 OK with bookmark data showing bookmarked=false
     */
    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = {"BUYER"})
    @DisplayName("Should remove bookmark and return 200 OK with bookmarked=false")
    void toggleBookmark_whenBookmarked_shouldRemoveAndReturn200() throws Exception {
        // Arrange: Mock bookmark removal response
        BookmarkResponse removeResponse = BookmarkResponse.builder()
                .userId(TEST_USER_ID)
                .listingId(TEST_LISTING_ID)
                .userEmail(TEST_USER_EMAIL)
                .userFullName("John Doe")
                .listingType("APARTMENT")
                .propertyAddress("123 Main St, City")
                .bookmarked(false)
                .actionTimestamp(LocalDateTime.now())
                .build();

        when(bookmarkApplicationService.toggleBookmark(any(UUID.class), any(UUID.class)))
                .thenReturn(removeResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/listings/bookmark/{listingId}", TEST_LISTING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Listing bookmark removed successfully"))
                .andExpect(jsonPath("$.data.bookmarked").value(false));
    }

    /**
     * Test Case: Valid UUID format
     * Expected: 200 OK with correct UUID in response
     */
    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = {"BUYER"})
    @DisplayName("Should handle UUID path variable correctly")
    void toggleBookmark_withValidUuid_shouldParseCorrectly() throws Exception {
        // Arrange
        UUID specificListingId = UUID.randomUUID();
        BookmarkResponse specificResponse = BookmarkResponse.builder()
                .userId(TEST_USER_ID)
                .listingId(specificListingId)
                .userEmail(TEST_USER_EMAIL)
                .userFullName("John Doe")
                .listingType("HOUSE")
                .propertyAddress("456 Oak Ave")
                .bookmarked(true)
                .actionTimestamp(LocalDateTime.now())
                .build();

        when(bookmarkApplicationService.toggleBookmark(any(UUID.class), any(UUID.class)))
                .thenReturn(specificResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/listings/bookmark/{listingId}", specificListingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.listing_id").value(specificListingId.toString()))
                .andExpect(jsonPath("$.data.property_address").value("456 Oak Ave"));
    }

    /**
     * Test Case: User not found error
     * Expected: 404 Not Found
     */
    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = {"BUYER"})
    @DisplayName("Should return 404 when user not found")
    void toggleBookmark_whenUserNotFound_shouldReturn404() throws Exception {
        // Arrange: Mock user not found
        when(userApplicationService.findUserIdByEmail(TEST_USER_EMAIL))
                .thenThrow(new com.sep.realvista.domain.common.exception.ResourceNotFoundException(
                        "User", TEST_USER_EMAIL));

        // Act & Assert
        mockMvc.perform(post("/api/v1/listings/bookmark/{listingId}", TEST_LISTING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error_code").value("RESOURCE_NOT_FOUND"));
    }

    /**
     * Test Case: Listing not found error
     * Expected: 404 Not Found
     */
    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = {"BUYER"})
    @DisplayName("Should return 404 when listing not found")
    void toggleBookmark_whenListingNotFound_shouldReturn404() throws Exception {
        // Arrange: Mock listing not found
        when(bookmarkApplicationService.toggleBookmark(any(UUID.class), any(UUID.class)))
                .thenThrow(new com.sep.realvista.domain.common.exception.ResourceNotFoundException(
                        "Listing", TEST_LISTING_ID));

        // Act & Assert
        mockMvc.perform(post("/api/v1/listings/bookmark/{listingId}", TEST_LISTING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error_code").value("RESOURCE_NOT_FOUND"));
    }

    /**
     * Test Case: Verify complete response structure
     * Expected: All fields present in response
     */
    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = {"BUYER"})
    @DisplayName("Should return complete bookmark response with all required fields")
    void toggleBookmark_shouldReturnCompleteResponseStructure() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/listings/bookmark/{listingId}", TEST_LISTING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.user_id").exists())
                .andExpect(jsonPath("$.data.listing_id").exists())
                .andExpect(jsonPath("$.data.user_email").exists())
                .andExpect(jsonPath("$.data.user_full_name").exists())
                .andExpect(jsonPath("$.data.listing_type").exists())
                .andExpect(jsonPath("$.data.property_address").exists())
                .andExpect(jsonPath("$.data.bookmarked").exists())
                .andExpect(jsonPath("$.data.action_timestamp").exists());
    }
}
