package com.sep.realvista.application.appointment.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AppointmentSummaryResponse {
    long totalAppointments;
    long pendingAppointments;
    long acceptedAppointments;
    long rejectedAppointments;
    long canceledAppointments;
    long completedAppointments;
    long upcomingAppointments;
}
