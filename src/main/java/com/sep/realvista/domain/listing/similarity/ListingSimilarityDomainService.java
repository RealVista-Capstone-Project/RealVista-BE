package com.sep.realvista.domain.listing.similarity;

import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Domain Service for calculating similarity between listings.
 * Provides business logic for scoring listings based on:
 * - Property Type (30%)
 * - Location (15%): same location=100%, same parent location=50%
 * - Price Range (25%)
 * - Area (15%)
 * - Common Attributes (15%)
 */
@Service
@Slf4j
public class ListingSimilarityDomainService {

    private static final double PROPERTY_TYPE_WEIGHT = 0.30;
    private static final double LOCATION_WEIGHT = 0.15;
    private static final double PRICE_WEIGHT = 0.25;
    private static final double AREA_WEIGHT = 0.15;
    private static final double ATTRIBUTE_WEIGHT = 0.15;

    // Price tolerance thresholds
    private static final double PRICE_EXACT_MATCH_THRESHOLD = 0.20;  // ±20%
    private static final double PRICE_CLOSE_MATCH_THRESHOLD = 0.30;  // ±30%
    private static final double PRICE_ACCEPTABLE_THRESHOLD = 0.40;  // ±40%

    // Area tolerance thresholds
    private static final double AREA_EXACT_MATCH_THRESHOLD = 0.15;   // ±15%
    private static final double AREA_CLOSE_MATCH_THRESHOLD = 0.25;   // ±25%
    private static final double AREA_ACCEPTABLE_THRESHOLD = 0.40;    // ±40%

    /**
     * Calculate similarity score between two listings.
     *
     * @param current the reference listing context
     * @param candidate the candidate listing context
     * @return similarity score between 0.0 and 1.0
     */
    public SimilarityScore calculateSimilarity(SimilarityContext current, SimilarityContext candidate) {

        // Property Type Score (30%)
        double propertyTypeScore = calculatePropertyTypeScore(
                current.getPropertyTypeId(),
                candidate.getPropertyTypeId());

        // Location Score (15%)
        double locationScore = calculateLocationScore(
                current.getLocationId(),
                candidate.getLocationId(),
                current.getParentLocationId(),
                candidate.getParentLocationId());

        // Price Score (25%)
        double priceScore = calculatePriceScore(
                current.getPrice(),
                candidate.getPrice());

        // Area Score (15%)
        double areaScore = calculateAreaScore(
                current.getArea(),
                candidate.getArea());

        // Common Attributes Score (15%)
        double attributeScore = calculateAttributeSimilarityScore(
                current.getAttributes(),
                candidate.getAttributes());

        // Calculate weighted total
        double totalScore = (propertyTypeScore * PROPERTY_TYPE_WEIGHT)
                + (locationScore * LOCATION_WEIGHT)
                + (priceScore * PRICE_WEIGHT)
                + (areaScore * AREA_WEIGHT)
                + (attributeScore * ATTRIBUTE_WEIGHT);

        log.debug("Similarity calculation - Type: {}, Location: {}, Price: {}, Area: {}, Attr: {}, Total: {}",
                propertyTypeScore, locationScore, priceScore, areaScore, attributeScore, totalScore);

        return SimilarityScore.builder()
                .propertyTypeScore(propertyTypeScore)
                .locationScore(locationScore)
                .priceScore(priceScore)
                .areaScore(areaScore)
                .attributeScore(attributeScore)
                .totalScore(totalScore)
                .build();
    }

    /**
     * Calculate property type similarity score.
     * Exact match = 1.0, different types = 0.0
     */
    private double calculatePropertyTypeScore(UUID currentTypeId, UUID candidateTypeId) {
        return currentTypeId.equals(candidateTypeId) ? 1.0 : 0.0;
    }

    /**
     * Calculate location similarity score.
     * - Same location (exact match): 1.0
     * - Same parent location (same district/area): 0.5
     * - Different location: 0.0
     */
    private double calculateLocationScore(
            UUID currentLocationId,
            UUID candidateLocationId,
            UUID currentParentLocationId,
            UUID candidateParentLocationId) {

        // Exact location match
        if (currentLocationId != null && currentLocationId.equals(candidateLocationId)) {
            return 1.0;
        }

        // Same parent location (same district/area)
        if (currentParentLocationId != null
                && currentParentLocationId.equals(candidateParentLocationId)) {
            return 0.5;
        }

        return 0.0;
    }

    /**
     * Calculate price similarity score based on percentage difference.
     * - Within ±20%: 1.0
     * - Within ±30%: 0.7
     * - Within ±40%: 0.4
     * - Beyond ±40%: 0.0
     *
     * Note: NULL price (missing data) is treated differently from zero price (free).
     * - If either price is NULL: return 0.0 (cannot compare missing data)
     * - If both prices are zero: return 1.0 (both are free)
     * - Otherwise: calculate percentage difference
     */
    private double calculatePriceScore(BigDecimal currentPrice, BigDecimal candidatePrice) {
        // Missing price data - cannot compare
        if (currentPrice == null || candidatePrice == null) {
            return 0.0;
        }

        // Both are free (price = 0) - perfect match
        if (currentPrice.compareTo(BigDecimal.ZERO) == 0
                && candidatePrice.compareTo(BigDecimal.ZERO) == 0) {
            return 1.0;
        }

        // One is free, one is not - calculate based on the non-zero price
        if (currentPrice.compareTo(BigDecimal.ZERO) == 0
                || candidatePrice.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0; // Free vs paid is not similar
        }

        double percentageDiff = calculatePercentageDifference(currentPrice, candidatePrice);

        if (percentageDiff <= PRICE_EXACT_MATCH_THRESHOLD) {
            return 1.0;
        } else if (percentageDiff <= PRICE_CLOSE_MATCH_THRESHOLD) {
            return 0.7;
        } else if (percentageDiff <= PRICE_ACCEPTABLE_THRESHOLD) {
            return 0.4;
        } else {
            return 0.0;
        }
    }

