package com.sep.realvista.infrastructure.external.storage;

import com.sep.realvista.infrastructure.config.SpacesConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpacesStorageService {

    private final S3Client s3Client;
    private final SpacesConfig spacesConfig;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", 
            "image/jpg", 
            "image/png", 
            "image/gif", 
            "image/webp",
            "image/heic",           // HEIC format (Apple)
            "image/heif",           // HEIF format (alternative)
            "image/heic-sequence",  // HEIC sequence
            "image/heif-sequence"   // HEIF sequence
    );

    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo", "video/webm"
    );

    private static final long MAX_IMAGE_SIZE = 20 * 1024 * 1024; // 20MB
    private static final long MAX_VIDEO_SIZE = 200 * 1024 * 1024; // 200MB

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        validateFile(file);

        String fileName = generateFileName(file.getOriginalFilename());
        String key = buildKey(folder, fileName);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(spacesConfig.getBucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            PutObjectResponse response = s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(file.getBytes())
            );

            String fileUrl = buildFileUrl(key);
            log.info("File uploaded successfully to Spaces: {} - ETag: {}", fileUrl, response.eTag());

            return fileUrl;

        } catch (S3Exception e) {
            log.error("Failed to upload file to DigitalOcean Spaces: {}", e.awsErrorDetails().errorMessage(), e);
            throw new IOException("Failed to upload file: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    public String uploadBytes(byte[] bytes, String folder, String fileName, String contentType) throws IOException {
        if (bytes == null || bytes.length == 0) {
            throw new IOException("File bytes are empty or null");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IOException("File name is required");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IOException("Content type is required");
        }

        String key = buildKey(folder, fileName);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(spacesConfig.getBucketName())
                    .key(key)
                    .contentType(contentType)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            PutObjectResponse response = s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(bytes)
            );

            String fileUrl = buildFileUrl(key);
            log.info("Bytes uploaded successfully to Spaces: {} - ETag: {}", fileUrl, response.eTag());
            return fileUrl;
        } catch (S3Exception e) {
            log.error("Failed to upload bytes to DigitalOcean Spaces: {}", e.awsErrorDetails().errorMessage(), e);
            throw new IOException("Failed to upload file: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(spacesConfig.getBucketName())
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from Spaces: {}", fileUrl);

        } catch (S3Exception e) {
            log.error("Failed to delete file from DigitalOcean Spaces: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to delete file: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty or null");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        
        if (contentType == null) {
            throw new IOException("File content type is null");
        }

        // Check by content type
        boolean isImage = ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase());
        boolean isVideo = ALLOWED_VIDEO_TYPES.contains(contentType.toLowerCase());
        
        // Fallback: Check by file extension for HEIC/HEIF files
        // Some systems report HEIC as "application/octet-stream" or other types
        if (!isImage && !isVideo && originalFilename != null) {
            String lowerFilename = originalFilename.toLowerCase();
            if (lowerFilename.endsWith(".heic") || lowerFilename.endsWith(".heif")) {
                isImage = true;
                log.info("HEIC/HEIF file detected by extension: {}", originalFilename);
            }
        }

        if (!isImage && !isVideo) {
            throw new IOException("Invalid file type. Allowed types: images (JPEG, PNG, GIF, WebP, "
                    + "HEIC/HEIF) and videos (MP4, MPEG, QuickTime, AVI, WebM)");
        }

        long fileSize = file.getSize();
        if (isImage && fileSize > MAX_IMAGE_SIZE) {
            throw new IOException("Image file size exceeds maximum allowed size of 20MB");
        }

        if (isVideo && fileSize > MAX_VIDEO_SIZE) {
            throw new IOException("Video file size exceeds maximum allowed size of 200MB");
        }
    }

    private String generateFileName(String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);
        return timestamp + "_" + uuid + extension;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String buildKey(String folder, String fileName) {
        if (folder == null || folder.isEmpty()) {
            return fileName;
        }
        return folder.endsWith("/") ? folder + fileName : folder + "/" + fileName;
    }

    private String buildFileUrl(String key) {
        String cdnEndpoint = spacesConfig.getCdnEndpoint();
        if (cdnEndpoint != null && !cdnEndpoint.isEmpty()) {
            return cdnEndpoint.endsWith("/") ? cdnEndpoint + key : cdnEndpoint + "/" + key;
        }
        String endpoint = spacesConfig.getEndpoint();
        String bucketName = spacesConfig.getBucketName();
        return endpoint.replace("https://", "https://" + bucketName + ".") + "/" + key;
    }

    private String extractKeyFromUrl(String fileUrl) {
        String cdnEndpoint = spacesConfig.getCdnEndpoint();
        if (cdnEndpoint != null && !cdnEndpoint.isEmpty() && fileUrl.startsWith(cdnEndpoint)) {
            return fileUrl.substring(cdnEndpoint.length() + (cdnEndpoint.endsWith("/") ? 0 : 1));
        }

        String endpoint = spacesConfig.getEndpoint();
        String bucketName = spacesConfig.getBucketName();
        String baseUrl = endpoint.replace("https://", "https://" + bucketName + ".");
        if (fileUrl.startsWith(baseUrl)) {
            return fileUrl.substring(baseUrl.length() + 1);
        }

        return fileUrl;
    }

    public boolean isImageFile(String contentType) {
        return contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase());
    }

    public boolean isVideoFile(String contentType) {
        return contentType != null && ALLOWED_VIDEO_TYPES.contains(contentType.toLowerCase());
    }
}
