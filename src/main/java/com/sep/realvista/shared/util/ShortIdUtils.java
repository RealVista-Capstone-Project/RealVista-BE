package com.sep.realvista.shared.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Utility class for generating short IDs from UUIDs
 * Converts UUID (36 chars) to short alphanumeric string (11-12 chars)
 * Uses Base62 encoding for URL-safe, compact representation
 */
public final class ShortIdUtils {

    private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final BigInteger BASE = BigInteger.valueOf(62);

    private ShortIdUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Convert UUID to short ID (Base62 encoded)
     * Example: 550e8400-e29b-41d4-a716-446655440000 → 2qLn4Z8XooP
     *
     * @param uuid UUID to convert
     * @return Short ID string (11-12 characters)
     */
    public static String toShortId(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }

        // Convert UUID to BigInteger
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        BigInteger number = new BigInteger(1, bb.array());

        // Encode to Base62
        StringBuilder result = new StringBuilder();
        while (number.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divmod = number.divideAndRemainder(BASE);
            number = divmod[0];
            result.insert(0, BASE62_ALPHABET.charAt(divmod[1].intValue()));
        }

        return result.length() > 0 ? result.toString() : "0";
    }

    /**
     * Convert short ID back to UUID
     * Example: 2qLn4Z8XooP → 550e8400-e29b-41d4-a716-446655440000
     *
     * @param shortId Short ID string
     * @return UUID
     * @throws IllegalArgumentException if shortId is invalid
     */
    public static UUID fromShortId(String shortId) {
        if (shortId == null || shortId.isBlank()) {
            throw new IllegalArgumentException("Short ID cannot be empty");
        }

        // Decode Base62 to BigInteger
        BigInteger number = BigInteger.ZERO;
        for (char c : shortId.toCharArray()) {
            int digit = BASE62_ALPHABET.indexOf(c);
            if (digit < 0) {
                throw new IllegalArgumentException("Invalid character in short ID: " + c);
            }
            number = number.multiply(BASE).add(BigInteger.valueOf(digit));
        }

        // Convert BigInteger to UUID
        byte[] bytes = number.toByteArray();
        byte[] uuid128 = new byte[16];

        // Handle leading zeros
        int copyStart = Math.max(0, bytes.length - 16);
        int copyLength = Math.min(bytes.length, 16);
        int destStart = 16 - copyLength;
        System.arraycopy(bytes, copyStart, uuid128, destStart, copyLength);

        ByteBuffer bb = ByteBuffer.wrap(uuid128);
        long mostSigBits = bb.getLong();
        long leastSigBits = bb.getLong();

        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     * Generate listing slug: {slugified-name}-{short-id}
     * Example: luxury-2-bedroom-apartment-2qLn4Z8XooP
     *
     * @param name Listing name
     * @param uuid Listing UUID
     * @return Slug string
     */
    public static String generateSlug(String name, UUID uuid) {
        String slugifiedName = SlugUtils.slugify(name);
        String shortId = toShortId(uuid);
        return slugifiedName + "-" + shortId;
    }

    /**
     * Extract UUID from slug format
     * Slug format: {slugified-name}-{short-id}
     * Example: luxury-2-bedroom-apartment-2qLn4Z8XooP → UUID
     *
     * @param slug Slug to extract UUID from
     * @return UUID extracted from slug
     * @throws IllegalArgumentException if slug format is invalid
     */
    public static UUID extractUuidFromSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("Slug cannot be empty");
        }

        // Extract short ID part (after last hyphen)
        int lastHyphenIndex = slug.lastIndexOf('-');
        if (lastHyphenIndex == -1 || lastHyphenIndex == slug.length() - 1) {
            throw new IllegalArgumentException("Invalid slug format: " + slug);
        }

        String shortId = slug.substring(lastHyphenIndex + 1);
        return fromShortId(shortId);
    }
}
