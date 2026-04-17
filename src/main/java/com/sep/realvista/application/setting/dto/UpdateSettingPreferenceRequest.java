package com.sep.realvista.application.setting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSettingPreferenceRequest {
    private Boolean inAppEnabled;
    private Boolean emailEnabled;
    private Boolean pushEnabled;
    private Boolean contactViaEmail;
    private Boolean contactViaPhone;
    private Boolean hidePhoneNumber;
    private Boolean hideEmail;
    private Boolean autoRefreshEnabled;
}
