package com.sep.realvista.infrastructure.external.email;

import com.sep.realvista.application.service.EmailService;
import com.sep.realvista.application.service.TemplateEngineService;
import com.sep.realvista.domain.user.notification.NotificationTemplate;
import com.sep.realvista.domain.user.notification.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final EmailProvider emailProvider;
    private final SpringTemplateEngine templateEngine;
    private final NotificationTemplateRepository templateRepository;
    private final TemplateEngineService dbTemplateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            emailProvider.sendSimple(fromEmail, to, subject, text);
            log.info("Simple email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to {}", to, e);
            throw e;
        }
    }

    @Async
    @Override
    public CompletableFuture<Void> sendSimpleMessageAsync(String to, String subject, String text) {
        try {
            sendSimpleMessage(to, subject, text);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public void sendHtmlMessage(String to, String subject, String htmlBody) {
        try {
            emailProvider.sendHtml(fromEmail, to, subject, htmlBody);
            log.info("HTML email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    @Async
    @Override
    public CompletableFuture<Void> sendHtmlMessageAsync(String to, String subject, String htmlBody) {
        try {
            sendHtmlMessage(to, subject, htmlBody);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public void sendTemplateMessage(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            String htmlBody = templateEngine.process("mail/" + templateName, context);
            sendHtmlMessage(to, subject, htmlBody);
        } catch (Exception e) {
            log.error("Failed to send template email '{}' to {}: {}", templateName, to, e.getMessage(), e);
            throw new RuntimeException("Failed to send template email: " + e.getMessage(), e);
        }
    }

    @Async
    @Override
    public CompletableFuture<Void> sendTemplateMessageAsync(String to, String subject, String templateName,
                                                            Map<String, Object> variables) {
        try {
            sendTemplateMessage(to, subject, templateName, variables);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Async template email '{}' to {} failed: {}", templateName, to, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public void sendDbTemplateMessage(String to, String templateKey, String language, Map<String, Object> variables) {
        try {
            NotificationTemplate template = templateRepository.findByTemplateKeyAndLanguage(templateKey, language)
                    .orElseThrow(() -> new RuntimeException("Email template not found: " 
                            + templateKey + " (" + language + ")"));

            TemplateEngineService.RenderedTemplate rendered = dbTemplateEngine.preview(
                    template.getTitle(),
                    template.getContentBody(),
                    variables
            );

            sendHtmlMessage(to, rendered.title(), rendered.body());
        } catch (Exception e) {
            log.error("Failed to send DB template email '{}' to {}: {}", templateKey, to, e.getMessage(), e);
            throw new RuntimeException("Failed to send DB template email: " + e.getMessage(), e);
        }
    }

    @Async
    @Override
    public CompletableFuture<Void> sendDbTemplateMessageAsync(String to, String templateKey, String language,
                                                              Map<String, Object> variables) {
        try {
            sendDbTemplateMessage(to, templateKey, language, variables);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Async DB template email '{}' to {} failed: {}", templateKey, to, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
