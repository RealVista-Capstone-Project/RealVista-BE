package com.sep.realvista.presentation.rest.media;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.media.dto.MediaUploadResponse;
import com.sep.realvista.application.media.service.MediaUploadApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Media Upload", description = "API endpoints for uploading images and videos to DigitalOcean Spaces")
@SecurityRequirement(name = "Bearer Authentication")
public class MediaUploadController {

    private final MediaUploadApplicationService mediaUploadService;

    @Operation(
            summary = "Upload a single media file",
            description = "Upload a single image or video file to DigitalOcean Spaces. Supports JPEG, PNG, GIF, WebP, HEIC/HEIF for images and MP4, MPEG, QuickTime, AVI, WebM for videos."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "File uploaded successfully",
                    content = @Content(schema = @Schema(implementation = MediaUploadResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file or file type"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during upload"
            )
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MediaUploadResponse>> uploadMedia(
            @Parameter(description = "Media file to upload (image or video)", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Folder path in storage (e.g., 'prod/properties', 'non-prod/test')", example = "non-prod/test")
            @RequestParam(value = "folder", required = false, defaultValue = "media") String folder
    ) {
        // Debug logging
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Upload request - Authentication: {}, Principal: {}, Authorities: {}",
                auth != null ? auth.getClass().getSimpleName() : "null",
                auth != null ? auth.getName() : "null",
                auth != null ? auth.getAuthorities() : "null");

        log.info("Received upload request for file: {} to folder: {}", file.getOriginalFilename(), folder);

        MediaUploadResponse response = mediaUploadService.uploadMedia(file, folder);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Media uploaded successfully", response));
    }

    @Operation(
            summary = "Upload multiple media files",
            description = "Upload multiple image or video files to DigitalOcean Spaces in a single request. Returns success and failure details for each file."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Files uploaded (may include partial failures)",
                    content = @Content(schema = @Schema(implementation = com.sep.realvista.application.media.dto.BulkMediaUploadResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or no files provided"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token"
            )
    })
    @PostMapping(value = "/upload/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<com.sep.realvista.application.media.dto.BulkMediaUploadResponse>> uploadMultipleMedia(
            @Parameter(description = "Multiple media files to upload", required = true)
            @RequestParam("files") List<MultipartFile> files,

            @Parameter(description = "Folder path in storage", example = "listings")
            @RequestParam(value = "folder", required = false, defaultValue = "media") String folder
    ) {
        log.info("Received bulk upload request for {} files to folder: {}", files.size(), folder);

        if (files.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No files provided for upload"));
        }

        com.sep.realvista.application.media.dto.BulkMediaUploadResponse response = mediaUploadService.uploadMultipleMedia(files, folder);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bulk upload completed", response));
    }

    @Operation(
            summary = "Delete a media file",
            description = "Delete a media file from DigitalOcean Spaces using the file URL"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "File deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid media URL"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Insufficient permissions"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during deletion"
            )
    })
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(
            @Parameter(description = "Full URL of the media file to delete", required = true, example = "https://realvista.sgp1.cdn.digitaloceanspaces.com/media/image.jpg")
            @RequestParam("mediaUrl") String mediaUrl
    ) {
        log.info("Received delete request for media: {}", mediaUrl);

        if (mediaUrl == null || mediaUrl.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Media URL is required"));
        }

        mediaUploadService.deleteMedia(mediaUrl);

        return ResponseEntity
                .ok(ApiResponse.success("Media deleted successfully", null));
    }
}
