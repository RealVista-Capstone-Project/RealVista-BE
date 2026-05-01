package com.sep.realvista.presentation.rest.notification;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.notification.dto.CreateNotificationTemplateRequest;
import com.sep.realvista.application.notification.dto.NotificationTemplateResponse;
import com.sep.realvista.application.notification.dto.UpdateNotificationTemplateRequest;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.application.notification.service.NotificationTemplateApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import com.sep.realvista.application.service.TemplateEngineService;
import com.sep.realvista.application.notification.dto.TemplatePreviewRequest;
import com.sep.realvista.application.notification.dto.TemplateSchemaResponse;
import com.sep.realvista.application.notification.dto.TestSendRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.sep.realvista.application.service.EmailService;
import com.sep.realvista.application.service.FirebaseNotificationService;
import com.sep.realvista.application.user.service.UserApplicationService;
import com.sep.realvista.application.user.dto.UserResponse;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/admin/templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminNotificationTemplateController {

    private final NotificationTemplateApplicationService templateService;
    private final TemplateEngineService engineService;
    private final EmailService emailService;
    private final NotificationApplicationService notificationService;
    private final FirebaseNotificationService fcmService;
    private final UserApplicationService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationTemplateResponse>>> getAllTemplates(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            Pageable pageable) {
        Page<NotificationTemplateResponse> page = templateService.getAllTemplates(keyword, type, pageable);
        PageResponse<NotificationTemplateResponse> pageResponse = PageResponse.<NotificationTemplateResponse>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> getTemplate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(templateService.getTemplate(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> createTemplate(
            @Valid @RequestBody CreateNotificationTemplateRequest request) {
        NotificationTemplateResponse created = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Template created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateNotificationTemplateRequest request) {
        NotificationTemplateResponse updated = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Template updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Template deleted successfully", null));
    }

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<TemplateEngineService.RenderedTemplate>> previewTemplate(
            @RequestBody TemplatePreviewRequest request) {

        Map<String, Object> mockData = new HashMap<>();
        if (request.getMockData() != null) {
            mockData.putAll(request.getMockData());
        }

        if (!mockData.containsKey("userName")) {
            mockData.put("userName", "John Doe");
        }
        if (!mockData.containsKey("name")) {
            mockData.put("name", "John Doe");
        }
        if (!mockData.containsKey("otp")) {
            mockData.put("otp", "123456");
        }

        TemplateEngineService.RenderedTemplate rendered = engineService.preview(
                request.getTitle(), request.getContentBody(), mockData);
        return ResponseEntity.ok(ApiResponse.success(rendered));
    }

    @GetMapping("/schema/{templateKey}")
    public ResponseEntity<ApiResponse<TemplateSchemaResponse>> getTemplateSchema(@PathVariable String templateKey) {
        return ResponseEntity.ok(ApiResponse.success(engineService.getSchema(templateKey)));
    }

    @PostMapping("/test-send")
    public ResponseEntity<ApiResponse<Void>> testSend(@Valid @RequestBody TestSendRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        // Get user details to get email/FCM token
        UUID userId = userService.findUserIdByEmail(currentUsername);
        UserResponse user = userService.getUserById(userId);

        Map<String, Object> mockData = new HashMap<>();
        if (request.getMockData() != null) {
            mockData.putAll(request.getMockData());
        }

        // Default mock data if missing
        mockData.putIfAbsent("userName", user.getFullName());
        mockData.putIfAbsent("name", user.getFullName());

        TemplateEngineService.RenderedTemplate rendered = engineService.preview(
                request.getTitle(), request.getContentBody(), mockData);

        if ("EMAIL".equalsIgnoreCase(request.getType())) {
            emailService.sendHtmlMessage(user.getEmail(), rendered.title(), rendered.body());
        } else if ("IN_APP".equalsIgnoreCase(request.getType())) {
            notificationService.sendNotification(com.sep.realvista.application.notification.dto.SendNotificationRequest.builder()
                    .userId(user.getUserId())
                    .userEmail(user.getEmail())
                    .title(rendered.title())
                    .message(rendered.body())
                    .eventType(com.sep.realvista.domain.user.notification.EventType.SYSTEM)
                    .entityType(null)
                    .entityId(null)
                    .metadata(new HashMap<>())
                    .build());
            log.info("Test-send Notification to {}: {} - {}", user.getFullName(), rendered.title(), rendered.body());
        }

        return ResponseEntity.ok(ApiResponse.<Void>success("Test notification sent to " + user.getEmail(), null));
    }
}
