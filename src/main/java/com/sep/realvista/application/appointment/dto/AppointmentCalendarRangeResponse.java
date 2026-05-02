package com.sep.realvista.application.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCalendarRangeResponse {
    private String startDate;
    private String endDate;
    private String timezone;
}
