package com.sep.realvista.infrastructure.external.email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "email.provider", havingValue = "sendgrid")
@Slf4j
public class SendGridEmailProvider implements EmailProvider {

    private final SendGrid sendGrid;

    public SendGridEmailProvider(@Value("${sendgrid.api-key}") String apiKey) {
        this.sendGrid = new SendGrid(apiKey);
    }

    @Override
    public void sendSimple(String from, String to, String subject, String text) {
        sendMail(from, to, subject, new Content("text/plain", text));
    }

    @Override
    public void sendHtml(String from, String to, String subject, String htmlBody) {
        sendMail(from, to, subject, new Content("text/html", htmlBody));
    }

    private void sendMail(String from, String to, String subject, Content content) {
        Mail mail = new Mail(new Email(from), subject, new Email(to), content);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            if (response.getStatusCode() >= 400) {
                log.error("SendGrid rejected email to {} — status: {}, body: {}",
                        to, response.getStatusCode(), response.getBody());
                throw new RuntimeException("SendGrid delivery failed with status " + response.getStatusCode());
            }
            log.info("SendGrid email sent to {} — status: {}", to, response.getStatusCode());
        } catch (IOException e) {
            log.error("SendGrid IO error sending email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email via SendGrid: " + e.getMessage(), e);
        }
    }
}
