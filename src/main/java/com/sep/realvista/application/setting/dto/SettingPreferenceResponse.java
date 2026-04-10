package com.sep.realvista.application.setting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingPreferenceResponse {
    private UUID settingPreferenceId;
    private UUID userId;
    private Boolean inAppEnabled;
    private Boolean emailEnabled;
    private Boolean pushEnabled;
    private Boolean contactViaEmail;
    private Boolean contactViaPhone;
    private Boolean hidePhoneNumber;
    private Boolean hideEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
