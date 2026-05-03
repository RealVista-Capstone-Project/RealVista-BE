package com.sep.realvista.application.service;

import java.util.concurrent.CompletableFuture;

public interface EmailService {
    /**
     * Send a simple email message.
     *
     * @param to      Recipient email address
     * @param subject Email subject
     * @param text    Email body text
     */
    void sendSimpleMessage(String to, String subject, String text);

    /**
     * Send an email message asynchronously.
     *
     * @param to      Recipient email address
     * @param subject Email subject
     * @param text    Email body text
     * @return CompletableFuture representing the async operation
     */
    CompletableFuture<Void> sendSimpleMessageAsync(String to, String subject, String text);

    /**
     * Send an HTML email message.
     *
     * @param to      Recipient email address
     * @param subject Email subject
     * @param htmlBody HTML body content
     */
    void sendHtmlMessage(String to, String subject, String htmlBody);

    /**
     * Send an HTML email message asynchronously.
     *
     * @param to      Recipient email address
     * @param subject Email subject
     * @param htmlBody HTML body content
     * @return CompletableFuture representing the async operation
     */
    CompletableFuture<Void> sendHtmlMessageAsync(String to, String subject, String htmlBody);

    /**
     * Send an email message using a template.
     *
     * @param to           Recipient email address
     * @param subject      Email subject
     * @param templateName Name of the template (in templates/mail/)
     * @param variables    Variables to be used in the template
     */
    void sendTemplateMessage(String to, String subject, String templateName, java.util.Map<String, Object> variables);

    /**
     * Send an email message using a template asynchronously.
     *
     * @param to           Recipient email address
     * @param subject      Email subject
     * @param templateName Name of the template (in templates/mail/)
     * @param variables    Variables to be used in the template
     * @return CompletableFuture representing the async operation
     */
    CompletableFuture<Void> sendTemplateMessageAsync(String to, String subject, String templateName, 
                                                     java.util.Map<String, Object> variables);

    /**
     * Send an email message using a template from the database.
     *
     * @param to           Recipient email address
     * @param templateKey  Key of the template in database
     * @param language     Language of the template (e.g., "en", "vi")
     * @param variables    Variables to be used in the template
     */
    void sendDbTemplateMessage(String to, String templateKey, String language, java.util.Map<String, Object> variables);

    /**
     * Send an email message using a template from the database asynchronously.
     *
     * @param to           Recipient email address
     * @param templateKey  Key of the template in database
     * @param language     Language of the template (e.g., "en", "vi")
     * @param variables    Variables to be used in the template
     * @return CompletableFuture representing the async operation
     */
    CompletableFuture<Void> sendDbTemplateMessageAsync(String to, String templateKey, String language, 
                                                       java.util.Map<String, Object> variables);
}
