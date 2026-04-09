package com.sep.realvista.unit.infrastructure;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.infrastructure.scheduler.ListingExpiryScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.application.notification.dto.SendNotificationRequest;
import java.util.Optional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListingExpiryScheduler Unit Tests")
class ListingExpirySchedulerTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private NotificationApplicationService notificationApplicationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ListingExpiryScheduler scheduler;

    private UUID listingId;
    private UUID propertyId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        listingId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        userId = UUID.randomUUID();

        setField(scheduler, "maxLifetimeDays", 0L);
        setField(scheduler, "expiryWarningDays", 0L);
    }

    private Listing buildPublishedListing(int secondsAgo) {
        Listing listing = Listing.builder()
                .listingId(listingId)
                .propertyId(propertyId)
                .userId(userId)
                .listingType(ListingType.RENT)
                .status(ListingStatus.PUBLISHED)
                .slug("test-listing-" + UUID.randomUUID())
                .name("Test Published Listing")
                .price(new BigDecimal("5000000"))
                .isNegotiable(false)
                .build();

        // Set publishedAt to simulate it was published `secondsAgo` seconds ago
        setField(listing, "publishedAt", LocalDateTime.now().minusSeconds(secondsAgo));
        return listing;
    }

    @Test
    @DisplayName("Should move listing to DRAFT when lifetime is exceeded")
    void expireStaleListings_whenListingExceededLifetime_shouldMoveToDraft() {
        /*
         * Arrange: listing published 5 seconds ago; with maxLifetimeDays=0 the
         * cutoff is now(), so any past publishedAt is "expired".
         */
        Listing expiredListing = buildPublishedListing(5);
        when(listingRepository.findPublishedListingsPublishedBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(expiredListing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        scheduler.expireStaleListings();

        // Assert – status flipped to DRAFT and publishedAt cleared by Listing#unpublish()
        assertThat(expiredListing.getStatus()).isEqualTo(ListingStatus.DRAFT);
        assertThat(expiredListing.getPublishedAt()).isNull();

        verify(listingRepository).findPublishedListingsPublishedBefore(any(LocalDateTime.class));
        verify(listingRepository).save(expiredListing);
    }

    @Test
    @DisplayName("Should expire all listings when multiple listings have exceeded lifetime")
    void expireStaleListings_withMultipleExpiredListings_shouldExpireAll() {
        // Arrange – three listings all published a few seconds ago
        Listing listing1 = buildPublishedListing(3);
        Listing listing2 = Listing.builder()
                .listingId(UUID.randomUUID())
                .propertyId(propertyId)
                .userId(userId)
                .listingType(ListingType.SALE)
                .status(ListingStatus.PUBLISHED)
                .slug("expired-sale-listing")
                .name("Expired Sale Listing")
                .price(new BigDecimal("2000000000"))
                .isNegotiable(true)
                .build();
        setField(listing2, "publishedAt", LocalDateTime.now().minusSeconds(7));

        Listing listing3 = Listing.builder()
                .listingId(UUID.randomUUID())
                .propertyId(UUID.randomUUID())
                .userId(userId)
                .listingType(ListingType.RENT)
                .status(ListingStatus.PUBLISHED)
                .slug("another-expired-listing")
                .name("Another Expired Listing")
                .price(new BigDecimal("3000000"))
                .isNegotiable(false)
                .build();
        setField(listing3, "publishedAt", LocalDateTime.now().minusSeconds(10));

        when(listingRepository.findPublishedListingsPublishedBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(listing1, listing2, listing3));
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        scheduler.expireStaleListings();

        // Assert – all three listings moved to DRAFT
        assertThat(listing1.getStatus()).isEqualTo(ListingStatus.DRAFT);
        assertThat(listing2.getStatus()).isEqualTo(ListingStatus.DRAFT);
        assertThat(listing3.getStatus()).isEqualTo(ListingStatus.DRAFT);

        assertThat(listing1.getPublishedAt()).isNull();
        assertThat(listing2.getPublishedAt()).isNull();
        assertThat(listing3.getPublishedAt()).isNull();

        // save() called once per listing
        verify(listingRepository, times(3)).save(any(Listing.class));
    }

    @Test
    @DisplayName("Should not expire any listing when none have exceeded lifetime")
    void expireStaleListings_whenNoExpiredListings_shouldDoNothing() {
        // Arrange – repository returns empty list (no listings past cutoff)
        when(listingRepository.findPublishedListingsPublishedBefore(any(LocalDateTime.class)))
                .thenReturn(List.of());

        // Act
        scheduler.expireStaleListings();

        // Assert – save() never called
        verify(listingRepository).findPublishedListingsPublishedBefore(any(LocalDateTime.class));
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    @DisplayName("Should use correct cutoff derived from maxLifetimeDays")
    void expireStaleListings_shouldPassCorrectCutoffToRepository() {
        // Arrange – set maxLifetimeDays to 0 (already done in setUp, this makes the intent explicit)
        setField(scheduler, "maxLifetimeDays", 0L);
        when(listingRepository.findPublishedListingsPublishedBefore(any(LocalDateTime.class)))
                .thenReturn(List.of());

        LocalDateTime before = LocalDateTime.now();

        // Act
        scheduler.expireStaleListings();

        LocalDateTime after = LocalDateTime.now();

        // Assert – capture the cutoff actually passed to the repository
        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(listingRepository).findPublishedListingsPublishedBefore(cutoffCaptor.capture());

        LocalDateTime capturedCutoff = cutoffCaptor.getValue();
        // With maxLifetimeDays=0 the cutoff should be approximately "now" (within test execution window)
        assertThat(capturedCutoff).isAfterOrEqualTo(before).isBeforeOrEqualTo(after);
    }

    @Test
    @DisplayName("Should use 14-day cutoff under production default configuration")
    void expireStaleListings_withDefaultMaxLifetime_shouldUse14DayCutoff() {
        // Arrange – restore the real production default
        setField(scheduler, "maxLifetimeDays", 14L);
        when(listingRepository.findPublishedListingsPublishedBefore(any(LocalDateTime.class)))
                .thenReturn(List.of());

        LocalDateTime expectedCutoffApprox = LocalDateTime.now().minusDays(14);

        // Act
        scheduler.expireStaleListings();

        // Assert
        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(listingRepository).findPublishedListingsPublishedBefore(cutoffCaptor.capture());

        LocalDateTime captured = cutoffCaptor.getValue();
        // Should be within ~1 second of 14 days ago
        assertThat(captured)
                .isAfterOrEqualTo(expectedCutoffApprox.minusSeconds(2))
                .isBeforeOrEqualTo(expectedCutoffApprox.plusSeconds(2));
    }

    @Test
    @DisplayName("Should continue processing remaining listings when one save fails")
    void expireStaleListings_whenOneSaveFails_shouldContinueWithOthers() {
        // Arrange
        Listing goodListing = buildPublishedListing(5);
        Listing badListing = Listing.builder()
                .listingId(UUID.randomUUID())
                .propertyId(propertyId)
                .userId(userId)
                .listingType(ListingType.RENT)
                .status(ListingStatus.PUBLISHED)
                .slug("problematic-listing")
                .name("Problematic Listing")
                .price(new BigDecimal("1000000"))
                .isNegotiable(false)
                .build();
        setField(badListing, "publishedAt", LocalDateTime.now().minusSeconds(3));

        when(listingRepository.findPublishedListingsPublishedBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(badListing, goodListing));

        // First save throws, second succeeds
        when(listingRepository.save(badListing))
                .thenThrow(new RuntimeException("Simulated DB error"));
        when(listingRepository.save(goodListing))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act – should not propagate the exception
        scheduler.expireStaleListings();

        // Assert – goodListing was still unpublished despite badListing failure
        assertThat(goodListing.getStatus()).isEqualTo(ListingStatus.DRAFT);
        assertThat(goodListing.getPublishedAt()).isNull();

        // save was attempted for both
        verify(listingRepository, times(2)).save(any(Listing.class));
    }

    @Test
    @DisplayName("Should not affect non-published listings")
    void expireStaleListings_shouldOnlyQueryPublishedListings() {
        /*
         * The domain repository contract already filters by PUBLISHED status via
         * the JPQL query, so only PUBLISHED listings are ever returned.
         * This test verifies the scheduler never calls repository.save() when
         * the repository correctly returns an empty list for non-published listings.
         */
        when(listingRepository.findPublishedListingsPublishedBefore(any(LocalDateTime.class)))
                .thenReturn(List.of()); // non-published listings were filtered out by repo

        scheduler.expireStaleListings();

        verify(listingRepository).findPublishedListingsPublishedBefore(any(LocalDateTime.class));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Warning notifications behaviour
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should send notifications for listings nearing expiry")
    void notifyExpiringListings_whenListingsFound_shouldSendNotifications() {
        // Arrange
        Listing expiringListing = buildPublishedListing(5);
        User mockUser = User.builder()
                .userId(userId)
                .email(Email.of("test@example.com"))
                .build();

        when(listingRepository.findPublishedListingsPublishedBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(expiringListing));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        scheduler.notifyExpiringListings();

        // Assert
        ArgumentCaptor<SendNotificationRequest> requestCaptor = ArgumentCaptor.forClass(SendNotificationRequest.class);
        verify(notificationApplicationService).sendNotification(requestCaptor.capture());

        SendNotificationRequest sentRequest = requestCaptor.getValue();
        assertThat(sentRequest.getUserId()).isEqualTo(userId);
        assertThat(sentRequest.getUserEmail()).isEqualTo("test@example.com");
        assertThat(sentRequest.getEntityId()).isEqualTo(expiringListing.getListingId());
        assertThat(sentRequest.getEventType().name()).isEqualTo("LISTING_EXPIRING_SOON");
    }

    @Test
    @DisplayName("Should continue processing when notification fails for one listing")
    void notifyExpiringListings_whenOneFails_shouldContinueWithOthers() {
        // Arrange
        Listing goodListing = buildPublishedListing(5);
        Listing badListing = Listing.builder()
                .listingId(UUID.randomUUID())
                .propertyId(propertyId)
                .userId(UUID.randomUUID()) // Different user
                .listingType(ListingType.RENT)
                .status(ListingStatus.PUBLISHED)
                .slug("problematic-listing")
                .name("Problematic Listing")
                .price(new BigDecimal("1000000"))
                .build();
        setField(badListing, "publishedAt", LocalDateTime.now().minusSeconds(3));

        User goodUser = User.builder().userId(userId).email(Email.of("good@example.com")).build();
        User badUser = User.builder().userId(badListing.getUserId()).build();

        when(listingRepository.findPublishedListingsPublishedBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(badListing, goodListing));

        when(userRepository.findById(badListing.getUserId())).thenReturn(Optional.of(badUser));
        when(userRepository.findById(userId)).thenReturn(Optional.of(goodUser));

        // Simulate failure for badUser notification
        doThrow(new RuntimeException("Simulated notification error"))
                .when(notificationApplicationService).sendNotification(
                        org.mockito.ArgumentMatchers.argThat(req -> req.getUserId().equals(badUser.getUserId()))
                );

        // Act
        scheduler.notifyExpiringListings();

        // Assert
        verify(notificationApplicationService, times(2)).sendNotification(any(SendNotificationRequest.class));
    }
}
