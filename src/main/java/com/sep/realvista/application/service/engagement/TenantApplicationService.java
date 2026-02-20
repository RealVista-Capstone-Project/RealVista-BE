package com.sep.realvista.application.service.engagement;

import com.sep.realvista.application.engagement.mapper.TenantApplicationMapper;
import com.sep.realvista.application.listing.dto.TenantApplicationDto;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.engagement.rental.TenantApplication;
import com.sep.realvista.domain.engagement.rental.repository.TenantApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantApplicationService {

    private final TenantApplicationRepository tenantApplicationRepository;
    private final TenantApplicationMapper tenantApplicationMapper;

    @Transactional(readOnly = true)
    public List<TenantApplicationDto> getMyApplications(UUID userId) {
        List<TenantApplication> applications = tenantApplicationRepository.findByUserId(userId);
        return applications.stream()
                .map(tenantApplicationMapper::toDto)
                .toList();
    }

    @Transactional
    public void softDeleteApplication(UUID applicationId, UUID userId) {
        TenantApplication application = tenantApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant Application", applicationId));

        if (!application.getUserId().equals(userId)) {
             throw new SecurityException("You are not authorized to delete this application");
        }

        tenantApplicationRepository.delete(application);
    }
}
