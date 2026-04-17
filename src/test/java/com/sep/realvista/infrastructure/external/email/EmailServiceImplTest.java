package com.sep.realvista.infrastructure.external.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private EmailProvider emailProvider;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    private static final String FROM = "noreply@realvista.com";
    private static final String TO = "test@example.com";
    private static final String SUBJECT = "Test Subject";
    private static final String TEXT = "Test Body";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", FROM);
    }

    @Test
    void sendSimpleMessage_shouldDelegateToProvider() {
        doNothing().when(emailProvider).sendSimple(any(), any(), any(), any());

        emailService.sendSimpleMessage(TO, SUBJECT, TEXT);

        verify(emailProvider).sendSimple(FROM, TO, SUBJECT, TEXT);
    }

    @Test
    void sendSimpleMessageAsync_shouldCompleteSuccessfully() throws ExecutionException, InterruptedException {
        doNothing().when(emailProvider).sendSimple(any(), any(), any(), any());

        CompletableFuture<Void> future = emailService.sendSimpleMessageAsync(TO, SUBJECT, TEXT);

        assertDoesNotThrow(() -> future.get());
        verify(emailProvider).sendSimple(FROM, TO, SUBJECT, TEXT);
    }

    @Test
    void sendHtmlMessage_shouldDelegateToProvider() {
        doNothing().when(emailProvider).sendHtml(any(), any(), any(), any());

        emailService.sendHtmlMessage(TO, SUBJECT, "<h1>Test</h1>");

        verify(emailProvider).sendHtml(FROM, TO, SUBJECT, "<h1>Test</h1>");
    }

    @Test
    void sendHtmlMessageAsync_shouldCompleteSuccessfully() throws ExecutionException, InterruptedException {
        doNothing().when(emailProvider).sendHtml(any(), any(), any(), any());

        CompletableFuture<Void> future = emailService.sendHtmlMessageAsync(TO, SUBJECT, "<h1>Test</h1>");

        assertDoesNotThrow(() -> future.get());
        verify(emailProvider).sendHtml(FROM, TO, SUBJECT, "<h1>Test</h1>");
    }

    @Test
    void sendTemplateMessage_shouldRenderTemplateAndDelegateToProvider() {
        when(templateEngine.process(eq("mail/test-template"), any(Context.class))).thenReturn("<h1>Template</h1>");
        doNothing().when(emailProvider).sendHtml(any(), any(), any(), any());

        emailService.sendTemplateMessage(TO, SUBJECT, "test-template", Map.of("name", "User"));

        verify(templateEngine).process(eq("mail/test-template"), any(Context.class));
        verify(emailProvider).sendHtml(FROM, TO, SUBJECT, "<h1>Template</h1>");
    }

    @Test
    void sendTemplateMessageAsync_shouldCompleteSuccessfully() throws ExecutionException, InterruptedException {
        when(templateEngine.process(eq("mail/test-template"), any(Context.class))).thenReturn("<h1>Template</h1>");
        doNothing().when(emailProvider).sendHtml(any(), any(), any(), any());

        CompletableFuture<Void> future = emailService.sendTemplateMessageAsync(TO, SUBJECT, "test-template", Map.of("name", "User"));

        assertDoesNotThrow(() -> future.get());
        verify(emailProvider).sendHtml(FROM, TO, SUBJECT, "<h1>Template</h1>");
    }
}
