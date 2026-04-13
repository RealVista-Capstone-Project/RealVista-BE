package com.sep.realvista.application.appointment.service;

import com.sep.realvista.application.appointment.dto.BookTourRequest;
import com.sep.realvista.application.notification.dto.SendNotificationRequest;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.application.service.EmailService;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.appointment.Appointment;
import com.sep.realvista.domain.listing.appointment.AppointmentService;
import com.sep.realvista.domain.listing.appointment.BookTourResult;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.notification.EntityType;
import com.sep.realvista.domain.user.notification.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AppointmentApplicationService {

    private final AppointmentService appointmentService;
    private final EmailService emailService;
    private final NotificationApplicationService notificationApplicationService;

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
        String appointmentsUrl = frontendUrl + "/appointments";

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
                notificationVars.put("senderPhone", sender.getPhone());
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
                                .eventType(EventType.NEW_TOUR_REQUEST)
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
}
