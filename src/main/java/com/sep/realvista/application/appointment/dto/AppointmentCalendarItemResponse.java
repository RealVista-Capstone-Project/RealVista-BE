package com.sep.realvista.application.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCalendarItemResponse {
    private String appointmentId;
    private String listingId;
    private String listingName;
    private String listingAddress;
    private String startTime;
    private String endTime;
    private String status;
    private String appointmentType;
}
