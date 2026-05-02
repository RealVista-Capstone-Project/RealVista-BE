package com.sep.realvista.application.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDashboardSnapshotResponse {
    private AppointmentCalendarRangeResponse range;
    private List<AppointmentCalendarDayResponse> calendarDays;
    private List<AppointmentCalendarItemResponse> appointments;
}
