package com.sep.realvista.infrastructure.external.email;

/**
 * Strategy interface for email delivery providers.
 * Implementations include SMTP and SendGrid.
 */
public interface EmailProvider {

    /**
     * Send a plain-text email.
     *
     * @param from    Sender address
     * @param to      Recipient address
     * @param subject Email subject
     * @param text    Plain-text body
     */
    void sendSimple(String from, String to, String subject, String text);

    /**
     * Send an HTML email.
     *
     * @param from     Sender address
     * @param to       Recipient address
     * @param subject  Email subject
     * @param htmlBody HTML body content
     */
    void sendHtml(String from, String to, String subject, String htmlBody);
}
