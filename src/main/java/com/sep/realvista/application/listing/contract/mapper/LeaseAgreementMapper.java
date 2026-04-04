package com.sep.realvista.application.listing.contract.mapper;

import com.sep.realvista.application.listing.contract.dto.LeaseResponse;
import com.sep.realvista.domain.listing.contract.LeaseAgreement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link LeaseAgreement} entity to/from DTOs.
 */
@Mapper(componentModel = "spring")
public interface LeaseAgreementMapper {

    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    LeaseResponse toResponse(LeaseAgreement leaseAgreement);
}
