package com.sep.realvista.unit.application.listing.bookmark;

import com.sep.realvista.application.listing.bookmark.dto.BookmarkResponse;
import com.sep.realvista.application.listing.bookmark.mapper.BookmarkMapper;
import com.sep.realvista.application.listing.bookmark.service.BookmarkApplicationService;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingRepository;
import com.sep.realvista.domain.listing.bookmark.Bookmark;
import com.sep.realvista.domain.listing.bookmark.BookmarkRepository;
import com.sep.realvista.domain.listing.exception.ListingNotFoundException;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BookmarkApplicationService.
 * 
 * Tests bookmark toggle business logic in isolation from infrastructure.
 * Covers scenarios including:
 * - Creating new bookmarks
 * - Removing existing bookmarks
 * - Error handling for missing users/listings
 * - Proper repository method calls
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookmarkApplicationService Unit Tests")
class BookmarkApplicationServiceTest {

    // Services and dependencies
    private BookmarkApplicationService bookmarkApplicationService;

    // Mocked dependencies
    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private BookmarkMapper bookmarkMapper;

    private static final UUID TEST_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID TEST_LISTING_ID = UUID.fromString("650e8400-e29b-41d4-a716-446655440002");

    @BeforeEach
    void setUp() {
        bookmarkApplicationService = new BookmarkApplicationService(
                bookmarkRepository,
                userRepository,
                listingRepository,
                bookmarkMapper
        );
    }

    /**
     * Test Case: Create new bookmark successfully
     * Expected: Bookmark saved, response shows bookmarked=true
     */
    @Test
    @DisplayName("Should create new bookmark when not already bookmarked")
    void toggleBookmark_whenNotExists_shouldCreateBookmark() {
        // Arrange
        User testUser = createTestUser();
        Listing testListing = createTestListing();
        
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(listingRepository.findById(TEST_LISTING_ID)).thenReturn(Optional.of(testListing));
        when(bookmarkRepository.existsByUserIdAndListingId(TEST_USER_ID, TEST_LISTING_ID))
                .thenReturn(false);
        
        BookmarkResponse expectedResponse = createBookmarkResponse(true);
        when(bookmarkMapper.toResponse(any(User.class), any(Listing.class), any(Boolean.class), any(LocalDateTime.class)))
                .thenReturn(expectedResponse);

        // Act
        BookmarkResponse result = bookmarkApplicationService.toggleBookmark(TEST_USER_ID, TEST_LISTING_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isBookmarked()).isTrue();
        
        // Verify save was called
        verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
        
        // Verify delete was NOT called
        verify(bookmarkRepository, never()).deleteByUserIdAndListingId(any(UUID.class), any(UUID.class));
    }

    /**
     * Test Case: Remove existing bookmark
     * Expected: Bookmark deleted, response shows bookmarked=false
     */
    @Test
    @DisplayName("Should remove bookmark when already bookmarked")
    void toggleBookmark_whenExists_shouldDeleteBookmark() {
        // Arrange
        User testUser = createTestUser();
        Listing testListing = createTestListing();
        
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(listingRepository.findById(TEST_LISTING_ID)).thenReturn(Optional.of(testListing));
        when(bookmarkRepository.existsByUserIdAndListingId(TEST_USER_ID, TEST_LISTING_ID))
                .thenReturn(true);
        
        BookmarkResponse expectedResponse = createBookmarkResponse(false);
        when(bookmarkMapper.toResponse(any(User.class), any(Listing.class), any(Boolean.class), any(LocalDateTime.class)))
                .thenReturn(expectedResponse);

        // Act
        BookmarkResponse result = bookmarkApplicationService.toggleBookmark(TEST_USER_ID, TEST_LISTING_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isBookmarked()).isFalse();
        
        // Verify delete was called
        verify(bookmarkRepository, times(1)).deleteByUserIdAndListingId(TEST_USER_ID, TEST_LISTING_ID);
        
        // Verify save was NOT called
        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    /**
     * Test Case: User not found
     * Expected: UserNotFoundException thrown
     */
    @Test
    @DisplayName("Should throw UserNotFoundException when user doesn't exist")
    void toggleBookmark_whenUserNotFound_shouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookmarkApplicationService.toggleBookmark(TEST_USER_ID, TEST_LISTING_ID))
                .isInstanceOf(UserNotFoundException.class);
        
