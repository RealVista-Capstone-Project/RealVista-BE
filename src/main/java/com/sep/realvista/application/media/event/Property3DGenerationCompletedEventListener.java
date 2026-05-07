package com.sep.realvista.application.media.event;

import com.sep.realvista.application.notification.dto.SendNotificationRequest;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.domain.property.event.Property3DGenerationCompletedEvent;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.notification.EntityType;
import com.sep.realvista.domain.user.notification.EventType;
import com.sep.realvista.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class Property3DGenerationCompletedEventListener {

    private final NotificationApplicationService notificationService;
    private final UserRepository userRepository;

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleProperty3DGenerationCompleted(Property3DGenerationCompletedEvent event) {
        log.info("Received Property3DGenerationCompletedEvent for Property ID: {}, Success: {}", 
                event.getPropertyId(), event.isSuccess());

        User user = userRepository.findById(event.getUploaderId()).orElse(null);
        if (user == null) {
            log.warn("User {} not found, skipping notification for 3D Generation", event.getUploaderId());
            return;
        }

        String title = event.isSuccess() ? "Tour 3D đã được tạo thành công" : "Tạo Tour 3D thất bại";
        String message = event.isSuccess() 
                ? "Tour 3D của bạn đã sẵn sàng để xem cho bất động sản."
                : "Không thể tạo Tour 3D cho bất động sản của bạn. "
                        + "Vui lòng thử lại với ảnh khác.";
        
        EventType eventType = event.isSuccess() 
                ? EventType.PROPERTY_3D_GENERATED : EventType.PROPERTY_3D_FAILED;

        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(user.getUserId())
                .userEmail(user.getEmail().getValue())
                .title(title)
                .message(message)
                .eventType(eventType)
                .entityType(EntityType.PROPERTY)
                .entityId(event.getPropertyId())
                .build();

        notificationService.sendNotification(request);
    }
}
