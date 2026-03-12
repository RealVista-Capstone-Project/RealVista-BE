package com.sep.realvista.presentation.rest.me;

import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.application.setting.dto.SettingPreferenceResponse;
import com.sep.realvista.application.setting.dto.UpdateSettingPreferenceRequest;
import com.sep.realvista.application.setting.service.SettingPreferenceApplicationService;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.presentation.common.util.ControllerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "Endpoints for managing user setting preferences")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class SettingPreferenceController {

    private final SettingPreferenceApplicationService settingPreferenceApplicationService;
    private final ControllerUtils controllerUtils;

    @GetMapping
    @Operation(summary = "Get setting preferences",
            description = "Returns the current user's notification and preference settings")
    public ResponseEntity<ApiResponse<SettingPreferenceResponse>> getSettings(Authentication authentication) {
        controllerUtils.initializeTraceId();
        User currentUser = controllerUtils.getCurrentUser(authentication);
        SettingPreferenceResponse response =
                settingPreferenceApplicationService.getSettingPreference(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    @Operation(summary = "Update setting preferences",
            description = "Updates the current user's notification and preference settings")
    public ResponseEntity<ApiResponse<SettingPreferenceResponse>> updateSettings(
            Authentication authentication,
            @RequestBody UpdateSettingPreferenceRequest request
    ) {
        controllerUtils.initializeTraceId();
        User currentUser = controllerUtils.getCurrentUser(authentication);
        SettingPreferenceResponse response =
                settingPreferenceApplicationService.updateSettingPreference(currentUser.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Settings updated successfully", response));
    }
}
