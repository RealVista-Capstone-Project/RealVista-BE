package com.sep.realvista.application.policy;

import com.sep.realvista.application.policy.dto.PolicyDto;
import com.sep.realvista.application.policy.mapper.PolicyMapper;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.policy.Policy;
import com.sep.realvista.domain.policy.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyMapper policyMapper;

    @Transactional(readOnly = true)
    public PolicyDto getPolicyBySlug(String slug) {
        Policy policy = policyRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with slug: " + slug));
        return policyMapper.toDto(policy);
    }

    @Transactional(readOnly = true)
    public PolicyDto getActivePolicyBySlug(String slug) {
        Policy policy = policyRepository.findActiveBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Active policy not found with slug: " + slug));
        return policyMapper.toDto(policy);
    }

    @Transactional(readOnly = true)
    public List<PolicyDto> getAllPolicies() {
        return policyRepository.findAll().stream()
                .map(policyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PolicyDto> getAllActivePolicies() {
        return policyRepository.findAllActive().stream()
                .map(policyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PolicyDto updatePolicy(UUID id, String title, String content) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with id: " + id));
        policy.updatePolicy(title, content);
        Policy saved = policyRepository.save(policy);
        return policyMapper.toDto(saved);
    }
}
