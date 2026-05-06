package com.sep.realvista.application.media.service;

import com.sep.realvista.application.media.dto.BulkMediaUploadResponse;
import com.sep.realvista.application.media.dto.MediaUploadResponse;
import com.sep.realvista.domain.property.MediaType;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingMedia;
import com.sep.realvista.domain.listing.repository.ListingMediaRepository;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.property.PropertyMedia;
import com.sep.realvista.domain.property.repository.PropertyMediaRepository;
import com.sep.realvista.infrastructure.external.storage.SpacesStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaUploadApplicationService {
    private final SpacesStorageService spacesStorageService;
    private final PropertyMediaRepository propertyMediaRepository;
    private final ListingRepository listingRepository;
    private final ListingMediaRepository listingMediaRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "listings", key = "#listingId", condition = "#listingId != null"),
            @CacheEvict(value = "similarListings", allEntries = true, condition = "#listingId != null")
    })
    public MediaUploadResponse uploadMedia(MultipartFile file, String folder,
            UUID propertyId, UUID listingId, UUID userId) {
        try {
            return processFileUpload(file, folder, propertyId, listingId, userId);
        } catch (IOException e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to upload media: " + e.getMessage(), e);
        }
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "listings", key = "#listingId", condition = "#listingId != null"),
            @CacheEvict(value = "similarListings", allEntries = true, condition = "#listingId != null")
    })
    public BulkMediaUploadResponse uploadMultipleMedia(List<MultipartFile> files, String folder,
            UUID propertyId, UUID listingId, UUID userId) {
        log.info("Starting bulk upload of {} files to folder: {}", files.size(), folder);

        List<MediaUploadResponse> uploadedFiles = new ArrayList<>();
        List<BulkMediaUploadResponse.FailedUpload> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                uploadedFiles.add(processFileUpload(file, folder, propertyId, listingId, userId));
            } catch (Exception e) {
                log.error("Bulk upload failed for file: {}", file.getOriginalFilename(), e);
                failedFiles.add(BulkMediaUploadResponse.FailedUpload.builder()
                        .fileName(file.getOriginalFilename())
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        BulkMediaUploadResponse response = BulkMediaUploadResponse.builder()
                .uploadedFiles(uploadedFiles)
                .totalCount(files.size())
                .successCount(uploadedFiles.size())
                .failedCount(failedFiles.size())
                .failedFiles(failedFiles)
                .build();

        log.info("Bulk upload completed: {} successful, {} failed out of {} total files",
                uploadedFiles.size(), failedFiles.size(), files.size());

        return response;
    }

    private MediaUploadResponse processFileUpload(MultipartFile file, String folder,
            UUID propertyId, UUID listingId, UUID userId) throws IOException {
        String fileName = file.getOriginalFilename();
        log.info("Processing file upload: {} to folder: {} (Size: {} bytes, Type: {})",
                fileName, folder, file.getSize(), file.getContentType());

        String mediaUrl = spacesStorageService.uploadFile(file, folder);

        UUID mediaId = null;
        UUID listingMediaId = null;
        UUID resolvedPropertyId = resolvePropertyId(propertyId, listingId);
        if (resolvedPropertyId != null && userId != null) {
            PropertyMedia pm = PropertyMedia.builder()
                    .propertyId(resolvedPropertyId)
                    .uploadBy(userId)
                    .mediaType(determineMediaType(file.getContentType()))
                    .mediaUrl(mediaUrl)
                    .isPropertyStandard(false)
                    .isPrimary(false)
                    .build();
            pm = propertyMediaRepository.save(pm);
            mediaId = pm.getPropertyMediaId();
            log.debug("Persisted PropertyMedia for file: {} with ID: {}", fileName, mediaId);

            if (listingId != null) {
                int displayOrder = listingMediaRepository.findByListingId(listingId).size();
                ListingMedia listingMedia = ListingMedia.create(listingId, mediaId, displayOrder, displayOrder == 0);
                listingMedia = listingMediaRepository.save(listingMedia);
                listingMediaId = listingMedia.getListingMediaId();
                log.debug("Linked PropertyMedia ID: {} to Listing ID: {} with ListingMedia ID: {}",
                        mediaId, listingId, listingMediaId);
            }
        }

        MediaUploadResponse response = MediaUploadResponse.builder()
                .mediaId(mediaId)
                .listingMediaId(listingMediaId)
                .mediaUrl(mediaUrl)
                .mediaType(file.getContentType())
                .fileSize(file.getSize())
                .fileName(fileName)
                .uploadedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .folder(folder)
                .build();

        log.info("Successfully uploaded file: {} - URL: {}", fileName, mediaUrl);
        return response;
    }

    private UUID resolvePropertyId(UUID propertyId, UUID listingId) {
        if (propertyId != null) {
            return propertyId;
        }
        if (listingId == null) {
            return null;
        }
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", listingId));
        return listing.getPropertyId();
    }

    @Transactional
    public void deleteMedia(UUID mediaId) {
        PropertyMedia media = propertyMediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found with ID: " + mediaId));

        log.info("Deleting media record: {} and file: {}", mediaId, media.getMediaUrl());

        // 1. Delete record from database
        propertyMediaRepository.delete(media);

        // 2. Delete file from cloud storage
        try {
            spacesStorageService.deleteFile(media.getMediaUrl());
        } catch (Exception e) {
            log.error("Failed to delete file from storage for media {}: {}", mediaId, e.getMessage());
            // No throw: prioritizing DB cleanup for the user experience, storage cleanup is
            // async or retryable.
        }
    }

    @Transactional
    public void deleteMediaByUrl(String mediaUrl) {
        try {
            log.info("Deleting media by URL: {}", mediaUrl);
            spacesStorageService.deleteFile(mediaUrl);
            log.info("Media file deleted successfully: {}", mediaUrl);
        } catch (Exception e) {
            log.error("Failed to delete media file: {}", mediaUrl, e.getMessage());
            throw new RuntimeException("Failed to delete media file: " + e.getMessage(), e);
        }
    }

    private MediaType determineMediaType(String contentType) {
        if (contentType == null) {
            return MediaType.IMAGE;
        }
        if (contentType.startsWith("video/")) {
            return MediaType.VIDEO;
        }
        return MediaType.IMAGE;
    }
}
