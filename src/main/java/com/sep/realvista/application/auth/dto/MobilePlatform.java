package com.sep.realvista.application.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum representing mobile platforms for Google authentication.
 * <p>
 * Different platforms require different Google OAuth2 client IDs:
 * - ANDROID: Uses Android OAuth client ID
 * - IOS: Uses iOS OAuth client ID
 */
@Schema(description = "Mobile platform type")
public enum MobilePlatform {

    @Schema(description = "Android platform")
    ANDROID,

    @Schema(description = "iOS platform")
    IOS;

    /**
     * Jackson deserializer for JSON to enum conversion.
     * Converts a string to MobilePlatform enum with proper validation.
     * Case-insensitive comparison.
     *
     * @param platform the platform string from JSON
     * @return MobilePlatform enum value
     * @throws IllegalArgumentException if platform is invalid (results in 400 Bad Request)
     */
    @JsonCreator
    public static MobilePlatform fromString(String platform) {
        if (platform == null || platform.isBlank()) {
            throw new IllegalArgumentException("Platform cannot be null or empty");
        }

        try {
            return MobilePlatform.valueOf(platform.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid platform: " + platform + ". Must be either 'android' or 'ios'"
            );
        }
    }

    /**
     * Jackson serializer for enum to JSON conversion.
     *
     * @return the enum name in lowercase
     */
    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }
}
