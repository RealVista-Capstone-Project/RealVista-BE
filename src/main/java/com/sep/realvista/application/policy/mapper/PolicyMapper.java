package com.sep.realvista.application.policy.mapper;

import com.sep.realvista.application.policy.dto.PolicyDto;
import com.sep.realvista.domain.policy.Policy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PolicyMapper {
    PolicyDto toDto(Policy policy);
}