    /**
     * Calculate area similarity score based on percentage difference.
     * - Within ±15%: 1.0
     * - Within ±25%: 0.6
     * - Within ±40%: 0.3
     * - Beyond ±40%: 0.0
     */
    private double calculateAreaScore(BigDecimal currentArea, BigDecimal candidateArea) {
        if (currentArea == null || candidateArea == null) {
            return 0.0;
        }

        double percentageDiff = calculatePercentageDifference(currentArea, candidateArea);

        if (percentageDiff <= AREA_EXACT_MATCH_THRESHOLD) {
            return 1.0;
        } else if (percentageDiff <= AREA_CLOSE_MATCH_THRESHOLD) {
            return 0.6;
        } else if (percentageDiff <= AREA_ACCEPTABLE_THRESHOLD) {
            return 0.3;
        } else {
            return 0.0;
        }
    }

    /**
     * Calculate attribute similarity score by comparing common NUMBER-type attributes.
     * Returns the ratio of similar attributes to total common attributes.
     */
    private double calculateAttributeSimilarityScore(
            List<PropertyAttributeValue> currentAttributes,
            List<PropertyAttributeValue> candidateAttributes) {

        // Filter only NUMBER-type attributes and create maps
        Map<UUID, BigDecimal> currentNumberAttrs = currentAttributes.stream()
                .filter(attr -> attr.getValueNumber() != null)
                .collect(Collectors.toMap(
                        PropertyAttributeValue::getPropertyAttributeId,
                        PropertyAttributeValue::getValueNumber
                ));

        Map<UUID, BigDecimal> candidateNumberAttrs = candidateAttributes.stream()
                .filter(attr -> attr.getValueNumber() != null)
                .collect(Collectors.toMap(
                        PropertyAttributeValue::getPropertyAttributeId,
                        PropertyAttributeValue::getValueNumber
                ));

        // Find common attributes (intersection)
        Map<UUID, BigDecimal> commonAttrs = currentNumberAttrs.entrySet().stream()
                .filter(entry -> candidateNumberAttrs.containsKey(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        if (commonAttrs.isEmpty()) {
            return 0.0;
        }

        // Count similar attributes
        long similarCount = commonAttrs.entrySet().stream()
                .filter(entry -> {
                    BigDecimal currentValue = entry.getValue();
                    BigDecimal candidateValue = candidateNumberAttrs.get(entry.getKey());
                    return areAttributesSimilar(currentValue, candidateValue);
                })
                .count();

        return (double) similarCount / commonAttrs.size();
    }

    /**
     * Determine if two attribute values are similar.
     * For small numbers (≤10): exact match or ±1
     * For larger numbers: within ±20%
     */
    private boolean areAttributesSimilar(BigDecimal current, BigDecimal candidate) {
        if (current == null || candidate == null) {
            return false;
        }

        // For small numbers like bedrooms, bathrooms
        if (current.compareTo(BigDecimal.valueOf(10)) <= 0) {
            BigDecimal diff = current.subtract(candidate).abs();
            return diff.compareTo(BigDecimal.ONE) <= 0;
        }

        // For measurements like area, width, depth
        double percentageDiff = calculatePercentageDifference(current, candidate);
        return percentageDiff <= 0.20; // ±20%
    }

    /**
     * Calculate percentage difference between two values.
     * Returns the absolute difference as a percentage of the average.
     */
    private double calculatePercentageDifference(BigDecimal value1, BigDecimal value2) {
        BigDecimal avg = value1.add(value2).divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
        BigDecimal diff = value1.subtract(value2).abs();

        if (avg.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        return diff.divide(avg, 4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Value object representing similarity scores.
     */
    @lombok.Builder
    @lombok.Data
    public static class SimilarityScore {
        private double propertyTypeScore;
        private double locationScore;
        private double priceScore;
        private double areaScore;
        private double attributeScore;
        private double totalScore;

        /**
         * Get total score as percentage (0-100).
         */
        public int getScorePercentage() {
            return (int) Math.round(totalScore * 100);
        }
    }

    /**
     * Context object containing all data needed for similarity calculation.
     * Reduces parameter count to comply with checkstyle rules.
     */
    @lombok.Builder
    @lombok.Data
    public static class SimilarityContext {
        private UUID propertyTypeId;
        private UUID locationId;
        private UUID parentLocationId;
        private BigDecimal price;
        private BigDecimal area;
        private List<PropertyAttributeValue> attributes;
    }
}
