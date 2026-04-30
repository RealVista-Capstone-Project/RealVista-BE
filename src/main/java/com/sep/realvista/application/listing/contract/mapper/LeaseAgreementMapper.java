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
    @Mapping(target = "renterFullName", source = "renter.fullName")
    @Mapping(target = "renterEmail", source = "renter.email")
    @Mapping(target = "renterPhone", source = "renter.phoneNumber")
    @Mapping(target = "renterAvatarUrl", source = "renter.avatarUrl")
    @Mapping(target = "landlordFullName", source = "landlord.fullName")
    @Mapping(target = "landlordEmail", source = "landlord.email")
    @Mapping(target = "landlordPhone", source = "landlord.phoneNumber")
    @Mapping(target = "landlordAvatarUrl", source = "landlord.avatarUrl")
    @Mapping(target = "propertyTitle", source = "property.title")
    @Mapping(target = "propertyAddress", source = "property.address")
    @Mapping(target = "propertyType", source = "property.propertyType")
    LeaseResponse toResponse(LeaseAgreement leaseAgreement);
}
