package com.sep.realvista.application.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.ai.dto.AiChatMessageResponse;
import com.sep.realvista.application.ai.dto.AiConversationMessagesResponse;
import com.sep.realvista.application.listing.dto.AmenityDTO;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.application.listing.dto.SimilarListingDTO;
import com.sep.realvista.application.listing.dto.SimilarListingsResponse;
import com.sep.realvista.application.listing.mapper.ListingMapper;
import com.sep.realvista.application.listing.service.ListingApplicationService;
import com.sep.realvista.domain.billing.subscription.AiFeature;
import com.sep.realvista.domain.aichat.AiConversation;
import com.sep.realvista.domain.aichat.AiConversationRepository;
import com.sep.realvista.domain.aichat.AiMessage;
import com.sep.realvista.domain.aichat.AiMessageRepository;
import com.sep.realvista.domain.aichat.AiMessageRole;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.amenity.PropertyAmenity;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.property.attribute.repository.PropertyAttributeValueRepository;
import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.domain.property.location.LocationType;
import com.sep.realvista.domain.property.repository.PropertyAmenityRepository;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages AI chat conversations with persistence.
 *
 * <p>Flow for streaming chat:
 * <ol>
 *   <li>Find-or-create the user's single AI conversation</li>
 *   <li>Persist the USER message (synchronous, within TX)</li>
 *   <li>POST to NestJS AI service with conversation threadId</li>
 *   <li>Stream SSE tokens; on complete save ASSISTANT message
 *       in a separate thread/TX via
 *       {@link AiChatPersistenceHelper}</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatApplicationService {

    private static final ParameterizedTypeReference<
            ServerSentEvent<String>> SSE_TYPE =
            new ParameterizedTypeReference<>() { };

    private final WebClient aiWebClient;
    private final AiQuotaApplicationService quotaService;
    private final ObjectMapper objectMapper;
    @Value("${realvista.ai.api-key:}")
    private String serviceApiKey;
    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;
    private final AiChatPersistenceHelper persistenceHelper;
    private final ListingRepository listingRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyAttributeValueRepository propertyAttributeValueRepository;
    private final PropertyAmenityRepository propertyAmenityRepository;
    private final ListingMapper listingMapper;
    private final ListingApplicationService listingApplicationService;

    /**
     * Stream an AI chat response, persisting both the user message
     * and the final assistant response.
     *
     * @return Flux of raw SSE strings from the AI service
     */
    @Transactional
    public Flux<String> streamChat(String message,
                                   UUID listingId,
                                   UUID userId,
                                   String userName,
                                   String userRoles) {
        log.info("AI chat request from user={}", userId);

        // 1. Check AI Quota
        if (!quotaService.checkAndIncrementQuota(userId, AiFeature.AI_ASSISTANT)) {
            return Flux.just(buildErrorEvent(
                    "Bạn đã hết lượt sử dụng AI Assistant hôm nay. Hãy mua thêm gói để tiếp tục!"));
        }

        // 2. Find or create conversation
        AiConversation conversation = conversationRepository
                .findByUserId(userId)
                .orElseGet(() -> {
                    AiConversation c = AiConversation.create(userId);
                    log.info("Created AI conversation user={}, "
                            + "threadId={}", userId, c.getThreadId());
                    return conversationRepository.save(c);
                });

        // 3. Save the USER message
        int nextSeq = messageRepository
                .countByConversationId(conversation.getId()) + 1;
        AiMessage userMsg = AiMessage.create(
                conversation.getId(), AiMessageRole.USER,
                message, nextSeq);
        messageRepository.save(userMsg);

        conversation.touchUpdatedAt();
        conversationRepository.save(conversation);

        // 4. Build request body for the AI service (enrich prompt when listing context is present)
        String prompt = message;
        if (listingId != null) {
            String listingContext = buildListingContextBlock(listingId);
            if (!listingContext.isBlank()) {
                prompt = listingContext + "\n\nCâu hỏi: " + message;
            }
            if (isCompareIntent(message)) {
                String similarListingsContext = buildSimilarListingsContextBlock(listingId, userId);
                if (!similarListingsContext.isBlank()) {
                    prompt = prompt + "\n\n" + similarListingsContext;
                }
            }
        }
        prompt = prompt + "\n\nYêu cầu bắt buộc: Luôn trả lời hoàn toàn bằng tiếng Việt.";

        Map<String, Object> body = new HashMap<>();
        body.put("prompt", prompt);
        body.put("threadId", conversation.getThreadId().toString());

        // 5. Emit start event, then stream AI tokens
        String startEvent = buildStartEvent(conversation);
        int assistantSeq = nextSeq + 1;
        UUID convId = conversation.getId();

        // Accumulate token content as fallback;
        // prefer fullResponse from the done event.
        StringBuilder tokenAccumulator = new StringBuilder();
        AtomicReference<String> doneFullResponse =
                new AtomicReference<>();
        AtomicBoolean hasError = new AtomicBoolean(false);

        Flux<String> aiStream = aiWebClient.post()
                .uri("/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .header("x-api-key", serviceApiKey)
                .header("x-user-id", userId.toString())
                .header("x-user-name",
                        userName != null ? userName : "unknown")
                .header("x-user-roles",
                        userRoles != null ? userRoles : "USER")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(SSE_TYPE)
                .mapNotNull(sse -> processSseEvent(
                        sse, tokenAccumulator,
                        doneFullResponse, hasError))
                .doOnComplete(() -> {
                    if (hasError.get()) {
                        return;
                    }
                    String content = doneFullResponse.get();
                    if (content == null || content.isBlank()) {
                        content = tokenAccumulator.toString();
                    }
                    persistenceHelper.saveAssistantMessage(
                            convId, content, assistantSeq);
                })
                .onErrorResume(
                        WebClientResponseException.class, ex -> {
                    log.error("AI service error: status={}, body={}",
                            ex.getStatusCode(),
                            ex.getResponseBodyAsString());
                    return Flux.just(buildErrorEvent(
                            "AI service error: "
                                    + ex.getStatusCode()));
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Unexpected streaming error: {}",
                            ex.getMessage(), ex);
                    return Flux.just(
                            buildErrorEvent("Internal server error"));
                });

        return Flux.concat(Flux.just(startEvent), aiStream);
    }

    /**
     * Returns all messages for the user's AI conversation.
     */
    @Transactional(readOnly = true)
    public AiConversationMessagesResponse getMessages(UUID userId) {
        AiConversation conversation = conversationRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AI conversation", userId));

        List<AiMessage> messages = messageRepository
                .findByConversationIdOrderBySequence(
                        conversation.getId());

        List<AiChatMessageResponse> dtos = messages.stream()
                .map(this::toDto)
                .toList();

        return AiConversationMessagesResponse.builder()
                .conversationId(conversation.getId())
                .threadId(conversation.getThreadId())
                .createdAt(conversation.getCreatedAt())
                .messages(dtos)
                .build();
    }

    /**
     * Deletes the user's AI conversation and notifies the AI service
     * to clean up the thread.
     */
    @Transactional
    public void deleteConversation(UUID userId,
                                   String userName,
                                   String userRoles) {
        AiConversation conversation = conversationRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AI conversation", userId));

        UUID threadId = conversation.getThreadId();

        // Hard-delete locally (CASCADE removes messages)
        conversationRepository.delete(conversation);
        log.info("Deleted AI conversation user={}, threadId={}",
                userId, threadId);

        // Fire-and-forget: tell NestJS to clean up the thread
        aiWebClient.delete()
                .uri("/ai/threads/{threadId}", threadId)
                .header("x-api-key", serviceApiKey)
                .header("x-user-id", userId.toString())
                .header("x-user-name",
                        userName != null ? userName : "unknown")
                .header("x-user-roles",
                        userRoles != null ? userRoles : "USER")
                .retrieve()
                .toBodilessEntity()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(ex -> log.warn(
                        "Failed to delete AI thread {}: {}",
                        threadId, ex.getMessage()))
                .subscribe();
    }

    // ── private helpers ─────────────────────────────────────────

    /**
     * Builds the same Vietnamese listing context block previously assembled on the FE,
     * so the NestJS agent prompt shape stays compatible with its system instructions.
     *
     * @return non-blank context, or empty string if the listing cannot be loaded
     */
    private String buildListingContextBlock(UUID listingId) {
        try {
            Optional<Listing> listingOpt = listingRepository.findById(listingId);
            if (listingOpt.isEmpty()) {
                return "";
            }
            Listing listing = listingOpt.get();
            Optional<Property> propertyOpt = propertyRepository.findById(listing.getPropertyId());
            if (propertyOpt.isEmpty()) {
                return "";
            }
            Property property = propertyOpt.get();

            List<PropertyAttributeValue> attributeValues =
                    propertyAttributeValueRepository.findByPropertyIdWithAttribute(
                            property.getPropertyId());
            List<PropertyAmenity> propertyAmenities =
                    propertyAmenityRepository.findByPropertyIdWithAmenity(
                            property.getPropertyId());

            String attributes = listingMapper.toAttributeList(attributeValues).stream()
                    .map(this::formatAttributeLine)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining("\n"));

            String amenities = listingMapper.toAmenityList(propertyAmenities).stream()
                    .map(this::formatAmenityLine)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining("\n"));

            String districtCity = formatDistrictCity(property.getLocation());
            String propertyTypeName = property.getPropertyType() != null
                    ? Objects.toString(property.getPropertyType().getName(), "")
                    : "";
            String priceStr = listing.getPrice() != null
                    ? listing.getPrice().toPlainString()
                    : "";
            String desc = property.getDescriptions() != null ? property.getDescriptions() : "";

            String land = formatAreaM2(property.getLandSizeM2());
            String usable = formatAreaM2(property.getUsableSizeM2());
            String dimensions = formatDimensionsM(property.getWidthM(), property.getLengthM());

            return String.join("\n",
                    "[THÔNG TIN BẤT ĐỘNG SẢN ĐANG XEM]",
                    "- ID: " + listing.getListingId(),
                    "- Tên: " + Objects.toString(listing.getName(), ""),
                    "- Giá: " + priceStr + " VND",
                    "- Loại hình: " + listing.getListingType().name() + " (" + propertyTypeName + ")",
                    "- Địa chỉ: " + districtCity,
                    "- Diện tích đất: " + land + " m2",
                    "- Diện tích sử dụng: " + usable + " m2",
                    "- Kích thước: " + dimensions,
                    "- Mô tả: " + desc,
                    "",
                    "[THUỘC TÍNH]",
                    attributes,
                    "",
                    "[TIỆN ÍCH]",
                    amenities
            ).trim();
        } catch (Exception ex) {
            log.warn("Failed to build listing context for listingId={}: {}",
                    listingId, ex.getMessage());
            return "";
        }
    }

    /**
     * For Template D compare intent, preload a compact similar listings block from the core service.
     * This block is additive and optional. If anything fails, we simply skip it to preserve legacy flow.
     */
    private String buildSimilarListingsContextBlock(UUID listingId, UUID userId) {
        try {
            SimilarListingsResponse response = listingApplicationService.getSimilarListings(listingId, 5, userId);
            if (response == null || response.getListings() == null || response.getListings().isEmpty()) {
                return "";
            }

            String listingLines = response.getListings().stream()
                    .limit(5)
                    .map(this::formatSimilarListingLine)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining("\n"));

            if (listingLines.isBlank()) {
                return "";
            }

            return String.join("\n",
                    "[DANH SÁCH TIN TƯƠNG TỰ TỪ HỆ THỐNG]",
                    listingLines
            );
        } catch (Exception ex) {
            log.warn("Failed to preload similar listings for listingId={}: {}",
                    listingId, ex.getMessage());
            return "";
        }
    }

    private String formatSimilarListingLine(SimilarListingDTO listing) {
        if (listing == null || listing.getListingId() == null) {
            return "";
        }
        String id = listing.getListingId().toString();
        String name = Objects.toString(listing.getName(), "");
        String price = listing.getPrice() != null ? listing.getPrice().toPlainString() + " VND" : "";
        String area = listing.getArea() != null
                ? listing.getArea().stripTrailingZeros().toPlainString() + " m2"
                : "";
        String address = Objects.toString(listing.getFullAddress(), "");
        if (address.isBlank()) {
            address = Objects.toString(listing.getLocationName(), "");
        }
        String attrs = formatSimilarListingAttributes(listing.getAttributes());
        String similarity = listing.getSimilarityScore() != null
                ? listing.getSimilarityScore() + "%"
                : "";

        return String.join(" | ",
                "- ID: " + id,
                "Tên: " + name,
                "Giá: " + price,
                "Diện tích: " + area,
                "Địa chỉ: " + address,
                "Thuộc tính: " + attrs,
                "Độ tương đồng: " + similarity
        );
    }

    private String formatSimilarListingAttributes(List<PropertyAttributeDTO> attrs) {
        if (attrs == null || attrs.isEmpty()) {
            return "";
        }
        return attrs.stream()
                .map(this::formatAttributeInline)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
    }

    private String formatAttributeInline(PropertyAttributeDTO a) {
        if (a == null || a.getAttributeName() == null) {
            return "";
        }
        String value = Objects.toString(a.getDisplayValue(), "").trim();
        if (value.isBlank()) {
            return "";
        }
        return a.getAttributeName().trim() + ": " + value;
    }

    private boolean isCompareIntent(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        String normalized = message.toLowerCase(Locale.ROOT);
        return normalized.contains("so sánh")
                || normalized.contains("so sanh")
                || normalized.contains("similar")
                || normalized.contains("compare");
    }

    private String formatAttributeLine(PropertyAttributeDTO a) {
        if (a == null || a.getAttributeName() == null) {
            return "";
        }
        String dv = a.getDisplayValue();
        return "- " + a.getAttributeName() + ": " + Objects.toString(dv, "");
    }

    private String formatAmenityLine(AmenityDTO a) {
        if (a == null || a.getAmenityName() == null) {
            return "";
        }
        return "- " + a.getAmenityName();
    }

    /**
     * Resolves district and city names from a ward (or lower) location by walking parents,
     * matching {@link ListingMapper#mapLocationInfo}.
     */
    private String formatDistrictCity(Location location) {
        if (location == null) {
            return ", ";
        }
        Map<LocationType, String> locationNames = new HashMap<>();
        Location current = location;
        while (current != null) {
            locationNames.put(current.getType(), current.getName());
            current = current.getParent();
        }
        String district = locationNames.getOrDefault(LocationType.DISTRICT, "");
        String city = locationNames.getOrDefault(LocationType.CITY, "");
        return district + ", " + city;
    }

    private static String formatAreaM2(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private static String formatDimensionsM(BigDecimal widthM, BigDecimal lengthM) {
        String w = widthM != null ? widthM.stripTrailingZeros().toPlainString() : "";
        String l = lengthM != null ? lengthM.stripTrailingZeros().toPlainString() : "";
        return w + "m x " + l + "m";
    }

    /**
     * Processes a single SSE event from the NestJS AI service
     * and returns a structured JSON string to forward to the
     * frontend, or {@code null} to skip the event.
     *
     * <p>Event types handled:
     * <ul>
     *   <li>{@code token} – extract {@code content}, accumulate,
     *       forward as {@code {"type":"token","content":"..."}}</li>
     *   <li>{@code done} – extract {@code fullResponse} for
     *       persistence, forward {@code {"type":"done"}}</li>
     *   <li>{@code tool_start/tool_end} – forward with
     *       {@code name} field for loading indicators</li>
     *   <li>{@code error} – mark error flag, forward with
     *       {@code message} field</li>
     *   <li>{@code start} – ignored (we emit our own)</li>
     * </ul>
     */
    @SuppressWarnings("ReturnCount")
    private String processSseEvent(
            ServerSentEvent<String> sse,
            StringBuilder tokenAccumulator,
            AtomicReference<String> doneFullResponse,
            AtomicBoolean hasError) {
        String event = sse.event();
        String data = sse.data();

        if (event == null || data == null) {
            return null;
        }

        try {
            JsonNode node = objectMapper.readTree(data);
            return switch (event) {
                case "token" -> handleTokenEvent(
                        node, tokenAccumulator);
                case "done" -> handleDoneEvent(
                        node, doneFullResponse);
                case "tool_start", "tool_end" ->
                        handleToolEvent(event, node);
                case "error" -> handleErrorEvent(
                        node, hasError);
                default -> {
                    log.debug("Ignoring SSE event: {}", event);
                    yield null;
                }
            };
        } catch (JsonProcessingException ex) {
            log.warn("Failed to parse SSE data for event={}: {}",
                    event, ex.getMessage());
            return null;
        }
    }

    private String handleTokenEvent(JsonNode node,
                                    StringBuilder accumulator) {
        String content = node.path("content").asText("");
        if (!content.isEmpty()) {
            accumulator.append(content);
        }
        return "{\"type\":\"token\",\"content\":"
                + quoteJson(content) + "}";
    }

    private String handleDoneEvent(
            JsonNode node,
            AtomicReference<String> doneFullResponse) {
        String full = node.path("fullResponse").asText(null);
        if (full != null) {
            doneFullResponse.set(full);
        }
        return "{\"type\":\"done\"}";
    }

    private String handleToolEvent(String event, JsonNode node) {
        String name = node.path("name").asText("");
        return "{\"type\":\"" + event + "\",\"name\":"
                + quoteJson(name) + "}";
    }

    private String handleErrorEvent(JsonNode node,
                                    AtomicBoolean hasError) {
        hasError.set(true);
        String msg = node.path("message").asText("Unknown error");
        log.error("AI service returned error event: {}", msg);
        return buildErrorEvent(msg);
    }

    private String quoteJson(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "\"\"";
        }
    }

    private String buildStartEvent(AiConversation conversation) {
        return "{\"type\":\"start\","
                + "\"conversationId\":\""
                + conversation.getId() + "\","
                + "\"threadId\":\""
                + conversation.getThreadId() + "\"}";
    }

    private String buildErrorEvent(String msg) {
        return "{\"type\":\"error\",\"message\":\""
                + msg.replace("\"", "\\\"") + "\"}";
    }

    private AiChatMessageResponse toDto(AiMessage msg) {
        return AiChatMessageResponse.builder()
                .messageId(msg.getId())
                .role(msg.getRole().name())
                .content(msg.getContent())
                .sequence(msg.getSequence())
                .build();
    }

    /**
     * Get the remaining AI chat quota for a user.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAiQuotaStatus(UUID userId) {
        return quotaService.getAiQuotaStatus(userId);
    }
}
