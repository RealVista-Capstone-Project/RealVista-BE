package com.sep.realvista.shared.util;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility class for generating SEO-friendly slugs
 * Format: {slugified-name}-i.{uuid} (legacy) or used by ShortIdUtils for modern format
 * Example: luxury-2-bedroom-apartment-i.610e8400-e29b-41d4-a716-446655440001
 */
public final class SlugUtils {

    private static final Pattern SLUG_PATTERN = Pattern.compile("[^a-z0-9\\s-]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern MULTIPLE_HYPHENS_PATTERN = Pattern.compile("-+");

    private SlugUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Slugify a string:
     * 1. Convert to lowercase
     * 2. Remove Vietnamese accents
     * 3. Remove special characters (keep letters, numbers, spaces, hyphens)
     * 4. Replace spaces with hyphens
     * 5. Replace multiple hyphens with single hyphen
     * 6. Trim leading/trailing hyphens
     *
     * @param input String to slugify
     * @return Slugified string
     */
    public static String slugify(String input) {
        if (input == null || input.isBlank()) {
            return "property";
        }

        // Limit length to 50 chars for SEO-friendly URLs
        String limited = input.length() > 50 ? input.substring(0, 50) : input;

        // Convert to lowercase
        String lowercase = limited.toLowerCase();

        // Remove Vietnamese accents using custom mapping
        String unaccented = removeVietnameseAccents(lowercase);

        // Remove special characters (keep alphanumeric, spaces, hyphens)
        String cleaned = SLUG_PATTERN.matcher(unaccented).replaceAll("");

        // Replace whitespace with hyphens
        String hyphenated = WHITESPACE_PATTERN.matcher(cleaned).replaceAll("-");

        // Replace multiple hyphens with single hyphen
        String normalized = MULTIPLE_HYPHENS_PATTERN.matcher(hyphenated).replaceAll("-");

        // Trim leading/trailing hyphens
        String trimmed = normalized.replaceAll("^-+|-+$", "");

        return trimmed.isEmpty() ? "property" : trimmed;
    }

    /**
     * Remove Vietnamese accents from string
     * Uses manual character mapping for better control
     */
    private static String removeVietnameseAccents(String input) {
        // Vietnamese character mapping (67 chars each)
        // a-vowels (17): á à ả ã ạ ă ắ ằ ẳ ẵ ặ â ấ ầ ẩ ẫ ậ
        // e-vowels (11): é è ẻ ẽ ẹ ê ế ề ể ễ ệ
        // i-vowels (5):  í ì ỉ ĩ ị
        // o-vowels (17): ó ò ỏ õ ọ ô ố ồ ổ ỗ ộ ơ ớ ờ ở ỡ ợ
        // u-vowels (11): ú ù ủ ũ ụ ư ứ ừ ử ữ ự
        // y-vowels (5):  ý ỳ ỷ ỹ ỵ
        // d (1):         đ
        String vietnamese = "áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ";
        String replacement = "aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd";

        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            int index = vietnamese.indexOf(c);
            if (index >= 0) {
                result.append(replacement.charAt(index));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Extract UUID from slug format (legacy)
     * Slug format: {listing-name}-i.{uuid}
     *
     * @param slug Slug to extract UUID from
     * @return UUID extracted from slug
     * @throws IllegalArgumentException if slug format is invalid
     */
    public static UUID extractUuidFromSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("Slug cannot be empty");
        }

        // Find the last occurrence of "-i."
        int separatorIndex = slug.lastIndexOf("-i.");
        if (separatorIndex == -1) {
            // Not a slug format, try to parse as direct UUID
            try {
                return UUID.fromString(slug);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid slug or UUID format: " + slug);
            }
        }

        // Extract UUID part after "-i."
        String uuidPart = slug.substring(separatorIndex + 3);
        try {
            return UUID.fromString(uuidPart);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID in slug: " + slug);
        }
    }
}
