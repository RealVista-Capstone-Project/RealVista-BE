package com.sep.realvista.application.notification.service;

import com.sep.realvista.application.notification.dto.CreateNotificationTemplateRequest;
import com.sep.realvista.application.notification.dto.NotificationTemplateResponse;
import com.sep.realvista.application.notification.dto.UpdateNotificationTemplateRequest;
import com.sep.realvista.domain.user.notification.NotificationTemplate;
import com.sep.realvista.domain.user.notification.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationTemplateApplicationService {

    private final NotificationTemplateRepository repository;

    @Transactional(readOnly = true)
    public Page<NotificationTemplateResponse> getAllTemplates(String search, String type, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.findByNameContainingIgnoreCase(search, pageable).map(this::toDto);
        }
        return repository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public NotificationTemplateResponse getTemplate(UUID id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Template not found"));
    }

    public NotificationTemplateResponse createTemplate(CreateNotificationTemplateRequest request) {
        NotificationTemplate template = NotificationTemplate.builder()
                .templateKey(request.getTemplateKey())
                .name(request.getName())
                .type(request.getType())
                .language(request.getLanguage())
                .title(request.getTitle())
                .contentBody(request.getContentBody())
                .build();
        return toDto(repository.save(template));
    }

    public NotificationTemplateResponse updateTemplate(UUID id, UpdateNotificationTemplateRequest request) {
        NotificationTemplate template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        template.update(request.getName(), request.getTitle(), request.getContentBody());

        return toDto(repository.save(template));
    }

    public void deleteTemplate(UUID id) {
        repository.deleteById(id);
    }

    private NotificationTemplateResponse toDto(NotificationTemplate template) {
        return NotificationTemplateResponse.builder()
                .templateId(template.getTemplateId())
                .templateKey(template.getTemplateKey())
                .name(template.getName())
                .type(template.getType())
                .language(template.getLanguage())
                .title(template.getTitle())
                .contentBody(template.getContentBody())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
