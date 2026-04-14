package com.sep.realvista.unit.application.notification;

import com.sep.realvista.domain.user.notification.DeviceTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeviceTokenRepositoryTest {

    @Mock
    DeviceTokenRepository deviceTokenRepository;

    @Test
    @DisplayName("deactivateByFcmTokenIn exists on DeviceTokenRepository interface")
    void deactivateByFcmTokenIn_methodExists() {
        List<String> tokens = List.of("token-a", "token-b");
        deviceTokenRepository.deactivateByFcmTokenIn(tokens);
        verify(deviceTokenRepository).deactivateByFcmTokenIn(tokens);
    }
}
