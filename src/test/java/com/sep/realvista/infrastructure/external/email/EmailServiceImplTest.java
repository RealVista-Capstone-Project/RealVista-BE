package com.sep.realvista.infrastructure.external.email;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender emailSender;

    @Mock
    private org.thymeleaf.spring6.SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    private final String to = "test@example.com";
    private final String subject = "Test Subject";
    private final String text = "Test Body";

    @BeforeEach
    void setUp() {
    }

    @Test
    void sendSimpleMessage_shouldSendEmail() {
        doNothing().when(emailSender).send(any(SimpleMailMessage.class));

        emailService.sendSimpleMessage(to, subject, text);

        verify(emailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendSimpleMessageAsync_shouldSendEmailAsynchronously() throws ExecutionException, InterruptedException {
        doNothing().when(emailSender).send(any(SimpleMailMessage.class));

        CompletableFuture<Void> future = emailService.sendSimpleMessageAsync(to, subject, text);

        future.get(); // Wait for completion
        assertDoesNotThrow(() -> future.get());
        verify(emailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendHtmlMessage_shouldSendEmail() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(emailSender).send(any(MimeMessage.class));

        emailService.sendHtmlMessage(to, subject, "<h1>Test</h1>");

        verify(emailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendHtmlMessageAsync_shouldSendEmailAsynchronously() throws ExecutionException, InterruptedException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(emailSender).send(any(MimeMessage.class));

        CompletableFuture<Void> future = emailService.sendHtmlMessageAsync(to, subject, "<h1>Test</h1>");

        future.get(); // Wait for completion
        assertDoesNotThrow(() -> future.get());
        verify(emailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendTemplateMessage_shouldSendEmail() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(emailSender).send(any(MimeMessage.class));
        when(templateEngine.process(any(String.class), any(org.thymeleaf.context.Context.class))).thenReturn("<h1>Template</h1>");

        emailService.sendTemplateMessage(to, subject, "test-template", java.util.Map.of("name", "User"));

        verify(emailSender).send(any(MimeMessage.class));
        verify(templateEngine).process(any(String.class), any(org.thymeleaf.context.Context.class));
    }
}
