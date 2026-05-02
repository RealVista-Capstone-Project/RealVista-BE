package com.sep.realvista.unit.application.policy;

import com.sep.realvista.application.policy.PolicyService;
import com.sep.realvista.application.policy.dto.PolicyDto;
import com.sep.realvista.application.policy.mapper.PolicyMapper;
import com.sep.realvista.domain.policy.Policy;
import com.sep.realvista.domain.policy.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyMapper policyMapper;

    @InjectMocks
    private PolicyService policyService;

    private Policy policy;
    private PolicyDto policyDto;
    private UUID policyId;

    @BeforeEach
    void setUp() {
        policyId = UUID.randomUUID();
        policy = Policy.builder()
                .policyId(policyId)
                .title("Original Title")
                .slug("test-slug")
                .content("Original Content")
                .isActive(true)
                .build();

        policyDto = PolicyDto.builder()
                .policyId(policyId)
                .title("Original Title")
                .slug("test-slug")
                .content("Original Content")
                .isActive(true)
                .build();
    }

    @Test
    void getPolicyBySlug_ShouldReturnPolicyDto() {
        when(policyRepository.findBySlug("test-slug")).thenReturn(Optional.of(policy));
        when(policyMapper.toDto(policy)).thenReturn(policyDto);

        PolicyDto result = policyService.getPolicyBySlug("test-slug");

        assertThat(result).isNotNull();
        assertThat(result.getSlug()).isEqualTo("test-slug");
    }

    @Test
    void getPolicyBySlug_WhenNotFound_ShouldThrowException() {
        when(policyRepository.findBySlug("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> policyService.getPolicyBySlug("invalid"))
                .isInstanceOf(RuntimeException.class); // Changed from IllegalArgumentException because ResourceNotFoundException is a RuntimeException
    }

    @Test
    void getAllPolicies_ShouldReturnListOfPolicyDtos() {
        when(policyRepository.findAll()).thenReturn(List.of(policy));
        when(policyMapper.toDto(policy)).thenReturn(policyDto);

        List<PolicyDto> results = policyService.getAllPolicies();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSlug()).isEqualTo("test-slug");
    }

    @Test
    void updatePolicy_ShouldUpdateAndReturnPolicyDto() {
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);
        when(policyMapper.toDto(policy)).thenReturn(policyDto);

        PolicyDto result = policyService.updatePolicy(policyId, "New Title", "New Content", false);

        assertThat(policy.getTitle()).isEqualTo("New Title");
        assertThat(policy.getContent()).isEqualTo("New Content");
        assertThat(policy.getIsActive()).isFalse();
        verify(policyRepository).save(policy);
        assertThat(result).isNotNull();
    }

    @Test
    void updatePolicy_WhenNotFound_ShouldThrowException() {
        UUID invalidId = UUID.randomUUID();
        when(policyRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> policyService.updatePolicy(invalidId, "Title", "Content", true))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
