package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.common.util.VietnameseCurrencyUtil;
import com.sep.realvista.application.notification.dto.DbNotificationContext;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.application.service.EmailService;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.bookmark.BookmarkRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.notification.EntityType;
import com.sep.realvista.domain.user.notification.EventType;
import com.sep.realvista.domain.user.preference.SettingPreference;
import com.sep.realvista.domain.user.preference.SettingPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Notifies users who bookmarked a listing when its price changes (in-app + optional email).
 *
 * <p>Runs synchronously inside the listing-update flow (mirrors the book-a-tour path) so that
 * WebSocket pushes happen on the request thread and reach connected clients immediately.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceChangeNotificationService {

    @Value("${spring.application.frontend.url:http://localhost:3000}")
    private String frontendBaseUrl;

    private static final String IN_APP_DROP = "PRICE_DROP";
    private static final String IN_APP_INCREASE = "PRICE_INCREASE";
    private static final String EMAIL_DROP = "PRICE_DROP_EMAIL";
    private static final String EMAIL_INCREASE = "PRICE_INCREASE_EMAIL";

    private final BookmarkRepository bookmarkRepository;
    private final ListingRepository listingRepository;
    private final NotificationApplicationService notificationApplicationService;
    private final EmailService emailService;
    private final SettingPreferenceRepository settingPreferenceRepository;
    private final UserRepository userRepository;

    /**
     * Fan-out price change notifications to every user that bookmarked the listing.
     * Each recipient is processed in a try/catch so a single failure does not break the loop.
     */
    public void notifyBookmarkUsersAboutPriceChange(
            UUID listingId,
            String listingName,
            BigDecimal oldPrice,
            BigDecimal newPrice,
            UUID actorUserId) {
        if (listingId == null || oldPrice == null || newPrice == null
                || oldPrice.compareTo(newPrice) == 0) {
            return;
        }

        boolean isDrop = newPrice.compareTo(oldPrice) < 0;
        String inAppKey = isDrop ? IN_APP_DROP : IN_APP_INCREASE;
        String emailKey = isDrop ? EMAIL_DROP : EMAIL_INCREASE;

        List<UUID> userIds = bookmarkRepository.findActiveUserIdsByListingId(listingId);
        if (userIds.isEmpty()) {
            log.debug("No bookmarks for listing {}, skipping price-change notifications", listingId);
            return;
        }

        // Resolve slug once (used both in URL and notification metadata for FE deep linking)
        String slug = listingRepository.findById(listingId)
                .map(Listing::getSlug)
                .orElse(null);

        String resolvedName = listingName != null && !listingName.isBlank() ? listingName : "Listing";

        for (UUID userId : userIds) {
            try {
                SettingPreference prefs = settingPreferenceRepository.findByUserId(userId).orElse(null);
                String langRaw = prefs != null ? prefs.getPreferredLanguage() : "vi";
                final String lang = (langRaw == null || langRaw.isBlank()) ? "vi" : langRaw;

                final Map<String, Object> vars = buildVars(listingId, slug, resolvedName, oldPrice, newPrice, lang);

                Map<String, String> metadata = new HashMap<>();
                metadata.put("listing_id", listingId.toString());
                if (slug != null) {
                    metadata.put("listing_slug", slug);
                }

                notificationApplicationService.sendDbNotification(
                        userId,
                        inAppKey,
                        lang,
                        vars,
                        DbNotificationContext.of(EventType.PRICE_CHANGE, EntityType.LISTING, listingId, metadata));

                boolean emailOk = prefs == null || Boolean.TRUE.equals(prefs.getEmailEnabled());
                if (emailOk) {
                    userRepository.findById(userId).ifPresent(user -> {
                        String email = user.getEmail().getValue();
                        if (email != null && !email.isBlank()) {
                            emailService.sendDbTemplateMessageAsync(email, emailKey, lang, vars);
                        }
                    });
                }
            } catch (Exception e) {
                log.error("Failed price-change notify for user {} listing {}: {}",
                        userId, listingId, e.getMessage(), e);
            }
        }

        log.debug("Finished price-change notifications for listing {} (actor {})", listingId, actorUserId);
    }

    private Map<String, Object> buildVars(
            UUID listingId,
            String slug,
            String listingName,
            BigDecimal oldPrice,
            BigDecimal newPrice,
            String lang) {

        BigDecimal diffAbs = newPrice.subtract(oldPrice).abs();
        BigDecimal percentBd = BigDecimal.ZERO;
        if (oldPrice.compareTo(BigDecimal.ZERO) != 0) {
            percentBd = diffAbs.multiply(BigDecimal.valueOf(100))
                    .divide(oldPrice, 1, RoundingMode.HALF_UP);
        }
        String percent = percentBd.stripTrailingZeros().toPlainString();

        boolean vietnamese = lang != null && lang.toLowerCase(Locale.ROOT).startsWith("vi");
        String oldFormatted = formatPrice(oldPrice, vietnamese);
        String newFormatted = formatPrice(newPrice, vietnamese);
        String diffFormatted = formatPrice(diffAbs, vietnamese);

        Map<String, Object> vars = new HashMap<>();
        vars.put("listingName", listingName);
        vars.put("listingId", listingId.toString());
        vars.put("oldPrice", oldFormatted);
        vars.put("newPrice", newFormatted);
        vars.put("diff", diffFormatted);
        vars.put("percent", percent);

        String base = frontendBaseUrl != null ? frontendBaseUrl.trim() : "http://localhost:3000";
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String localeSeg = vietnamese ? "vi" : "en";
        // The public listing detail page lives at /[locale]/listing/[slug]; fall back to id-style
        // path if a slug is not yet available so the link still resolves to the listing UUID.
        String pathSegment = (slug != null && !slug.isBlank()) ? "listing/" + slug : "listing/" + listingId;
        vars.put("listingUrl", base + "/" + localeSeg + "/" + pathSegment);

        return vars;
    }

    private static String formatPrice(BigDecimal amount, boolean vietnamese) {
        if (amount == null) {
            return "";
        }
        if (vietnamese) {
            return VietnameseCurrencyUtil.formatAmount(amount) + " đ";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat formatter = new DecimalFormat("#,##0.##", symbols);
        return formatter.format(amount);
    }
}
