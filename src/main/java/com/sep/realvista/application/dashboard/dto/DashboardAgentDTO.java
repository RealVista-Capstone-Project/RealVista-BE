package com.sep.realvista.application.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAgentDTO {
    private UUID userId;
    private String fullName;
    private String avatarUrl;
    private String phone;
    private Long activeLeads;
    private String leadBadge;
}
