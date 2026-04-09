package com.sep.realvista.application.service;

/**
 * OTP service for generating, storing, and verifying one-time passwords.
 */
public interface OtpService {

    /**
     * Generate a 6-digit OTP and store it with a TTL of {@code expiryMinutes}.
     *
     * @param key           unique key (e.g. "email-otp:userId")
     * @param expiryMinutes how long the OTP is valid
     * @return the generated OTP string
     */
    String generateAndStore(String key, int expiryMinutes);

    /**
     * Store an arbitrary string value with a TTL (for pending data like new email).
     */
    void store(String key, String value, int expiryMinutes);

    /**
     * Retrieve a stored value, or null if not found / expired.
     */
    String get(String key);

    /**
     * Remove a stored entry.
     */
    void remove(String key);

    /**
     * Verify the OTP for the given key.
     * The entry is removed on a successful match.
     *
     * @param key unique key
     * @param otp value submitted by the user
     * @return true if valid and not expired
     */
    boolean verify(String key, String otp);

    /**
     * Returns the remaining TTL in seconds, or -1 if not found / expired.
     */
    long remainingSeconds(String key);
}
