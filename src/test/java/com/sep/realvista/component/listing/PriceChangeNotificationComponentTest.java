package com.sep.realvista.component.listing;

import com.sep.realvista.application.listing.dto.UpdateListingRequest;
import com.sep.realvista.application.listing.service.ListingApplicationService;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.bookmark.Bookmark;
import com.sep.realvista.domain.listing.bookmark.BookmarkRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyCategory;
import com.sep.realvista.domain.property.PropertyStatus;
import com.sep.realvista.domain.property.PropertyType;
import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.domain.property.location.LocationRepository;
import com.sep.realvista.domain.property.location.LocationType;
import com.sep.realvista.domain.property.repository.PropertyCategoryRepository;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.domain.property.repository.PropertyTypeRepository;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.UserStatus;
import com.sep.realvista.domain.user.notification.EventType;
import com.sep.realvista.domain.user.notification.NotificationTemplate;
import com.sep.realvista.domain.user.notification.NotificationTemplateRepository;
import com.sep.realvista.domain.user.preference.SettingPreference;
import com.sep.realvista.domain.user.preference.SettingPreferenceRepository;
import com.sep.realvista.application.service.EmailService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Full-context test: listing price update notifies bookmarked users (in-app persisted + async email).
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Price change notifications for bookmarked listings")
class PriceChangeNotificationComponentTest {

    @Autowired
    private ListingApplicationService listingApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyTypeRepository propertyTypeRepository;

    @Autowired
    private PropertyCategoryRepository propertyCategoryRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;

    @Autowired
    private SettingPreferenceRepository settingPreferenceRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private EmailService emailService;

    private User owner;
    private User buyer1;
    private User buyer2;
    private Listing listing;
    private UUID listingId;
    private UUID locationId;

    @BeforeEach
    void setUp() {
        Mockito.reset(emailService);

        try {
            entityManager.createNativeQuery(
                    "CREATE ALIAS IF NOT EXISTS jsonb_extract_path_text FOR \"com.sep.realvista.integration.H2JsonFunctions.jsonbExtractPathText\"").executeUpdate();
        } catch (Exception ignored) {
            // best-effort for H2 JSON helpers
        }

        seedNotificationTemplatesIfMissing();

        owner = userRepository.save(User.builder()
                .email(Email.of("owner-" + UUID.randomUUID() + "@test.local"))
                .businessName("Owner Co")
                .passwordHash("hash")
                .status(UserStatus.ACTIVE)
                .build());

        buyer1 = userRepository.save(User.builder()
                .email(Email.of("buyer1-" + UUID.randomUUID() + "@test.local"))
                .businessName("Buyer One")
                .passwordHash("hash")
                .status(UserStatus.ACTIVE)
                .build());

        buyer2 = userRepository.save(User.builder()
                .email(Email.of("buyer2-" + UUID.randomUUID() + "@test.local"))
                .businessName("Buyer Two")
                .passwordHash("hash")
                .status(UserStatus.ACTIVE)
                .build());

        Location location = locationRepository.save(Location.builder()
                .name("Test City " + UUID.randomUUID())
                .code("TC-" + UUID.randomUUID().toString().substring(0, 8))
                .type(LocationType.CITY)
                .northLat(BigDecimal.valueOf(11))
                .southLat(BigDecimal.valueOf(10))
                .eastLng(BigDecimal.valueOf(107))
                .westLng(BigDecimal.valueOf(106))
                .build());
        locationId = location.getLocationId();

        String uidSuffix = UUID.randomUUID().toString().substring(0, 8);
        PropertyCategory cat = propertyCategoryRepository.save(PropertyCategory.builder()
                .code("RES-" + uidSuffix)
                .name("Residential")
                .build());

        PropertyType apt = propertyTypeRepository.save(PropertyType.builder()
                .code("APT-" + uidSuffix)
                .name("Apartment")
                .propertyCategoryId(cat.getPropertyCategoryId())
                .propertyCategory(cat)
                .build());

        Property property = propertyRepository.save(Property.builder()
                .streetAddress("1 Test St")
                .usableSizeM2(BigDecimal.valueOf(80))
                .descriptions("d")
                .propertyTypeId(apt.getPropertyTypeId())
                .propertyType(apt)
                .latitude(BigDecimal.ZERO)
                .longitude(BigDecimal.ZERO)
                .ownerId(owner.getUserId())
                .locationId(location.getLocationId())
                .status(PropertyStatus.AVAILABLE)
                .landSizeM2(BigDecimal.ZERO)
                .widthM(BigDecimal.ZERO)
                .lengthM(BigDecimal.ZERO)
                .build());

        listing = listingRepository.save(Listing.builder()
                .propertyId(property.getPropertyId())
                .property(property)
                .userId(owner.getUserId())
                .listingType(ListingType.RENT)
                .status(ListingStatus.PUBLISHED)
                .name("Bookmarked Apt")
                .slug("bookmarked-apt-" + UUID.randomUUID())
                .price(new BigDecimal("2000.00"))
                .publishedAt(LocalDateTime.now())
                .build());

        listingId = listing.getListingId();

        bookmarkRepository.save(Bookmark.builder()
                .userId(buyer1.getUserId())
                .listingId(listingId)
                .build());
        bookmarkRepository.save(Bookmark.builder()
                .userId(buyer2.getUserId())
                .listingId(listingId)
                .build());
    }

