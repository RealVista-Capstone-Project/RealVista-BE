package com.sep.realvista.application.media.service;

import com.sep.realvista.application.media.dto.BulkMediaUploadResponse;
import com.sep.realvista.application.media.dto.MediaUploadResponse;
import com.sep.realvista.domain.property.MediaType;
import com.sep.realvista.domain.property.PropertyMedia;
import com.sep.realvista.domain.property.repository.PropertyMediaRepository;
import com.sep.realvista.infrastructure.external.storage.SpacesStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public MediaUploadResponse uploadMedia(MultipartFile file, String folder, UUID propertyId, UUID userId) {
        try {
            return processFileUpload(file, folder, propertyId, userId);
        } catch (IOException e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to upload media: " + e.getMessage(), e);
        }
    }

    @Transactional
    public BulkMediaUploadResponse uploadMultipleMedia(List<MultipartFile> files, String folder,
                                                       UUID propertyId, UUID userId) {
        log.info("Starting bulk upload of {} files to folder: {}", files.size(), folder);

        List<MediaUploadResponse> uploadedFiles = new ArrayList<>();
        List<BulkMediaUploadResponse.FailedUpload> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                uploadedFiles.add(processFileUpload(file, folder, propertyId, userId));
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
                                                  UUID propertyId, UUID userId) throws IOException {
        String fileName = file.getOriginalFilename();
        log.info("Processing file upload: {} to folder: {} (Size: {} bytes, Type: {})",
                fileName, folder, file.getSize(), file.getContentType());

        String mediaUrl = spacesStorageService.uploadFile(file, folder);

        UUID mediaId = null;
        if (propertyId != null && userId != null) {
            PropertyMedia pm = PropertyMedia.builder()
                    .propertyId(propertyId)
                    .uploadBy(userId)
                    .mediaType(determineMediaType(file.getContentType()))
                    .mediaUrl(mediaUrl)
                    .isPropertyStandard(false)
                    .isPrimary(false)
                    .build();
            pm = propertyMediaRepository.save(pm);
            mediaId = pm.getPropertyMediaId();
            log.debug("Persisted PropertyMedia for file: {} with ID: {}", fileName, mediaId);
        }

        MediaUploadResponse response = MediaUploadResponse.builder()
                .mediaId(mediaId)
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

    @Transactional
    public void deleteMedia(String mediaUrl) {
        try {
            log.info("Deleting media: {}", mediaUrl);
            spacesStorageService.deleteFile(mediaUrl);
            log.info("Media deleted successfully: {}", mediaUrl);
        } catch (Exception e) {
            log.error("Failed to delete media: {}", mediaUrl, e);
            throw new RuntimeException("Failed to delete media: " + e.getMessage(), e);
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
