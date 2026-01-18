package com.sep.realvista.infrastructure.security.oauth2;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.sep.realvista.application.auth.dto.MobilePlatform;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for verifying Google ID tokens from mobile and web applications.
 * <p>
 * Mobile apps use Google Sign-In SDK to obtain an ID token,
 * which is then verified by this service to authenticate the user.
 * <p>
 * This approach works for mobile because:
 * - No redirect URLs needed (avoids private IP issue)
 * - Token validation happens server-side
 * - Secure and recommended by Google for mobile apps
 * <p>
 * Supports multiple Google Client IDs:
 * - Web Client ID (for web OAuth2 flow)
 * - Android Client ID (for React Native Android)
 * - iOS Client ID (for React Native iOS)
 */
@Service
@Slf4j
@Getter
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;
    private final String webClientId;
    private final String androidClientId;
    private final String iosClientId;
    private final Map<MobilePlatform, String> platformClientIds;
    private final List<String> clientIds;

    public GoogleTokenVerifier(
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String webClientId,
            @Value("${spring.security.oauth2.mobile.google.android.client-id}") String androidClientId,
            @Value("${spring.security.oauth2.mobile.google.ios.client-id}") String iosClientId
    ) {
        this.webClientId = webClientId;
        this.androidClientId = androidClientId;
        this.iosClientId = iosClientId;

        // Map platforms to their client IDs
        this.platformClientIds = new HashMap<>();
        this.platformClientIds.put(MobilePlatform.ANDROID, androidClientId);
        this.platformClientIds.put(MobilePlatform.IOS, iosClientId);

        // Support all client IDs for token verification
        this.clientIds = Arrays.asList(webClientId, androidClientId, iosClientId);

        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(clientIds)
                .build();

        log.info("GoogleTokenVerifier initialized with web, Android, and iOS client IDs");
        log.debug("Supported client IDs count: {}", clientIds.size());
    }

    /**
     * Verifies a Google ID token and extracts user information.
     *
     * @param idTokenString the ID token from Google Sign-In SDK
     * @return GoogleIdToken.Payload containing user information
     * @throws Exception if token is invalid or verification fails
     */
    public GoogleIdToken.Payload verifyToken(String idTokenString) throws Exception {
        log.debug("Verifying Google ID token");

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null) {
            log.error("Invalid ID token: verification failed");
            throw new IllegalArgumentException("Invalid ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        // Log user info for debugging
        log.info("Token verified successfully for user: {} ({})",
                payload.getEmail(),
                payload.getSubject());

        return payload;
    }

    /**
     * Verifies a Google ID token for a specific mobile platform.
     * <p>
     * This method ensures the token was issued for the correct platform's client ID,
     * providing an additional layer of validation beyond the standard token verification.
     *
     * @param idTokenString the ID token from Google Sign-In SDK
     * @param platform      the mobile platform (ANDROID or IOS)
     * @return GoogleIdToken.Payload containing user information
     * @throws Exception if token is invalid or verification fails
     */
    public GoogleIdToken.Payload verifyTokenForPlatform(String idTokenString, MobilePlatform platform)
            throws Exception {
        log.debug("Verifying Google ID token for platform: {}", platform);

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null) {
            log.error("Invalid mobile ID token: verification failed");
            throw new IllegalArgumentException("Invalid mobile ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        // Verify the token's audience matches the expected platform's client ID
        String expectedClientId = platformClientIds.get(platform);
        String tokenAudience = (String) payload.getAudience();

        log.debug("Token audience: {}, Expected client ID for {}: {}",
                tokenAudience, platform, expectedClientId);

        // Strict audience verification: token must match the expected platform's client ID
        if (!tokenAudience.equals(expectedClientId)) {
            log.error("Token audience mismatch. Expected: {}, but got: {}",
                    expectedClientId, tokenAudience);
            throw new IllegalArgumentException(
                    "Token audience mismatch. Expected: " + expectedClientId
            );
        }

        log.info("Token verified successfully for platform {} and user: {} ({})",
                platform, payload.getEmail(), payload.getSubject());

        return payload;
    }

    /**
     * Extracts user email from the token payload.
     *
     * @param payload the verified token payload
     * @return user email
     */
    public String getEmail(GoogleIdToken.Payload payload) {
        return payload.getEmail();
    }

    /**
     * Extracts user's first name from the token payload.
     *
     * @param payload the verified token payload
     * @return user's first name or null
     */
    public String getGivenName(GoogleIdToken.Payload payload) {
        return (String) payload.get("given_name");
    }

    /**
     * Extracts user's last name from the token payload.
     *
     * @param payload the verified token payload
     * @return user's last name or null
     */
    public String getFamilyName(GoogleIdToken.Payload payload) {
        return (String) payload.get("family_name");
    }

    /**
     * Extracts user's profile picture URL from the token payload.
     *
     * @param payload the verified token payload
     * @return profile picture URL or null
     */
    public String getPictureUrl(GoogleIdToken.Payload payload) {
        return (String) payload.get("picture");
    }

}
