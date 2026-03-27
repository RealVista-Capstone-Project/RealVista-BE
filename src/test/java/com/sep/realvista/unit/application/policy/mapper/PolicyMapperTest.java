package com.sep.realvista.unit.application.policy.mapper;

import com.sep.realvista.application.policy.dto.PolicyDto;
import com.sep.realvista.application.policy.mapper.PolicyMapper;
import com.sep.realvista.application.policy.mapper.PolicyMapperImpl;
import com.sep.realvista.domain.policy.Policy;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyMapperTest {

    private final PolicyMapper mapper = new PolicyMapperImpl();

    @Test
    void shouldMapPolicyToDto() {
        UUID id = UUID.randomUUID();
        Policy policy = Policy.builder()
                .policyId(id)
                .title("Terms")
                .slug("terms-of-service")
                .content("<p>Content</p>")
                .build();

        PolicyDto dto = mapper.toDto(policy);

        assertThat(dto).isNotNull();
        assertThat(dto.getPolicyId()).isEqualTo(id);
        assertThat(dto.getTitle()).isEqualTo("Terms");
        assertThat(dto.getSlug()).isEqualTo("terms-of-service");
        assertThat(dto.getContent()).isEqualTo("<p>Content</p>");
    }
}
