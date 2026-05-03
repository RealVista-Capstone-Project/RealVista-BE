package com.sep.realvista.unit.application.auth;

import com.sep.realvista.application.auth.dto.ResetPasswordRequest;
import com.sep.realvista.application.auth.mapper.AuthenticationMapper;
import com.sep.realvista.application.auth.service.AuthService;
import com.sep.realvista.application.auth.service.TokenService;
import com.sep.realvista.application.service.EmailService;
import com.sep.realvista.application.service.OtpService;
import com.sep.realvista.application.user.service.UserApplicationService;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.UserStatus;
import com.sep.realvista.infrastructure.security.oauth2.GoogleTokenVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServicePasswordResetUnitTest {

    @Mock
    private UserApplicationService userApplicationService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenService tokenService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationMapper authenticationMapper;
    @Mock
    private GoogleTokenVerifier googleTokenVerifier;
    @Mock
    private OtpService otpService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Test
    void requestPasswordReset_sendsEmailForActiveUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .userId(userId)
                .email(Email.of("user@example.com"))
                .passwordHash("hash")
                .businessName("Acme")
                .firstName("A")
                .lastName("B")
                .status(UserStatus.ACTIVE)
                .userRoles(Collections.emptySet())
                .build();

        when(userRepository.findByEmailValue("user@example.com")).thenReturn(Optional.of(user));

        authService.requestPasswordReset("User@Example.com", "en", "http://localhost:3000");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(otpService).store(keyCaptor.capture(), eq(userId.toString()), eq(30));
        assertThat(keyCaptor.getValue()).startsWith("pwdreset:");

        verify(emailService).sendDbTemplateMessageAsync(
                eq("user@example.com"),
                eq("PASSWORD_RESET"),
                eq("en"),
                argThat((Map<String, Object> m) ->
                        m.containsKey("resetLink") && m.containsKey("userName") && m.containsKey("expiryMinutes"))
        );
    }

    @Test
    void requestPasswordReset_skipsWhenEmailUnknown() {
        when(userRepository.findByEmailValue("nope@example.com")).thenReturn(Optional.empty());

        authService.requestPasswordReset("nope@example.com", "vi", "http://localhost:3000");

        verify(otpService, never()).store(anyString(), anyString(), anyInt());
        verify(emailService, never()).sendDbTemplateMessageAsync(anyString(), anyString(), anyString(), any());
    }

    @Test
    void requestPasswordReset_skipsWhenSuspended() {
        User user = User.builder()
                .userId(UUID.randomUUID())
                .email(Email.of("x@example.com"))
                .passwordHash("hash")
                .businessName("X")
                .status(UserStatus.SUSPENDED)
                .userRoles(Collections.emptySet())
                .build();
        when(userRepository.findByEmailValue("x@example.com")).thenReturn(Optional.of(user));

        authService.requestPasswordReset("x@example.com", "vi", "http://localhost:3000");

        verify(otpService, never()).store(anyString(), anyString(), anyInt());
        verify(emailService, never()).sendDbTemplateMessageAsync(anyString(), anyString(), anyString(), any());
    }

    @Test
    void resetPasswordWithToken_invokesUserServiceAndRemovesToken() {
        UUID userId = UUID.randomUUID();
        when(otpService.get("pwdreset:tok")).thenReturn(userId.toString());

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("tok");
        req.setNewPassword("Newpass1!");

        authService.resetPasswordWithToken(req);

        verify(userApplicationService).resetPasswordForgotten(userId, "Newpass1!");
        verify(otpService).remove("pwdreset:tok");
    }

    @Test
    void resetPasswordWithToken_throwsWhenTokenMissing() {
        when(otpService.get("pwdreset:bad")).thenReturn(null);

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("bad");
        req.setNewPassword("Newpass1!");

        assertThatThrownBy(() -> authService.resetPasswordWithToken(req))
                .isInstanceOf(BusinessConflictException.class)
                .hasFieldOrPropertyWithValue("errorCode", "ERROR_INVALID_OR_EXPIRED_RESET_TOKEN");

        verify(userApplicationService, never()).resetPasswordForgotten(any(), anyString());
    }
}
