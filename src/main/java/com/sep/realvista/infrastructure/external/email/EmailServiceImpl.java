package com.sep.realvista.infrastructure.external.email;

import com.sep.realvista.application.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;
    private final org.thymeleaf.spring6.SpringTemplateEngine templateEngine;

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
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
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = isHtml

            emailSender.send(message);
            log.info("HTML Email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
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
    public void sendTemplateMessage(String to, String subject, String templateName, 
                                    java.util.Map<String, Object> variables) {
        try {
            org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
            context.setVariables(variables);
            
            // Assuming templates are in "mail/" subfolder
            String htmlBody = templateEngine.process("mail/" + templateName, context);
            
            sendHtmlMessage(to, subject, htmlBody);
        } catch (Exception e) {
            log.error("Failed to send template email to {}", to, e);
            throw new RuntimeException("Failed to send template email", e);
        }
    }

    @Async
    @Override
    public CompletableFuture<Void> sendTemplateMessageAsync(String to, String subject, String templateName, 
                                                            java.util.Map<String, Object> variables) {
        try {
            sendTemplateMessage(to, subject, templateName, variables);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
