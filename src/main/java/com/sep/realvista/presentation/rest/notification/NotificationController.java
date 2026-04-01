package com.sep.realvista.presentation.rest.notification;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.common.dto.PageResponse;
import com.sep.realvista.application.notification.dto.NotificationResponse;
import com.sep.realvista.application.notification.dto.UnreadCountResponse;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for managing user notifications")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class NotificationController {

    private final NotificationApplicationService notificationApplicationService;

    @GetMapping
    @Operation(summary = "Get notifications",
            description = "Retrieves paginated notifications for the current user, ordered by most recent first")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("Fetching notifications for user: {}", currentUser.getUserId());
        PageResponse<NotificationResponse> notifications =
                notificationApplicationService.getUserNotifications(currentUser.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count",
            description = "Returns the number of unread notifications for the current user")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        long count = notificationApplicationService.getUnreadCount(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved",
                UnreadCountResponse.builder().unreadCount(count).build()));
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read",
            description = "Marks a single notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        log.info("Marking notification {} as read for user {}", notificationId, currentUser.getUserId());
        notificationApplicationService.markAsRead(notificationId, currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all as read",
            description = "Marks all notifications as read for the current user")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        log.info("Marking all notifications as read for user {}", currentUser.getUserId());
        notificationApplicationService.markAllAsRead(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
}