        // Verify repositories not called after user validation fails
        verify(listingRepository, never()).findById(any(UUID.class));
        verify(bookmarkRepository, never()).existsByUserIdAndListingId(any(UUID.class), any(UUID.class));
    }

    /**
     * Test Case: Listing not found
     * Expected: ListingNotFoundException thrown
     */
    @Test
    @DisplayName("Should throw ListingNotFoundException when listing doesn't exist")
    void toggleBookmark_whenListingNotFound_shouldThrowListingNotFoundException() {
        // Arrange
        User testUser = createTestUser();
        
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(listingRepository.findById(TEST_LISTING_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookmarkApplicationService.toggleBookmark(TEST_USER_ID, TEST_LISTING_ID))
                .isInstanceOf(ListingNotFoundException.class);
        
        // Verify bookmark checks not made after listing validation fails
        verify(bookmarkRepository, never()).existsByUserIdAndListingId(any(UUID.class), any(UUID.class));
    }

    /**
     * Test Case: Validate bookmark object structure on create
     * Expected: Bookmark created with correct userId and listingId
     */
    @Test
    @DisplayName("Should create bookmark with correct user and listing IDs")
    void toggleBookmark_shouldCreateBookmarkWithCorrectIds() {
        // Arrange
        User testUser = createTestUser();
        Listing testListing = createTestListing();
        
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(listingRepository.findById(TEST_LISTING_ID)).thenReturn(Optional.of(testListing));
        when(bookmarkRepository.existsByUserIdAndListingId(TEST_USER_ID, TEST_LISTING_ID))
                .thenReturn(false);
        
        BookmarkResponse expectedResponse = createBookmarkResponse(true);
        when(bookmarkMapper.toResponse(any(User.class), any(Listing.class), any(Boolean.class), any(LocalDateTime.class)))
                .thenReturn(expectedResponse);

        // Act
        bookmarkApplicationService.toggleBookmark(TEST_USER_ID, TEST_LISTING_ID);

        // Assert - capture bookmark argument and verify
        ArgumentCaptor<Bookmark> bookmarkCaptor = ArgumentCaptor.forClass(Bookmark.class);
        verify(bookmarkRepository).save(bookmarkCaptor.capture());
        
        Bookmark savedBookmark = bookmarkCaptor.getValue();
        assertThat(savedBookmark.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(savedBookmark.getListingId()).isEqualTo(TEST_LISTING_ID);
    }

    /**
     * Test Case: Response mapping is called with correct parameters
     * Expected: Mapper called with user, listing, bookmarked flag, and timestamp
     */
    @Test
    @DisplayName("Should call mapper with correct parameters")
    void toggleBookmark_shouldCallMapperWithCorrectParams() {
        // Arrange
        User testUser = createTestUser();
        Listing testListing = createTestListing();
        
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(listingRepository.findById(TEST_LISTING_ID)).thenReturn(Optional.of(testListing));
        when(bookmarkRepository.existsByUserIdAndListingId(TEST_USER_ID, TEST_LISTING_ID))
                .thenReturn(false);
        
        BookmarkResponse expectedResponse = createBookmarkResponse(true);
        when(bookmarkMapper.toResponse(any(User.class), any(Listing.class), any(Boolean.class), any(LocalDateTime.class)))
                .thenReturn(expectedResponse);

        // Act
        bookmarkApplicationService.toggleBookmark(TEST_USER_ID, TEST_LISTING_ID);

        // Assert - verify mapper was called with correct boolean flag
        verify(bookmarkMapper).toResponse(eq(testUser), eq(testListing), eq(true), any(LocalDateTime.class));
    }

    // Helper methods to create test data
    private User createTestUser() {
        return User.builder()
                .userId(TEST_USER_ID)
                .build();
    }

    private Listing createTestListing() {
        return Listing.builder()
                .listingId(TEST_LISTING_ID)
                .build();
    }

    private BookmarkResponse createBookmarkResponse(boolean isBookmarked) {
        return BookmarkResponse.builder()
                .userId(TEST_USER_ID)
                .listingId(TEST_LISTING_ID)
                .userEmail("buyer@example.com")
                .userFullName("John Doe")
                .listingType("APARTMENT")
                .propertyAddress("123 Main St, City")
                .bookmarked(isBookmarked)
                .actionTimestamp(LocalDateTime.now())
                .build();
    }
}
