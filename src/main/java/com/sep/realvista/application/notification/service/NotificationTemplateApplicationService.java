package com.sep.realvista.application.notification.service;

import com.sep.realvista.application.notification.dto.*;
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
    public Page<NotificationTemplateResponse> getPagedTemplates(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDto);
    }

    public NotificationTemplateResponse createTemplate(CreateNotificationTemplateRequest request) {
        NotificationTemplate template = NotificationTemplate.builder()
                .templateName(request.getTemplateName())
                .slug(request.getSlug())
                .subjectTemplate(request.getSubjectTemplate())
                .body_template(request.getBody_template())
                .description(request.getDescription())
                .isActive(true)
                .build();
        return toDto(repository.save(template));
    }

    public NotificationTemplateResponse updateTemplate(UUID id, UpdateNotificationTemplateRequest request) {
        NotificationTemplate template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        if (request.getTemplateName() != null) template.setTemplateName(request.getTemplateName());
        if (request.getSubjectTemplate() != null) template.setSubjectTemplate(request.getSubjectTemplate());
        if (request.getBody_template() != null) template.setBody_template(request.getBody_template());
        if (request.getDescription() != null) template.setDescription(request.getDescription());
        if (request.getIsActive() != null) template.setActive(request.getIsActive());

        return toDto(repository.save(template));
    }

    public void deleteTemplate(UUID id) {
        repository.deleteById(id);
    }

    private NotificationTemplateResponse toDto(NotificationTemplate template) {
        return NotificationTemplateResponse.builder()
                .templateId(template.getTemplateId())
                .templateName(template.getTemplateName())
                .slug(template.getSlug())
                .subjectTemplate(template.getSubjectTemplate())
                .body_template(template.getBody_template())
                .description(template.getDescription())
                .isActive(template.isActive())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
