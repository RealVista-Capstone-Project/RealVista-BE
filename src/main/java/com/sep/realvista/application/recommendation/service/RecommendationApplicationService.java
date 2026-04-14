package com.sep.realvista.application.recommendation.service;

import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.application.recommendation.dto.AiRecommendationResult;
import com.sep.realvista.application.recommendation.dto.RecommendationResponse;
import com.sep.realvista.application.recommendation.dto.UserBehaviorRequest;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.infrastructure.external.ai.AiServiceClient;
import com.sep.realvista.infrastructure.persistence.property.attribute.PropertyAttributeValueJpaRepository;
import com.sep.realvista.domain.profile.repository.SavedSearchRepository;
import com.sep.realvista.application.profile.mapper.SavedSearchMapper;
import com.sep.realvista.domain.profile.repository.CustomerProfileRepository;
import com.sep.realvista.domain.profile.CustomerProfile;
import com.sep.realvista.application.profile.dto.SavedSearchDto;
import com.sep.realvista.domain.listing.bookmark.BookmarkRepository;
import com.sep.realvista.domain.billing.boost.repository.ListingBoostRepository;
import com.sep.realvista.domain.billing.boost.ListingBoost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecommendationApplicationService {

    private final AiServiceClient aiServiceClient;
    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;
    private final PropertyAttributeValueJpaRepository propertyAttributeValueRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final SavedSearchMapper savedSearchMapper;
    private final CustomerProfileRepository customerProfileRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ListingBoostRepository listingBoostRepository;

    private final ConcurrentHashMap<String, AtomicInteger> userEventCounters =
            new ConcurrentHashMap<>();

    @Value("${realvista.recommendation.threshold:2}")
    private int metricsThreshold;

    @Value("${realvista.recommendation.default-limit:10}")
    private int defaultLimit;

    public boolean ingestBehavior(UserBehaviorRequest request,
                                  String userId,
                                  String userName,
                                  String userRoles) {
        log.info("Ingesting {} behavior events for user {}",
                request.getEvents().size(), userId);

        String trackingId = userId;
        boolean success = aiServiceClient.ingestBehavior(
                request, trackingId, userName, userRoles);

        if (success) {
            userEventCounters
                    .computeIfAbsent(userId, k -> new AtomicInteger(0))
                    .addAndGet(request.getEvents().size());
        }

        return success;
    }

    public RecommendationResponse getRecommendations(String userId,
                                                     Integer limit,
                                                     String userName,
                                                     String userRoles,
                                                     ListingType listingType) {
        int effectiveLimit = (limit != null && limit > 0) ? limit : defaultLimit;

        UUID parsedUserId;
        try {
            parsedUserId = UUID.fromString(userId);
        } catch (Exception e) {
            parsedUserId = null;
        }

        UUID profileId = null;
        if (parsedUserId != null) {
            profileId = customerProfileRepository.findByUserIdAndIsActiveTrueAndDeletedFalse(parsedUserId)
                    .map(CustomerProfile::getCustomerProfileId)
                    .orElse(null);
        }

        RecommendationResponse response = getCachedRecommendations(
                userId, profileId, effectiveLimit, userName, userRoles, listingType);

        return response.toBuilder()
                .recommendations(enrichWithFavoriteStatus(response.getRecommendations(), userId))
                .fromCache(true)
                .build();
    }

    @Cacheable(value = "recommendations",
            key = "#userId + ':' + (#profileId != null ? #profileId : 'NO_PROFILE') "
                    + " + ':' + (#listingType != null ? #listingType.name() : 'ANY')")
    public RecommendationResponse getCachedRecommendations(String userId,
                                                           UUID profileId,
                                                           int limit,
                                                           String userName,
                                                           String userRoles,
                                                           ListingType listingType) {
        RecommendationResponse response = fetchAndEnrichRecommendations(
                userId, profileId, limit, userName, userRoles, listingType);
        response.setFromCache(false);
        return response;
    }

    @CacheEvict(value = "recommendations",
            key = "#userId + ':' + '*' + ':' + (#listingType != null ? #listingType.name() : 'ANY')")
    public RecommendationResponse refreshRecommendations(String userId,
                                                        Integer limit,
                                                        String userName,
                                                        String userRoles,
                                                        ListingType listingType) {
        UUID profileId = null;
        try {
            profileId = customerProfileRepository.findByUserIdAndIsActiveTrueAndDeletedFalse(UUID.fromString(userId))
                    .map(CustomerProfile::getCustomerProfileId)
                    .orElse(null);
        } catch (Exception ignored) { }

        userEventCounters.remove(userId);
        int effectiveLimit = (limit != null && limit > 0) ? limit : defaultLimit;

        RecommendationResponse response = fetchAndEnrichRecommendations(
                userId, profileId, effectiveLimit, userName, userRoles, listingType);
        
        log.info("AI Service returned fresh recommendations for user {}: {} items", 
                userId, response.getRecommendations().size());
        
        return response.toBuilder()
                .recommendations(enrichWithFavoriteStatus(response.getRecommendations(), userId))
                .fromCache(false)
                .build();
    }

    public boolean isThresholdMet(String userId) {
        AtomicInteger counter = userEventCounters.get(userId);
        return counter != null && counter.get() >= metricsThreshold;
    }

    public int getEventCount(String userId) {
        AtomicInteger counter = userEventCounters.get(userId);
        return counter != null ? counter.get() : 0;
    }

    private RecommendationResponse fetchAndEnrichRecommendations(String userId,
                                                                 UUID profileId,
                                                                 int limit,
                                                                 String userName,
                                                                 String userRoles,
                                                                 ListingType listingType) {

        List<SavedSearchDto> preferences = Collections.emptyList();
        String profileName = "Default";
        if (profileId != null) {
            customerProfileRepository.findById(profileId).ifPresent(p -> {
                // If the profile exists, get its name (e.g., 'Studio')
                // This will be passed to AI as a semantic hint
            });
            var profileOpt = customerProfileRepository.findById(profileId);
            if (profileOpt.isPresent()) {
                profileName = profileOpt.get().getProfileName();
            }

            preferences = savedSearchRepository
                    .findByProfileIdAndIsRecommendationTrueAndDeletedFalse(profileId)
                    .stream()
                    .map(savedSearchMapper::toDto)
                    .collect(Collectors.toList());
            log.info("Found {} preference(s) for active profile '{}' ({})", 
                    preferences.size(), profileName, profileId);
        } else {
            log.warn("No active profile found for user {}, skipping preferences", userId);
        }

        AiRecommendationResult aiResult = aiServiceClient.getRecommendations(
                userId, limit, userName, userRoles, listingType, preferences, profileName);

        if (aiResult == null || aiResult.getRecommendations() == null || aiResult.getRecommendations().isEmpty()) {
            return RecommendationResponse.builder()
                    .userId(userId)
                    .recommendations(Collections.emptyList())
                    .generatedAt(java.time.Instant.now().toString())
                    .behaviorSummary("No behavior data available")
                    .fromCache(false)
                    .build();
        }

        List<UUID> listingIds = aiResult.getRecommendations().stream()
                .map(r -> {
                    try {
                        return UUID.fromString(r.getListingId());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<UUID, Listing> listingMap = listingRepository.findAllById(listingIds).stream()
                .collect(Collectors.toMap(Listing::getListingId, l -> l));

        Map<UUID, List<ListingBoost>> activeBoostsByListingId = listingBoostRepository
                .findAllActiveByListingIds(listingIds, LocalDate.now())
                .stream()
                .collect(Collectors.groupingBy(ListingBoost::getListingId));

        List<RecommendationResponse.RecommendedListingDTO> enrichedListings = new ArrayList<>();
        for (AiRecommendationResult.AiRecommendedListing aiRec : aiResult.getRecommendations()) {
            RecommendationResponse.RecommendedListingDTO dto = mapToRecommendedListingDTO(
                    aiRec, listingMap, listingType, Collections.emptySet(), activeBoostsByListingId);
            if (dto != null) {
                enrichedListings.add(dto);
            }
        }

        return RecommendationResponse.builder()
                .userId(userId)
                .recommendations(enrichedListings)
                .generatedAt(aiResult.getGeneratedAt())
                .behaviorSummary(aiResult.getBehaviorSummary())
                .fromCache(false)
                .build();
    }

    private RecommendationResponse.RecommendedListingDTO mapToRecommendedListingDTO(
            AiRecommendationResult.AiRecommendedListing aiRec,
            Map<UUID, Listing> listingMap,
            ListingType listingType,
            Set<UUID> bookmarkedIds,
            Map<UUID, List<ListingBoost>> activeBoostsByListingId) {
        
        UUID lid;
        try {
            lid = UUID.fromString(aiRec.getListingId());
        } catch (Exception e) {
            return null;
        }

        Listing listing = listingMap.get(lid);
        if (listing == null) {
            return null;
        }
        if (listingType != null && listing.getListingType() != listingType) {
            return null;
        }

        com.sep.realvista.application.listing.dto.ListingSearchResponse searchRes =
                listingMapper.toSearchResponse(listing);

        RecommendationResponse.RecommendedListingDTO.RecommendedListingDTOBuilder<?, ?> builder = 
                RecommendationResponse.RecommendedListingDTO.builder()
                .listingId(searchRes.getListingId())
                .name(searchRes.getName())
                .slug(searchRes.getSlug())
                .listingType(searchRes.getListingType())
                .status(searchRes.getStatus())
                .price(searchRes.getPrice())
                .area(searchRes.getArea())
                .publishedAt(searchRes.getPublishedAt())
                .userType(searchRes.getUserType())
                .isFavorite(bookmarkedIds.contains(lid))
                .isBoosted(activeBoostsByListingId.containsKey(lid))
                .boostPackages(activeBoostsByListingId.getOrDefault(lid, Collections.emptyList())
                        .stream()
                        .map(b -> b.getBoostType().name())
                        .collect(Collectors.toList()))
                .reason(aiRec.getReason())
                .score(aiRec.getScore());

        if (listing.getProperty() != null) {
            builder.streetAddress(listing.getProperty().getStreetAddress());
            Location loc = listing.getProperty().getLocation();
            while (loc != null) {
                switch (loc.getType()) {
                    case CITY:
                        builder.cityName(loc.getName());
                        break;
                    case DISTRICT:
                        builder.districtName(loc.getName());
                        break;
                    case WARD:
                        builder.wardName(loc.getName());
                        break;
                    default:
                        break;
                }
                loc = loc.getParent();
            }
        }

        var thumbnailArr = listingRepository.findThumbnailByListingId(lid);
        builder.thumbnail(thumbnailArr.orElse(null));

        List<PropertyAttributeValue> attrs = propertyAttributeValueRepository
                .findByPropertyIdWithAttribute(listing.getPropertyId());
        builder.attributes(listingMapper.toAttributeList(attrs));

        return builder.build();
    }

    private List<RecommendationResponse.RecommendedListingDTO> enrichWithFavoriteStatus(
            List<RecommendationResponse.RecommendedListingDTO> recommendations, String userId) {
        if (recommendations == null || recommendations.isEmpty() || userId == null) {
            return recommendations;
        }

        UUID parsedUserId;
        try {
            parsedUserId = UUID.fromString(userId);
        } catch (Exception e) {
            return recommendations;
        }

        List<UUID> listingIds = recommendations.stream()
                .map(RecommendationResponse.RecommendedListingDTO::getListingId)
                .collect(Collectors.toList());

        Set<UUID> bookmarkedIds = bookmarkRepository.findBookmarkedListingIds(parsedUserId, listingIds);

        List<RecommendationResponse.RecommendedListingDTO> result = new ArrayList<>();
        for (RecommendationResponse.RecommendedListingDTO dto : recommendations) {
            result.add(dto.toBuilder()
                    .isFavorite(bookmarkedIds.contains(dto.getListingId()))
                    .build());
        }
        return result;
    }
}
