package com.sep.realvista.application.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCalendarDayResponse {
    private String date;
    private long total;
    private long tourCount;
    private long blockCount;
    private boolean hasItems;
}