    @AfterEach
    void tearDown() {
        if (listingId == null) {
            return;
        }
        TransactionTemplate cleanupTx = new TransactionTemplate(transactionManager);
        cleanupTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        cleanupTx.executeWithoutResult(status -> {
            entityManager.createQuery("DELETE FROM Notification n WHERE n.entityId = :lid")
                    .setParameter("lid", listingId)
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM bookmarks WHERE listing_id = ?")
                    .setParameter(1, listingId)
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM listing_price_histories WHERE listing_id = ?")
                    .setParameter(1, listingId)
                    .executeUpdate();

            listingRepository.deleteById(listingId);

            if (listing != null && listing.getPropertyId() != null) {
                propertyRepository.deleteById(listing.getPropertyId());
            }

            if (locationId != null) {
                entityManager.createNativeQuery("DELETE FROM locations WHERE location_id = ?")
                        .setParameter(1, locationId)
                        .executeUpdate();
            }

            if (buyer1 != null) {
                settingPreferenceRepository.findByUserId(buyer1.getUserId()).ifPresent(sp ->
                        entityManager.remove(entityManager.merge(sp)));
            }
            if (buyer2 != null) {
                settingPreferenceRepository.findByUserId(buyer2.getUserId()).ifPresent(sp ->
                        entityManager.remove(entityManager.merge(sp)));
            }

            if (buyer1 != null) {
                userRepository.deleteById(buyer1.getUserId());
            }
            if (buyer2 != null) {
                userRepository.deleteById(buyer2.getUserId());
            }
            if (owner != null) {
                userRepository.deleteById(owner.getUserId());
            }
        });
    }

    @Test
    @DisplayName("Price drop notifies two bookmark users in-app and schedules two emails")
    void priceDrop_notifiesBookmarkUsers() throws Exception {
        updateListingInNewTransaction(listingId, new BigDecimal("1800.00"), owner.getUserId());

        awaitNotificationCount(2, 5000);

        verify(emailService, timeout(5000).times(2)).sendDbTemplateMessageAsync(
                any(String.class), eq("PRICE_DROP_EMAIL"), eq("vi"), anyMap());
    }

    @Test
    @DisplayName("Same price does not trigger bookmark price notifications")
    void samePrice_skipsNotifications() throws Exception {
        updateListingInNewTransaction(listingId, new BigDecimal("2000.00"), owner.getUserId());

        Thread.sleep(300);
        Long count = countPriceChangeNotifications();
        assertThat(count).isZero();
        verify(emailService, never()).sendDbTemplateMessageAsync(any(), any(), any(), anyMap());
    }

    @Test
    @DisplayName("User with email disabled still gets in-app row but no email")
    void emailDisabled_skipsEmailOnly() throws Exception {
        settingPreferenceRepository.save(SettingPreference.builder()
                .userId(buyer2.getUserId())
                .emailEnabled(false)
                .inAppEnabled(true)
                .pushEnabled(false)
                .preferredLanguage("vi")
                .build());

        updateListingInNewTransaction(listingId, new BigDecimal("1700.00"), owner.getUserId());

        awaitNotificationCount(2, 5000);

        verify(emailService, timeout(5000).times(1)).sendDbTemplateMessageAsync(
                eq(buyer1.getEmail().getValue()), eq("PRICE_DROP_EMAIL"), eq("vi"), anyMap());
        verify(emailService, never()).sendDbTemplateMessageAsync(
                eq(buyer2.getEmail().getValue()), any(), any(), any());
    }

    private void updateListingInNewTransaction(UUID targetListingId, BigDecimal newPrice, UUID ownerUserId) {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tt.executeWithoutResult(status -> listingApplicationService.updateListing(targetListingId,
                UpdateListingRequest.builder().price(newPrice).build(),
                ownerUserId));
    }

    private long countPriceChangeNotifications() {
        return entityManager.createQuery(
                        "SELECT COUNT(n) FROM Notification n WHERE n.entityId = :lid AND n.eventType = :et",
                        Long.class)
                .setParameter("lid", listingId)
                .setParameter("et", EventType.PRICE_CHANGE)
                .getSingleResult();
    }

    private void awaitNotificationCount(int expected, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (countPriceChangeNotifications() >= expected) {
                assertThat(countPriceChangeNotifications()).isEqualTo(expected);
                return;
            }
            TimeUnit.MILLISECONDS.sleep(50);
        }
        fail(String.format("Timed out waiting for %d PRICE_CHANGE notifications (got %d)",
                expected, countPriceChangeNotifications()));
    }

    private void seedNotificationTemplatesIfMissing() {
        upsertTemplate("PRICE_DROP", "Price Drop", "IN_APP", "vi",
                "Giá đã giảm: {{listingName}}",
                "BD {{listingName}} {{oldPrice}} {{newPrice}} {{percent}} {{diff}}");
        upsertTemplate("PRICE_DROP_EMAIL", "Price Drop Email", "EMAIL", "vi",
                "Tin vui {{listingName}}",
                "Body {{listingName}} {{oldPrice}} {{newPrice}} {{percent}} {{diff}}");
        upsertTemplate("PRICE_INCREASE", "Price Increase", "IN_APP", "vi",
                "Giá tăng {{listingName}}",
                "BD {{listingName}} {{oldPrice}} {{newPrice}} {{percent}} {{diff}}");
        upsertTemplate("PRICE_INCREASE_EMAIL", "Price Increase Email", "EMAIL", "vi",
                "Giá tăng mail {{listingName}}",
                "Body {{listingName}} {{oldPrice}} {{newPrice}} {{percent}} {{diff}}");
    }

    private void upsertTemplate(String key, String name, String type, String lang, String title, String body) {
        notificationTemplateRepository.findByTemplateKeyAndLanguage(key, lang).orElseGet(() ->
                notificationTemplateRepository.save(NotificationTemplate.builder()
                        .templateKey(key)
                        .name(name)
                        .type(type)
                        .language(lang)
                        .title(title)
                        .contentBody(body)
                        .build()));
    }
}
