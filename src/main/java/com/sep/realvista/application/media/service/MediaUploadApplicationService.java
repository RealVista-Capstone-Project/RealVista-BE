package com.sep.realvista.application.media.service;

import com.sep.realvista.application.media.dto.BulkMediaUploadResponse;
import com.sep.realvista.application.media.dto.MediaUploadResponse;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaUploadApplicationService {

    private final SpacesStorageService spacesStorageService;

    @Transactional
    public MediaUploadResponse uploadMedia(MultipartFile file, String folder) {
        try {
            log.info("Starting upload for file: {} to folder: {}",
                    file.getOriginalFilename(), folder);
            log.info("File details - Name: {}, Size: {} bytes, Type: {}",
                    file.getOriginalFilename(), file.getSize(), file.getContentType());

            String mediaUrl = spacesStorageService.uploadFile(file, folder);

            MediaUploadResponse response = MediaUploadResponse.builder()
                    .mediaUrl(mediaUrl)
                    .mediaType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileName(file.getOriginalFilename())
                    .uploadedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                    .folder(folder)
                    .build();

            log.info("Upload successful for file: {} - URL: {}", file.getOriginalFilename(), mediaUrl);
            return response;

        } catch (IOException e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to upload media: " + e.getMessage(), e);
        }
    }

    @Transactional
    public BulkMediaUploadResponse uploadMultipleMedia(List<MultipartFile> files, String folder) {
        log.info("Starting bulk upload for {} files to folder: {}", files.size(), folder);

        List<MediaUploadResponse> uploadedFiles = new ArrayList<>();
        List<BulkMediaUploadResponse.FailedUpload> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String mediaUrl = spacesStorageService.uploadFile(file, folder);

                MediaUploadResponse response = MediaUploadResponse.builder()
                        .mediaUrl(mediaUrl)
                        .mediaType(file.getContentType())
                        .fileSize(file.getSize())
                        .fileName(file.getOriginalFilename())
                        .uploadedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                        .folder(folder)
                        .build();

                uploadedFiles.add(response);
                log.info("Upload successful for file: {} - URL: {}", file.getOriginalFilename(), mediaUrl);

            } catch (Exception e) {
                log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
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

        log.info("Bulk upload completed: {} successful, {} failed out of {} total",
                uploadedFiles.size(), failedFiles.size(), files.size());

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
}
