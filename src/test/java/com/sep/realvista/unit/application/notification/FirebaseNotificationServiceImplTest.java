package com.sep.realvista.unit.application.notification;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import com.sep.realvista.domain.user.notification.DeviceTokenRepository;
import com.sep.realvista.infrastructure.external.firebase.FirebaseNotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseNotificationServiceImplTest {

    @Mock DeviceTokenRepository deviceTokenRepository;

    FirebaseNotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FirebaseNotificationServiceImpl(deviceTokenRepository);
    }

    @Test
    @DisplayName("deactivates UNREGISTERED and INVALID_ARGUMENT tokens after multicast")
    void sendNotificationToMultipleDevices_deactivatesStaleTokens() throws Exception {
        // Two stale tokens: one UNREGISTERED, one INVALID_ARGUMENT
        com.google.firebase.messaging.FirebaseMessagingException ex1 =
                mock(com.google.firebase.messaging.FirebaseMessagingException.class);
        when(ex1.getMessagingErrorCode()).thenReturn(MessagingErrorCode.UNREGISTERED);
        SendResponse unregistered = mock(SendResponse.class);
        when(unregistered.isSuccessful()).thenReturn(false);
        when(unregistered.getException()).thenReturn(ex1);

        com.google.firebase.messaging.FirebaseMessagingException ex2 =
                mock(com.google.firebase.messaging.FirebaseMessagingException.class);
        when(ex2.getMessagingErrorCode()).thenReturn(MessagingErrorCode.INVALID_ARGUMENT);
        SendResponse invalidArg = mock(SendResponse.class);
        when(invalidArg.isSuccessful()).thenReturn(false);
        when(invalidArg.getException()).thenReturn(ex2);

        BatchResponse batchResponse = mock(BatchResponse.class);
        when(batchResponse.getSuccessCount()).thenReturn(0);
        when(batchResponse.getFailureCount()).thenReturn(2);
        when(batchResponse.getResponses()).thenReturn(List.of(unregistered, invalidArg));

        FirebaseMessaging fbMessaging = mock(FirebaseMessaging.class);
        when(fbMessaging.sendEachForMulticast(any())).thenReturn(batchResponse);

        List<String> tokens = List.of("stale-token-1", "stale-token-2");

        try (MockedStatic<com.google.firebase.FirebaseApp> appMock =
                     mockStatic(com.google.firebase.FirebaseApp.class);
             MockedStatic<FirebaseMessaging> msgMock = mockStatic(FirebaseMessaging.class)) {

            appMock.when(com.google.firebase.FirebaseApp::getApps)
                   .thenReturn(List.of(mock(com.google.firebase.FirebaseApp.class)));
            msgMock.when(FirebaseMessaging::getInstance).thenReturn(fbMessaging);

            service.sendNotificationToMultipleDevices(tokens, "T", "B", Map.of());
        }

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(deviceTokenRepository).deactivateByFcmTokenIn(captor.capture());
        assertThat(captor.getValue()).containsExactlyInAnyOrder("stale-token-1", "stale-token-2");
    }

    @Test
    @DisplayName("does not call deactivateByFcmTokenIn when all sends succeed")
    void sendNotificationToMultipleDevices_noDeactivationOnSuccess() throws Exception {
        BatchResponse batchResponse = mock(BatchResponse.class);
        when(batchResponse.getSuccessCount()).thenReturn(1);
        when(batchResponse.getFailureCount()).thenReturn(0);

        FirebaseMessaging fbMessaging = mock(FirebaseMessaging.class);
        when(fbMessaging.sendEachForMulticast(any())).thenReturn(batchResponse);

        try (MockedStatic<com.google.firebase.FirebaseApp> appMock =
                     mockStatic(com.google.firebase.FirebaseApp.class);
             MockedStatic<FirebaseMessaging> msgMock = mockStatic(FirebaseMessaging.class)) {

            appMock.when(com.google.firebase.FirebaseApp::getApps)
                   .thenReturn(List.of(mock(com.google.firebase.FirebaseApp.class)));
            msgMock.when(FirebaseMessaging::getInstance).thenReturn(fbMessaging);

            service.sendNotificationToMultipleDevices(List.of("good-token"), "T", "B", Map.of());
        }

        verify(deviceTokenRepository, never()).deactivateByFcmTokenIn(any());
    }
}
