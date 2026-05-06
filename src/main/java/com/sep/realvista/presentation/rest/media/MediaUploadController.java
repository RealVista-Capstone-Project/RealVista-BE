package com.sep.realvista.presentation.rest.media;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.media.dto.BulkMediaUploadResponse;
import com.sep.realvista.application.media.dto.MediaUploadResponse;
import com.sep.realvista.application.media.service.MediaUploadApplicationService;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

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
            description = "Upload a single image or video file to DigitalOcean Spaces. "
                    + "Supports JPEG, PNG, GIF, WebP, HEIC/HEIF for images and MP4, MPEG, "
                    + "QuickTime, AVI, WebM for videos."
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MediaUploadResponse>> uploadMedia(
            @Parameter(description = "Media file to upload (image or video)", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Folder path in storage (e.g., 'prod/properties', "
                    + "'non-prod/test')", example = "non-prod/test")
            @RequestParam(value = "folder", required = false, defaultValue = "media") String folder,

            @Parameter(description = "Optional property ID to associate the media with",
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam(value = "propertyId", required = false) UUID propertyId,

            @Parameter(description = "Optional listing ID to immediately attach the uploaded media to",
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam(value = "listingId", required = false) UUID listingId,

            @AuthenticationPrincipal SecurityUserDetails userDetails
    ) {
        // Debug logging
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Upload request - Authentication: {}, Principal: {}, Authorities: {}",
                auth != null ? auth.getClass().getSimpleName() : "null",
                auth != null ? auth.getName() : "null",
                auth != null ? auth.getAuthorities() : "null");

        log.info("Received upload request for file: {} to folder: {}, propertyId: {}, listingId: {}",
                file.getOriginalFilename(), folder, propertyId, listingId);

        MediaUploadResponse response = mediaUploadService
                .uploadMedia(file, folder, propertyId, listingId, userDetails.getUserId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Media uploaded successfully", response));
    }

    @Operation(
            summary = "Upload multiple media files",
            description = "Upload multiple image or video files to DigitalOcean Spaces in a "
                    + "single request. Returns success and failure details for each file."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Files uploaded (may include partial failures)",
                    content = @Content(schema = @Schema(
                            implementation = BulkMediaUploadResponse.class))
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
    public ResponseEntity<ApiResponse<BulkMediaUploadResponse>> uploadMultipleMedia(
            @Parameter(description = "Multiple media files to upload", required = true)
            @RequestParam("files") List<MultipartFile> files,

            @Parameter(description = "Folder path in storage", example = "listings")
            @RequestParam(value = "folder", required = false, defaultValue = "media") String folder,

            @Parameter(description = "Optional property ID to associate the media with",
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam(value = "propertyId", required = false) UUID propertyId,

            @Parameter(description = "Optional listing ID to immediately attach the uploaded media to",
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam(value = "listingId", required = false) UUID listingId,

            @AuthenticationPrincipal SecurityUserDetails userDetails
    ) {
        log.info("Received bulk upload request for {} files to folder: {}, propertyId: {}, listingId: {}",
                files.size(), folder, propertyId, listingId);

        if (files.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No files provided for upload"));
        }

        BulkMediaUploadResponse response =
                mediaUploadService.uploadMultipleMedia(files, folder, propertyId, listingId, userDetails.getUserId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bulk upload completed", response));
    }

    @Operation(
            summary = "Delete a media record and file",
            description = "Delete a media record from the database and its associated file from DigitalOcean Spaces"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Media deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Media not found"
            )
    })
    @DeleteMapping("/{mediaId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<ApiResponse<Void>> deleteMediaById(
            @Parameter(description = "ID of the media record to delete", required = true)
            @PathVariable UUID mediaId
    ) {
        log.info("Received delete request for media ID: {}", mediaId);
        mediaUploadService.deleteMedia(mediaId);
        return ResponseEntity.ok(ApiResponse.success("Media deleted successfully", null));
    }

    @Operation(
            summary = "Delete a media file by URL",
            description = "Delete a media file from DigitalOcean Spaces using the file URL (storage only)"
    )
    @DeleteMapping("/by-url")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<ApiResponse<Void>> deleteMediaByUrl(
            @Parameter(description = "Full URL of the media file to delete", required = true)
            @RequestParam("mediaUrl") String mediaUrl
    ) {
        if (mediaUrl == null || mediaUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Media URL is required");
        }

        log.info("Received delete request for media URL: {}", mediaUrl);
        mediaUploadService.deleteMediaByUrl(mediaUrl);
        return ResponseEntity.ok(ApiResponse.success("Media file deleted successfully", null));
    }
}
