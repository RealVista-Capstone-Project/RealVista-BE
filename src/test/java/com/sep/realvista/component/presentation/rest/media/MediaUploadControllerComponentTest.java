package com.sep.realvista.component.presentation.rest.media;

import com.sep.realvista.application.auth.service.TokenService;
import com.sep.realvista.application.media.dto.BulkMediaUploadResponse;
import com.sep.realvista.application.media.dto.MediaUploadResponse;
import com.sep.realvista.application.media.service.MediaUploadApplicationService;
import com.sep.realvista.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import com.sep.realvista.presentation.exception.GlobalExceptionHandler;
import com.sep.realvista.presentation.rest.media.MediaUploadController;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Component tests for MediaUploadController.
 */
@WebMvcTest(MediaUploadController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("MediaUploadController Component Tests (Web Layer)")
class MediaUploadControllerComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaUploadApplicationService mediaUploadService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private TokenService tokenService;

    private MockMultipartFile validImageFile;
    private MockMultipartFile validVideoFile;
    private MockMultipartFile validHeicFile;
    private MediaUploadResponse mockImageResponse;
    private MediaUploadResponse mockVideoResponse;
    private SecurityUserDetails mockBuyerDetails;
    private SecurityUserDetails mockAgentDetails;
    private SecurityUserDetails mockAdminDetails;

    @BeforeEach
    void setUp() {
        validImageFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        validVideoFile = new MockMultipartFile(
                "file",
                "test-video.mp4",
                "video/mp4",
                "test video content".getBytes()
        );

        validHeicFile = new MockMultipartFile(
                "file",
                "test-photo.heic",
                "image/heic",
                "test heic content".getBytes()
        );

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        
        mockImageResponse = MediaUploadResponse.builder()
                .mediaUrl("https://realvista.sgp1.cdn.digitaloceanspaces.com/properties/"
                        + "20260315_180000_a1b2c3d4.jpg")
                .mediaType("image/jpeg")
                .fileSize(512000L)
                .fileName("test-image.jpg")
                .uploadedAt(timestamp)
                .build();

        mockVideoResponse = MediaUploadResponse.builder()
                .mediaUrl("https://realvista.sgp1.cdn.digitaloceanspaces.com/videos/"
                        + "20260315_180100_b2c3d4e5.mp4")
                .mediaType("video/mp4")
                .fileSize(52428800L)
                .fileName("test-video.mp4")
                .uploadedAt(timestamp)
                .build();

        mockBuyerDetails = SecurityUserDetails.builder()
                .userId(UUID.randomUUID())
                .username("buyer@example.com")
                .authorities(List.of(() -> "ROLE_BUYER"))
                .active(true)
                .build();

        mockAgentDetails = SecurityUserDetails.builder()
                .userId(UUID.randomUUID())
                .username("agent@example.com")
                .authorities(List.of(() -> "ROLE_AGENT"))
                .active(true)
                .build();

        mockAdminDetails = SecurityUserDetails.builder()
                .userId(UUID.randomUUID())
                .username("admin@example.com")
                .authorities(List.of(() -> "ROLE_ADMIN"))
                .active(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(SecurityUserDetails userDetails) {
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("POST /api/v1/media/upload - Single File Upload")
    class SingleFileUploadTests {

        @Test
        @DisplayName("Should return 201 Created when uploading valid image with authenticated user")
        void uploadMedia_withValidImageAndAuth_shouldReturn201() throws Exception {
            setAuthentication(mockBuyerDetails);
            when(mediaUploadService.uploadMedia(any(), eq("properties"), any(), any()))
                    .thenReturn(mockImageResponse);

            mockMvc.perform(multipart("/api/v1/media/upload")
                            .file(validImageFile)
                            .param("folder", "properties")
                            .with(user(mockBuyerDetails))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Media uploaded successfully"))
                    .andExpect(jsonPath("$.data.media_url").value(containsString("properties")))
                    .andExpect(jsonPath("$.data.media_type").value("image/jpeg"))
                    .andExpect(jsonPath("$.data.file_size").value(512000))
                    .andExpect(jsonPath("$.data.file_name").value("test-image.jpg"))
                    .andExpect(jsonPath("$.data.uploaded_at").exists());

            verify(mediaUploadService, times(1)).uploadMedia(any(), eq("properties"), any(), any());
        }

        @Test
        @DisplayName("Should return 201 Created when uploading valid video")
        void uploadMedia_withValidVideo_shouldReturn201() throws Exception {
            setAuthentication(mockAgentDetails);
            when(mediaUploadService.uploadMedia(any(), eq("videos"), any(), any()))
                    .thenReturn(mockVideoResponse);

            mockMvc.perform(multipart("/api/v1/media/upload")
                            .file(validVideoFile)
                            .param("folder", "videos")
                            .with(user(mockAgentDetails))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.media_type").value("video/mp4"))
                    .andExpect(jsonPath("$.data.file_size").value(52428800));

            verify(mediaUploadService, times(1)).uploadMedia(any(), eq("videos"), any(), any());
        }

        @Test
        @DisplayName("Should return 201 Created when uploading HEIC file")
        void uploadMedia_withHeicFile_shouldReturn201() throws Exception {
            setAuthentication(mockBuyerDetails);
            MediaUploadResponse heicResponse = MediaUploadResponse.builder()
                    .mediaUrl("https://realvista.sgp1.cdn.digitaloceanspaces.com/properties/photo.heic")
                    .mediaType("image/heic")
                    .fileSize(307200L)
                    .fileName("test-photo.heic")
                    .uploadedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                    .build();

            when(mediaUploadService.uploadMedia(any(), eq("properties"), any(), any()))
                    .thenReturn(heicResponse);

            mockMvc.perform(multipart("/api/v1/media/upload")
                            .file(validHeicFile)
                            .param("folder", "properties")
                            .with(user(mockBuyerDetails))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.media_type").value("image/heic"));
        }

        @Test
        @DisplayName("Should use default folder 'media' when no folder specified")
        void uploadMedia_withNoFolder_shouldUseDefaultFolder() throws Exception {
            setAuthentication(mockBuyerDetails);
            when(mediaUploadService.uploadMedia(any(), eq("media"), any(), any()))
                    .thenReturn(mockImageResponse);

            mockMvc.perform(multipart("/api/v1/media/upload")
                            .file(validImageFile)
                            .with(user(mockBuyerDetails))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isCreated());

            verify(mediaUploadService, times(1)).uploadMedia(any(), eq("media"), any(), any());
        }

        @Test
        @DisplayName("Should return 500 when service throws IOException")
        void uploadMedia_whenServiceThrowsIOException_shouldReturn500() throws Exception {
            setAuthentication(mockBuyerDetails);
            when(mediaUploadService.uploadMedia(any(), anyString(), any(), any()))
                    .thenThrow(new RuntimeException("Failed to upload media: Connection timeout"));

            mockMvc.perform(multipart("/api/v1/media/upload")
                            .file(validImageFile)
                            .param("folder", "test")
                            .with(user(mockBuyerDetails))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/media/upload/bulk - Bulk File Upload")
    class BulkFileUploadTests {

        @Test
        @DisplayName("Should return 201 Created when all files upload successfully")
        void uploadBulk_withAllFilesSuccess_shouldReturn201() throws Exception {
            setAuthentication(mockAgentDetails);
            List<MediaUploadResponse> uploadedFiles = List.of(mockImageResponse, mockVideoResponse);
            BulkMediaUploadResponse bulkResponse = BulkMediaUploadResponse.builder()
                    .uploadedFiles(uploadedFiles)
                    .totalCount(2)
                    .successCount(2)
                    .failedCount(0)
                    .failedFiles(new ArrayList<>())
                    .build();

            when(mediaUploadService.uploadMultipleMedia(anyList(), eq("properties"), any(), any()))
                    .thenReturn(bulkResponse);

            mockMvc.perform(multipart("/api/v1/media/upload/bulk")
                            .file(new MockMultipartFile("files", "image1.jpg", 
                                    "image/jpeg", "content1".getBytes()))
                            .file(new MockMultipartFile("files", "video1.mp4", 
                                    "video/mp4", "content2".getBytes()))
                            .param("folder", "properties")
                            .with(user(mockAgentDetails))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Bulk upload completed"))
                    .andExpect(jsonPath("$.data.total_count").value(2))
                    .andExpect(jsonPath("$.data.success_count").value(2))
                    .andExpect(jsonPath("$.data.failed_count").value(0))
                    .andExpect(jsonPath("$.data.uploaded_files", hasSize(2)))
                    .andExpect(jsonPath("$.data.failed_files", hasSize(0)));

            verify(mediaUploadService, times(1)).uploadMultipleMedia(anyList(), eq("properties"), any(), any());
        }

        @Test
        @DisplayName("Should return 201 with partial success when some files fail")
        void uploadBulk_withPartialSuccess_shouldReturn201WithFailures() throws Exception {
            setAuthentication(mockAgentDetails);
            List<MediaUploadResponse> uploadedFiles = List.of(mockImageResponse);
            List<BulkMediaUploadResponse.FailedUpload> failedFiles = List.of(
                    BulkMediaUploadResponse.FailedUpload.builder()
                            .fileName("invalid-file.txt")
                            .errorMessage("Invalid file type")
                            .build()
            );
            
            BulkMediaUploadResponse bulkResponse = BulkMediaUploadResponse.builder()
                    .uploadedFiles(uploadedFiles)
                    .totalCount(2)
                    .successCount(1)
                    .failedCount(1)
                    .failedFiles(failedFiles)
                    .build();

            when(mediaUploadService.uploadMultipleMedia(anyList(), anyString(), any(), any()))
                    .thenReturn(bulkResponse);

            mockMvc.perform(multipart("/api/v1/media/upload/bulk")
                            .file(new MockMultipartFile("files", "image1.jpg", 
                                    "image/jpeg", "content1".getBytes()))
                            .file(new MockMultipartFile("files", "invalid.txt", 
                                    "text/plain", "content2".getBytes()))
                            .param("folder", "test")
                            .with(user(mockAgentDetails))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.success_count").value(1))
                    .andExpect(jsonPath("$.data.failed_count").value(1))
                    .andExpect(jsonPath("$.data.failed_files[0].file_name").value("invalid-file.txt"))
                    .andExpect(jsonPath("$.data.failed_files[0].error_message")
                            .value("Invalid file type"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/media - File Deletion")
    class FileDeleteTests {

        @Test
        @DisplayName("Should return 200 OK when ADMIN deletes file successfully")
        void deleteMedia_withAdminRole_shouldReturn200() throws Exception {
            setAuthentication(mockAdminDetails);
            String mediaUrl = "https://realvista.sgp1.cdn.digitaloceanspaces.com/test/file.jpg";
            doNothing().when(mediaUploadService).deleteMediaByUrl(mediaUrl);

            mockMvc.perform(delete("/api/v1/media/by-url")
                            .param("mediaUrl", mediaUrl)
                            .with(user(mockAdminDetails))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Media file deleted successfully"));

            verify(mediaUploadService, times(1)).deleteMediaByUrl(mediaUrl);
        }

        @Test
        @DisplayName("Should return 200 OK when AGENT deletes file successfully")
        void deleteMedia_withAgentRole_shouldReturn200() throws Exception {
            setAuthentication(mockAgentDetails);
            String mediaUrl = "https://realvista.sgp1.cdn.digitaloceanspaces.com/properties/image.jpg";
            doNothing().when(mediaUploadService).deleteMediaByUrl(mediaUrl);

            mockMvc.perform(delete("/api/v1/media/by-url")
                            .param("mediaUrl", mediaUrl)
                            .with(user(mockAgentDetails))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(mediaUploadService, times(1)).deleteMediaByUrl(mediaUrl);
        }

        @Test
        @DisplayName("Should return 400 when mediaUrl is empty")
        void deleteMedia_withEmptyUrl_shouldReturn400() throws Exception {
            setAuthentication(mockAdminDetails);
            mockMvc.perform(delete("/api/v1/media/by-url")
                            .param("mediaUrl", "")
                            .with(user(mockAdminDetails))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Media URL is required"))
                    .andExpect(jsonPath("$.error_code").value("INVALID_ARGUMENT"))
                    .andExpect(jsonPath("$.path").value("/api/v1/media/by-url"));

            verify(mediaUploadService, never()).deleteMediaByUrl(anyString());
        }

        @Test
        @DisplayName("Should return 500 when service throws exception")
        void deleteMedia_whenServiceThrowsException_shouldReturn500() throws Exception {
            setAuthentication(mockAdminDetails);
            String mediaUrl = "https://realvista.sgp1.cdn.digitaloceanspaces.com/test/file.jpg";
            doThrow(new RuntimeException("Failed to delete media: File not found"))
                    .when(mediaUploadService).deleteMediaByUrl(mediaUrl);

            mockMvc.perform(delete("/api/v1/media/by-url")
                            .param("mediaUrl", mediaUrl)
                            .with(user(mockAdminDetails))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500));

            verify(mediaUploadService, times(1)).deleteMediaByUrl(mediaUrl);
        }
    }
}
