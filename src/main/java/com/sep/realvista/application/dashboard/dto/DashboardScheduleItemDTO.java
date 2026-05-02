package com.sep.realvista.application.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardScheduleItemDTO {
    private UUID appointmentId;
    private String title;
    private String address;
    private LocalDate date;
    private LocalTime time;
    private String type;
    private String status;
}
