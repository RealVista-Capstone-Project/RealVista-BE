package com.sep.realvista.application.media.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.media.dto.Property3DGenerationDto;
import com.sep.realvista.domain.property.MediaType;
import com.sep.realvista.domain.property.Property3DGeneration;
import com.sep.realvista.domain.property.Property3DGenerationStatus;
import com.sep.realvista.domain.property.PropertyMedia;
import com.sep.realvista.domain.property.repository.Property3DGenerationRepository;
import com.sep.realvista.domain.property.repository.PropertyMediaRepository;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.infrastructure.external.marble.MarbleClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import com.sep.realvista.domain.property.event.Property3DGenerationCompletedEvent;
import com.sep.realvista.application.media.dto.CreateProperty3DOperationRequest;
import com.sep.realvista.infrastructure.external.marble.MarbleGenerateRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class Property3DGenerationService {

    private final Property3DGenerationRepository generationRepository;
    private final PropertyMediaRepository mediaRepository;
    private final PropertyRepository propertyRepository;
    private final MarbleClient marbleClient;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Property3DGenerationDto initiateOperation(
            UUID propertyId, CreateProperty3DOperationRequest request, UUID uploaderId) {
        // verify property exists
        propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        // Build generate request
        MarbleGenerateRequest marbleRequest = MarbleGenerateRequest.builder()
                .model(request.getModel() != null ? request.getModel() : "Marble 0.1-plus")
                .displayName(request.getDisplayName() != null 
                        ? request.getDisplayName() : "Property 3D World")
                .worldPrompt(MarbleGenerateRequest.WorldPrompt.builder()
                        .type("multi-image")
                        .multiImagePrompt(request.getImages().stream()
                                .map(img -> {
                                    if (img.getMediaAssetId() == null) {
                                        log.warn("MediaAssetId is null for image at azimuth {}", 
                                                img.getAzimuth());
                                    }
                                    return MarbleGenerateRequest.MultiImagePrompt.builder()
                                        .azimuth(img.getAzimuth())
                                        .content(MarbleGenerateRequest.Content.builder()
                                                .source("media_asset")
                                                .mediaAssetId(img.getMediaAssetId())
                                                .build())
                                        .build();
                                })
                                .collect(Collectors.toList()))
                        .reconstructImages(request.getImages().size() > 4)
                        .build())
                .build();

        try {
            log.info("Initiating Marble 3D generation with payload: {}", 
                    objectMapper.writeValueAsString(marbleRequest));
        } catch (Exception e) {
            log.warn("Failed to log Marble request payload", e);
        }

        JsonNode response = marbleClient.generateWorld(marbleRequest);
        if (response == null || !response.hasNonNull("operation_id")) {
            throw new RuntimeException("Failed to get operation_id from Marble API");
        }
        
        String operationId = response.path("operation_id").asText();

        // create new operation tracking
        Property3DGeneration generation = Property3DGeneration.builder()
                .propertyId(propertyId)
                .uploaderId(uploaderId)
                .operationId(operationId)
                .roomName(request.getRoomName())
                .status(Property3DGenerationStatus.PENDING)
                .build();
        generationRepository.save(generation);

        return mapToDto(generation);
    }

    @Transactional
    public List<Property3DGenerationDto> getOperations(UUID propertyId) {
        List<Property3DGeneration> generations = generationRepository.findByPropertyId(propertyId);
        if (generations.isEmpty()) {
            return List.of();
        }

        return generations.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public void checkAndUpdateStatus(Property3DGeneration generation) {
        JsonNode payload = marbleClient.getOperation(generation.getOperationId());
        if (payload == null) {
            log.warn("Could not fetch operation {} from Marble. Check if MARBLE_API_KEY is configured and valid.", 
                    generation.getOperationId());
            return;
        }

        boolean done = payload.path("done").asBoolean(false);
        if (!done) {
            return;
        }

        JsonNode errorNode = payload.path("error");
        if (!errorNode.isNull() && !errorNode.isMissingNode()) {
            generation.updateStatus(Property3DGenerationStatus.FAILED, errorNode.toString());
            generationRepository.save(generation);
            eventPublisher.publishEvent(new Property3DGenerationCompletedEvent(
                    this, generation.getPropertyId(), generation.getUploaderId(), false));
            return;
        }

        // Success -> extract assets
        try {
            JsonNode responseNode = payload.path("response");
            JsonNode assetsNode = responseNode.path("assets");

            String fullResSpz = assetsNode.path("splats").path("spz_urls").path("full_res").asText(null);
            if (fullResSpz == null || fullResSpz.isBlank() || "null".equals(fullResSpz)) {
                // Not standard, but sometimes API might only give mesh
                fullResSpz = assetsNode.path("mesh").path("collider_mesh_url").asText(null);
            }
            String thumbnailUrl = assetsNode.path("thumbnail_url").asText(null);

            // Convert JsonNode to Map<String, Object> for metadata
            Map<String, Object> metadataMap = new HashMap<>();
            Map<String, Object> assetsMap = objectMapper.convertValue(
                    assetsNode, new TypeReference<Map<String, Object>>() { }
            );
            metadataMap.put("marble_assets", assetsMap);
            metadataMap.put("operation_id", generation.getOperationId());
            if (generation.getRoomName() != null) {
                metadataMap.put("room_name", generation.getRoomName());
            }

            // User requested to keep old 3D medias untouched, so we just add the new one.

            // Create new Media
            PropertyMedia newMedia = PropertyMedia.builder()
                    .propertyId(generation.getPropertyId())
                    .uploadBy(generation.getUploaderId())
                    .mediaType(MediaType.THREE_D)
                    .mediaUrl(fullResSpz != null ? fullResSpz : "")
                    .thumbnailUrl(thumbnailUrl)
                    .metadata(metadataMap)
                    .isPrimary(false)
                    .build();
            mediaRepository.save(newMedia);

            generation.updateStatus(Property3DGenerationStatus.SUCCEEDED, null);
            generationRepository.save(generation);
            eventPublisher.publishEvent(new Property3DGenerationCompletedEvent(
                    this, generation.getPropertyId(), generation.getUploaderId(), true));

        } catch (Exception e) {
            log.error("Error processing successful Marble response for op {}: {}", 
                    generation.getOperationId(), e.getMessage());
            generation.updateStatus(Property3DGenerationStatus.FAILED, "Internal processing error: " + e.getMessage());
            generationRepository.save(generation);
            eventPublisher.publishEvent(new Property3DGenerationCompletedEvent(
                    this, generation.getPropertyId(), generation.getUploaderId(), false));
        }
    }

    private Property3DGenerationDto mapToDto(Property3DGeneration gen) {
        return Property3DGenerationDto.builder()
                .id(gen.getId())
                .propertyId(gen.getPropertyId())
                .operationId(gen.getOperationId())
                .status(gen.getStatus())
                .roomName(gen.getRoomName())
                .errorMessage(gen.getErrorMessage())
                .createdAt(gen.getCreatedAt() != null ? gen.getCreatedAt().toString() : null)
                .build();
    }
}
