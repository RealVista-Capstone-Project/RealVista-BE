package com.sep.realvista.presentation.rest.test;

import com.sep.realvista.application.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/test/email")
@RequiredArgsConstructor
@Tag(name = "Test Email", description = "Endpoints for testing email service (Dev only)")
@Profile({"dev", "test"}) // Only enable in dev/test profiles
/**
 * Controller for testing email functionality.
 * <p>
 * <strong>NOTE:</strong> This controller is for testing purposes ONLY and will be removed in future versions.
 * Do not rely on these endpoints for production logic.
 */
public class EmailTestController {

    private final EmailService emailService;

    @PostMapping("/simple")
    @Operation(summary = "Send simple text email")
    public ResponseEntity<String> sendSimpleEmail(@RequestParam String to) {
        emailService.sendSimpleMessage(to, "Test Simple Email", "This is a test email from RealVista.");
        return ResponseEntity.ok("Simple email sent to " + to);
    }

    @PostMapping("/html")
    @Operation(summary = "Send HTML email")
    public ResponseEntity<String> sendHtmlEmail(@RequestParam String to) {
        String htmlBody = "<h1>Test HTML Email</h1><p>This is a <b>bold</b> test email.</p>";
        emailService.sendHtmlMessage(to, "Test HTML Email", htmlBody);
        return ResponseEntity.ok("HTML email sent to " + to);
    }

    @PostMapping("/template")
    @Operation(summary = "Send Template email")
    public ResponseEntity<String> sendTemplateEmail(@RequestParam String to) {
        Map<String, Object> variables = Map.of(
                "title", "Test Template",
                "message", "This email was generated from a Thymeleaf template.",
                "link", "http://localhost:3000"
        );
        emailService.sendTemplateMessage(to, "Test Template Email", "test-email", variables);
        return ResponseEntity.ok("Template email sent to " + to);
    }
}
