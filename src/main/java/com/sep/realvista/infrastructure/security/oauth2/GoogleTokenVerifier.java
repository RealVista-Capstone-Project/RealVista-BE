package com.sep.realvista.infrastructure.security.oauth2;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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
 * - Mobile Client ID (for React Native and other mobile apps)
 */
@Service
@Slf4j
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;
    private final String webClientId;
    private final String mobileClientId;

    public GoogleTokenVerifier(
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String webClientId,
            @Value("${spring.security.oauth2.mobile.google.client-id}") String mobileClientId
    ) {
        this.webClientId = webClientId;
        this.mobileClientId = mobileClientId;

        // Support both web and mobile client IDs for token verification
        List<String> clientIds = Arrays.asList(webClientId, mobileClientId);

        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(clientIds)
                .build();

        log.info("GoogleTokenVerifier initialized with web and mobile client IDs");
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

    /**
     * Gets the configured web client ID.
     *
     * @return web client ID
     */
    public String getWebClientId() {
        return webClientId;
    }

    /**
     * Gets the configured mobile client ID.
     *
     * @return mobile client ID
     */
    public String getMobileClientId() {
        return mobileClientId;
    }
}
