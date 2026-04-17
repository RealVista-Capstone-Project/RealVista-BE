package com.sep.realvista.infrastructure.service;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class NotificationMessageService {

    private final MessageSource messageSource;

    public NotificationMessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String key, String lang) {
        Locale locale = (lang == null || lang.isEmpty()) ? Locale.of("vi") : Locale.of(lang.toLowerCase());
        try {
            return messageSource.getMessage(key, null, locale);
        } catch (Exception e) {
            return key;
        }
    }

    public String getMessage(String key, String lang, Object... args) {
        Locale locale = (lang == null || lang.isEmpty()) ? Locale.of("vi") : Locale.of(lang.toLowerCase());
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (Exception e) {
            return key;
        }
    }
}
