package com.sep.realvista.application.appointment.service;

import com.sep.realvista.application.appointment.dto.BookTourRequest;
import com.sep.realvista.application.appointment.dto.AppointmentResponse;
import com.sep.realvista.application.appointment.dto.SyncBlocksRequest;
import com.sep.realvista.application.appointment.dto.UpdateAppointmentStatusRequest;
import com.sep.realvista.application.notification.dto.SendNotificationRequest;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.application.service.EmailService;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.appointment.Appointment;
import com.sep.realvista.domain.listing.appointment.AppointmentService;
import com.sep.realvista.domain.listing.appointment.AppointmentStatus;
import com.sep.realvista.domain.listing.appointment.BookTourResult;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.notification.EntityType;
import com.sep.realvista.domain.user.notification.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AppointmentApplicationService {

    private final AppointmentService appointmentService;
    private final EmailService emailService;
    private final NotificationApplicationService notificationApplicationService;
    private final com.sep.realvista.domain.user.preference.SettingPreferenceRepository settingPreferenceRepository;
    private final com.sep.realvista.infrastructure.service.NotificationMessageService notificationMessageService;

    @Value("${spring.application.frontend.url}")
    private String frontendUrl;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale.forLanguageTag("vi"));
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableSlots(UUID listingId, LocalDate date) {
        log.info("Fetching available slots for listingId: {} on date: {}", listingId, date);
        return appointmentService.getAvailableSlots(listingId, date);
    }

    public void bookTour(UUID userId, BookTourRequest request) {
        log.info("Booking tour for listingId: {}, userId: {}", request.getListingId(), userId);
        BookTourResult result = appointmentService.bookTour(
                request.getListingId(),
                userId,
                request.getSelectedSlots(),
                request.getNotes()
        );

        sendTourBookingEmails(result);
        sendTourBookingNotifications(result);
    }

    private void sendTourBookingEmails(BookTourResult result) {
        Listing listing = result.listing();
        User sender = result.sender();
        User owner = result.owner();

        String listingName = listing.getName();
        String propertyAddress = buildPropertyAddress(listing);
        String appointmentsUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .pathSegment("vi", "appointments")
                .build()
                .toUriString();

        for (Appointment appointment : result.appointments()) {
            String tourDate = appointment.getStartTime().format(DATE_FORMATTER);
            String tourTime = appointment.getStartTime().format(TIME_FORMATTER)
                    + " - " + appointment.getEndTime().format(TIME_FORMATTER);
            String status = "Chờ xác nhận";

            // Confirmation email to the booker
            try {
                Map<String, Object> confirmationVars = new HashMap<>();
                confirmationVars.put("senderName", sender.getFullName());
                confirmationVars.put("listingName", listingName);
                confirmationVars.put("propertyAddress", propertyAddress);
                confirmationVars.put("tourDate", tourDate);
                confirmationVars.put("tourTime", tourTime);
                confirmationVars.put("status", status);
                confirmationVars.put("notes", appointment.getSenderNotes());
                confirmationVars.put("viewAppointmentUrl", appointmentsUrl);

                emailService.sendTemplateMessageAsync(
                        sender.getEmail().getValue(),
                        "Đặt lịch tham quan: " + listingName,
                        "tour-booking-confirmation",
                        confirmationVars
                );
                log.info("Sent tour confirmation email to sender: {}", sender.getEmail().getValue());
            } catch (Exception e) {
                log.error("Failed to send tour confirmation email to sender {}: {}",
                        sender.getEmail().getValue(), e.getMessage(), e);
            }

            // Notification email to the property owner
            try {
                Map<String, Object> notificationVars = new HashMap<>();
                notificationVars.put("ownerName", owner.getFullName());
                notificationVars.put("senderName", sender.getFullName());
                notificationVars.put("senderEmail", sender.getEmail().getValue());
                notificationVars.put("senderPhone",
                        sender.getPhone() != null ? sender.getPhone() : "N/A");
                notificationVars.put("listingName", listingName);
                notificationVars.put("propertyAddress", propertyAddress);
                notificationVars.put("tourDate", tourDate);
                notificationVars.put("tourTime", tourTime);
                notificationVars.put("status", status);
                notificationVars.put("notes", appointment.getSenderNotes());
                notificationVars.put("viewAppointmentUrl", appointmentsUrl);

                emailService.sendTemplateMessageAsync(
                        owner.getEmail().getValue(),
                        "Yêu cầu tham quan mới: " + listingName,
                        "tour-booking-notification",
                        notificationVars
                );
                log.info("Sent tour notification email to owner: {}", owner.getEmail().getValue());
            } catch (Exception e) {
                log.error("Failed to send tour notification email to owner {}: {}",
                        owner.getEmail().getValue(), e.getMessage(), e);
            }
        }
    }

    private void sendTourBookingNotifications(BookTourResult result) {
        Listing listing = result.listing();
        User sender = result.sender();
        User owner = result.owner();

        String listingName = listing.getName();

        for (Appointment appointment : result.appointments()) {
            String tourDate = appointment.getStartTime().format(DATE_FORMATTER);
            String tourTime = appointment.getStartTime().format(TIME_FORMATTER)
                    + " - " + appointment.getEndTime().format(TIME_FORMATTER);

            // Build metadata for deep linking on frontend/mobile
            Map<String, String> metadata = new HashMap<>();
            metadata.put("listing_id", listing.getListingId().toString());
            metadata.put("appointment_id", appointment.getAppointmentId().toString());
            metadata.put("tour_date", tourDate);
            metadata.put("tour_time", tourTime);

            // In-app + push notification to the OWNER (most important - they need to respond)
            try {
                String ownerTitle = "Yêu cầu tham quan mới";
                String ownerMessage = sender.getFullName() + " muốn tham quan \""
                        + listingName + "\" vào " + tourDate + " lúc " + tourTime;

                notificationApplicationService.sendNotification(
                        SendNotificationRequest.builder()
                                .userId(owner.getUserId())
                                .userEmail(owner.getEmail().getValue())
                                .title(ownerTitle)
                                .message(ownerMessage)
                                .eventType(EventType.NEW_TOUR_REQUEST)
                                .entityType(EntityType.APPOINTMENT)
                                .entityId(appointment.getAppointmentId())
                                .metadata(metadata)
                                .build()
                );
            } catch (Exception e) {
                log.error("Failed to send in-app/push notification to owner {}: {}",
                        owner.getUserId(), e.getMessage(), e);
            }

            // In-app + push notification to the SENDER (confirmation)
            try {
                String senderTitle = "Đặt lịch tham quan thành công";
                String senderMessage = "Bạn đã đặt lịch tham quan \""
                        + listingName + "\" vào " + tourDate + " lúc " + tourTime
                        + ". Đang chờ xác nhận.";

                notificationApplicationService.sendNotification(
                        SendNotificationRequest.builder()
                                .userId(sender.getUserId())
                                .userEmail(sender.getEmail().getValue())
                                .title(senderTitle)
                                .message(senderMessage)
                                .eventType(EventType.APPOINTMENT_CONFIRMED)
                                .entityType(EntityType.APPOINTMENT)
                                .entityId(appointment.getAppointmentId())
                                .metadata(metadata)
                                .build()
                );
            } catch (Exception e) {
                log.error("Failed to send in-app/push notification to sender {}: {}",
                        sender.getUserId(), e.getMessage(), e);
            }
        }
    }

    private String buildPropertyAddress(Listing listing) {
        try {
            if (listing.getProperty() != null) {
                String streetAddress = listing.getProperty().getStreetAddress();
                if (listing.getProperty().getLocation() != null) {
                    return streetAddress + ", " + listing.getProperty().getLocation().getName();
                }
                return streetAddress;
            }
        } catch (Exception e) {
            log.warn("Could not resolve property address for listing {}: {}",
                    listing.getListingId(), e.getMessage());
        }
        return "";
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointments(UUID userId, LocalDateTime start, 
                                                     LocalDateTime end, List<AppointmentStatus> statuses) {
        List<Appointment> appointments = appointmentService.getAppointmentsByUserId(userId, start, end, statuses);
        return appointments.stream()
                .map(appt -> mapToResponse(appt, userId))
                .collect(Collectors.toList());
    }

    public AppointmentResponse updateAppointmentStatus(UUID userId, UUID appointmentId, 
                                                       UpdateAppointmentStatusRequest request) {
        AppointmentStatus newStatus = AppointmentStatus.valueOf(request.getStatus().toUpperCase());
        Appointment updated = appointmentService.updateAppointmentStatus(
                appointmentId, userId, newStatus, request.getReason());

        if (updated.isTour()) {
            sendAppointmentStatusNotifications(updated);
            sendAppointmentStatusEmails(updated);
        }

        return mapToResponse(updated, userId);
    }

    private void sendAppointmentStatusNotifications(Appointment appointment) {
        Listing listing = appointment.getListing();
        User sender = appointment.getSender();
        User receiver = appointment.getReceiver();

        if (listing == null || sender == null || receiver == null) {
            log.warn("Missing data for appointment status notification: listing={}, sender={}, receiver={}",
                    listing != null, sender != null, receiver != null);
            return;
        }

        String listingName = listing.getName();
        String tourDate = appointment.getStartTime().format(DATE_FORMATTER);
        String tourTime = appointment.getStartTime().format(TIME_FORMATTER)
                + " - " + appointment.getEndTime().format(TIME_FORMATTER);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("listing_id", listing.getListingId().toString());
        metadata.put("appointment_id", appointment.getAppointmentId().toString());
        metadata.put("tour_date", tourDate);
        metadata.put("tour_time", tourTime);

        UUID notifyUserId = null;
        String notifyUserEmail = null;
        String title = "";
        String message = "";
        EventType eventType = null;
        String lang = "vi";

        switch (appointment.getStatus()) {
            case ACCEPTED -> {
                notifyUserId = sender.getUserId();
                notifyUserEmail = sender.getEmail().getValue();
                lang = getUserLanguage(notifyUserId);
                title = notificationMessageService.getMessage("APPOINTMENT_ACCEPTED_TITLE", lang);
                message = String.format(notificationMessageService.getMessage("APPOINTMENT_ACCEPTED_MESSAGE", lang),
                        listingName, tourDate, tourTime);
                eventType = EventType.APPOINTMENT_CONFIRMED;
            }
            case REJECTED -> {
                notifyUserId = sender.getUserId();
                notifyUserEmail = sender.getEmail().getValue();
                lang = getUserLanguage(notifyUserId);
                title = notificationMessageService.getMessage("APPOINTMENT_REJECTED_TITLE", lang);
                message = String.format(notificationMessageService.getMessage("APPOINTMENT_REJECTED_MESSAGE", lang),
                        listingName, tourDate, tourTime);
                if (appointment.getRejectionReason() != null && !appointment.getRejectionReason().isBlank()) {
                    message += " " + (lang.equals("en") ? "Reason: " : "Lý do: ") + appointment.getRejectionReason();
                }
                eventType = EventType.APPOINTMENT_REJECTED;
            }
            case CANCELED -> {
                boolean cancelledBySender = appointment.getCanceledByUserId().equals(sender.getUserId());
                User actor = cancelledBySender ? sender : receiver;
                User recipient = cancelledBySender ? receiver : sender;

                notifyUserId = recipient.getUserId();
                notifyUserEmail = recipient.getEmail().getValue();
                lang = getUserLanguage(notifyUserId);
                title = notificationMessageService.getMessage("APPOINTMENT_CANCELLED_TITLE", lang);
                message = String.format(notificationMessageService.getMessage("APPOINTMENT_CANCELLED_MESSAGE", lang),
                        actor.getFullName(), listingName, tourDate, tourTime);
                if (appointment.getCancellationReason() != null && !appointment.getCancellationReason().isBlank()) {
                    message += " " + (lang.equals("en") ? "Reason: " : "Lý do: ") + appointment.getCancellationReason();
                }
                eventType = EventType.APPOINTMENT_CANCELLED;
            }
            case PENDING, COMPLETED -> {
                // No notification for PENDING or COMPLETED from this method
            }
            default -> log.debug("No notification logic for status: {}", appointment.getStatus());
        }

        if (notifyUserId != null) {
            try {
                notificationApplicationService.sendNotification(
                        SendNotificationRequest.builder()
                                .userId(notifyUserId)
                                .userEmail(notifyUserEmail)
                                .title(title)
                                .message(message)
                                .eventType(eventType)
                                .entityType(EntityType.APPOINTMENT)
                                .entityId(appointment.getAppointmentId())
                                .metadata(metadata)
                                .build()
                );
            } catch (Exception e) {
                log.error("Failed to send status change notification to user {}: {}", notifyUserId, e.getMessage(), e);
            }
        }
    }

    private void sendAppointmentStatusEmails(Appointment appointment) {
        Listing listing = appointment.getListing();
        User sender = appointment.getSender();
        User receiver = appointment.getReceiver();

        if (listing == null || sender == null || receiver == null) {
            return;
        }

        String listingName = listing.getName();
        String tourDate = appointment.getStartTime().format(DATE_FORMATTER);
        String tourTime = appointment.getStartTime().format(TIME_FORMATTER)
                + " - " + appointment.getEndTime().format(TIME_FORMATTER);
        String propertyAddress = buildPropertyAddress(listing);
        String appointmentsUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .pathSegment("vi", "appointments")
                .build()
                .toUriString();

        User recipient = null;
        String subject = "";
        String template = "";
        Map<String, Object> vars = new HashMap<>();

        switch (appointment.getStatus()) {
            case ACCEPTED -> {
                recipient = sender;
                subject = "Lịch tham quan được chấp nhận: " + listingName;
                template = "tour-booking-status-change";
                vars.put("status", "Đã chấp nhận");
            }
            case REJECTED -> {
                recipient = sender;
                subject = "Lịch tham quan bị từ chối: " + listingName;
                template = "tour-booking-status-change";
                vars.put("status", "Đã từ chối");
                vars.put("reason", appointment.getRejectionReason());
            }
            case CANCELED -> {
                boolean cancelledBySender = appointment.getCanceledByUserId().equals(sender.getUserId());
                recipient = cancelledBySender ? receiver : sender;
                User actor = cancelledBySender ? sender : receiver;
                subject = "Lịch tham quan đã bị hủy: " + listingName;
                template = "tour-booking-status-change";
                vars.put("status", "Đã hủy");
                vars.put("actorName", actor.getFullName());
                vars.put("reason", appointment.getCancellationReason());
            }
            case PENDING, COMPLETED -> {
                // No email for PENDING or COMPLETED from this method
            }
            default -> log.debug("No email logic for status: {}", appointment.getStatus());
        }

        if (recipient != null) {
            try {
                vars.put("recipientName", recipient.getFullName());
                vars.put("listingName", listingName);
                vars.put("propertyAddress", propertyAddress);
                vars.put("tourDate", tourDate);
                vars.put("tourTime", tourTime);
                vars.put("viewAppointmentUrl", appointmentsUrl);

                emailService.sendTemplateMessageAsync(
                        recipient.getEmail().getValue(),
                        subject,
                        template,
                        vars
                );
            } catch (Exception e) {
                log.error("Failed to send status change email to {}: {}",
                        recipient.getEmail().getValue(), e.getMessage());
            }
        }
    }


    
    public void syncBlocks(UUID userId, SyncBlocksRequest request) {
        appointmentService.syncBlocks(userId, request.getStartDate(), request.getEndDate(), request.getBlocks());
    }

    public void deleteAppointment(UUID userId, UUID appointmentId) {
        appointmentService.deleteAppointment(userId, appointmentId);
    }

    public void cancelActiveAppointmentsByListingId(UUID listingId, UUID ownerId, String reason) {
        log.info("Starting mass cancellation for listing {}. Reason: {}", listingId, reason);
        
        List<Appointment> cancelledList = appointmentService
                .cancelActiveAppointmentsByListingId(listingId, ownerId, reason);
        
        for (Appointment appt : cancelledList) {
            if (appt.isTour()) {
                try {
                    sendAppointmentStatusNotifications(appt);
                    sendAppointmentStatusEmails(appt);
                } catch (Exception e) {
                    log.error("Failed to notify user about mass cancellation for appointment {}: {}",
                            appt.getAppointmentId(), e.getMessage());
                }
            }
        }
    }

    public void notifyUnpublishedListing(Listing listing) {
        String listingName = listing.getName();
        List<Appointment> activeAppointments = appointmentService.getAppointmentsByListingIdAndStatuses(
                listing.getListingId(), List.of(AppointmentStatus.PENDING, AppointmentStatus.ACCEPTED));

        for (Appointment appointment : activeAppointments) {
            User sender = appointment.getSender();
            if (sender == null) {
                continue;
            }

            String lang = getUserLanguage(sender.getUserId());
            String title = notificationMessageService.getMessage("LISTING_UNPUBLISHED_TITLE", lang);
            String message = notificationMessageService.getMessage("LISTING_UNPUBLISHED_MESSAGE", lang, listingName);

            try {
                notificationApplicationService.sendNotification(
                        SendNotificationRequest.builder()
                                .userId(sender.getUserId())
                                .userEmail(sender.getEmail().getValue())
                                .title(title)
                                .message(message)
                                .eventType(EventType.LISTING_UNPUBLISHED)
                                .entityType(EntityType.APPOINTMENT)
                                .entityId(appointment.getAppointmentId())
                                .build()
                );
            } catch (Exception e) {
                log.error("Failed to notify user {} about unpublished listing: {}", sender.getUserId(), e.getMessage());
            }
        }
    }

    private String getUserLanguage(UUID userId) {
        if (userId == null) {
            return "vi";
        }
        return settingPreferenceRepository.findByUserId(userId)
                .map(com.sep.realvista.domain.user.preference.SettingPreference::getPreferredLanguage)
                .orElse("vi");
    }
    
    private AppointmentResponse mapToResponse(Appointment appt, UUID userId) {
// ...
        return AppointmentResponse.builder()
                .appointmentId(appt.getAppointmentId())
                .listingId(appt.getListingId())
                .listingName(appt.getListing() != null ? appt.getListing().getName() : null)
                .listingAddress(appt.getListing() != null ? buildPropertyAddress(appt.getListing()) : null)
                .senderId(appt.getSenderId())
                .senderName(appt.getSender() != null ? appt.getSender().getFullName() : null)
                .receiverId(appt.getReceiverId())
                .receiverName(appt.getReceiver() != null ? appt.getReceiver().getFullName() : null)
                .startTime(appt.getStartTime())
                .endTime(appt.getEndTime())
                .status(appt.getStatus() != null ? appt.getStatus().name() : null)
                .appointmentType(appt.getAppointmentType() != null ? appt.getAppointmentType().name() : null)
                .senderNotes(appt.getSenderNotes())
                .rejectionReason(appt.getRejectionReason())
                .cancellationReason(appt.getCancellationReason())
                .canceledByUserId(appt.getCanceledByUserId())
                .isSender(appt.getSenderId() != null && appt.getSenderId().equals(userId))
                .build();
    }
}
