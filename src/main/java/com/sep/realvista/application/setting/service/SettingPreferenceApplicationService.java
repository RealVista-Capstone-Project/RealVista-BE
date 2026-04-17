package com.sep.realvista.application.setting.service;

import com.sep.realvista.application.setting.dto.SettingPreferenceResponse;
import com.sep.realvista.application.setting.dto.UpdateSettingPreferenceRequest;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.user.preference.SettingPreference;
import com.sep.realvista.domain.user.preference.SettingPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SettingPreferenceApplicationService {

    private final SettingPreferenceRepository settingPreferenceRepository;

    public void createDefaultSettingPreference(UUID userId) {
        if (settingPreferenceRepository.existsByUserId(userId)) {
            log.warn("SettingPreference already exists for userId: {}", userId);
            return;
        }
        SettingPreference setting = SettingPreference.builder()
                .userId(userId)
                .build();
        settingPreferenceRepository.save(setting);
        log.info("Default SettingPreference created for userId: {}", userId);
    }

    @Transactional(readOnly = true)
    public SettingPreferenceResponse getSettingPreference(UUID userId) {
        SettingPreference setting = settingPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("SettingPreference not found for user: " + userId));
        return toResponse(setting);
    }

    public SettingPreferenceResponse updateSettingPreference(UUID userId, UpdateSettingPreferenceRequest request) {
        SettingPreference setting = settingPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("SettingPreference not found for user: " + userId));

        setting.updateNotificationChannels(
                request.getInAppEnabled(),
                request.getEmailEnabled(),
                request.getPushEnabled()
        );

        setting.updatePrivacyAndAutomation(
                request.getContactViaEmail(),
                request.getContactViaPhone(),
                request.getHidePhoneNumber(),
                request.getHideEmail(),
                request.getAutoRefreshEnabled()
        );

        SettingPreference saved = settingPreferenceRepository.save(setting);
        log.info("SettingPreference updated for userId: {}", userId);
        return toResponse(saved);
    }

    private SettingPreferenceResponse toResponse(SettingPreference setting) {
        return SettingPreferenceResponse.builder()
                .settingPreferenceId(setting.getSettingPreferenceId())
                .userId(setting.getUserId())
                .inAppEnabled(setting.getInAppEnabled())
                .emailEnabled(setting.getEmailEnabled())
                .pushEnabled(setting.getPushEnabled())
                .contactViaEmail(setting.getContactViaEmail())
                .contactViaPhone(setting.getContactViaPhone())
                .hidePhoneNumber(setting.getHidePhoneNumber())
                .hideEmail(setting.getHideEmail())
                .autoRefreshEnabled(setting.getAutoRefreshEnabled())
                .createdAt(setting.getCreatedAt())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}
